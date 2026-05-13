//
//  ProviderSearchViewModel.swift
//  bwell-swift-ios
//
//  Typeahead pattern:
//  1. Load 100 nearby providers on appear (sorted by distance)
//  2. As user types → instant client-side filter on cached results
//  3. After 500ms pause with 3+ chars → server-side search for broader results
//  4. Display merged results: server results take priority, local matches fill gaps
//  5. On submit → immediate server search (no debounce)
//  6. On clear → restore cached nearby results
//

import Foundation
import Combine
import BWellSDK

@MainActor
final class ProviderSearchViewModel: ObservableObject {
    @Published var searchText: String = ""
    @Published var results: [BWell.SearchHealthResourcesResults.Result] = []
    @Published var isLoading = true
    @Published var hasInitiallyLoaded = false
    @Published var isSearching = false // subtle indicator for background server search
    @Published var errorMessage: String?
    @Published var showFilters = false

    // Filter state
    @Published var includeProaOnly = true
    @Published var includeInactive = false
    @Published var organizationType: OrganizationType = .all
    @Published var genderFilter: GenderFilter = .any
    @Published var patientAcceptance: PatientAcceptanceFilter = .any
    @Published var specialtyFilter: String = ""
    @Published var useLocation = false
    @Published var latitude: Double = 39.2848102
    @Published var longitude: Double = -76.702898
    @Published var radiusText: String = ""
    @Published var requestSpecialtyFilterValues = false
    @Published var requestCommunicationFilterValues = false
    @Published var requestInsurancePlanFilterValues = false

    private var cachedResults: [BWell.SearchHealthResourcesResults.Result] = []
    private var cancellables = Set<AnyCancellable>()
    private var debounceTask: Task<Void, Never>?
    private weak var sdk: BWellClient?

    enum OrganizationType: String, CaseIterable {
        case all = "All"
        case provider = "Provider"
        case laboratory = "Laboratory"

        var filterValue: [BWell.ProviderType]? {
            switch self {
            case .all: return nil
            case .provider: return [.practice, .practitioner]
            case .laboratory: return [.laboratory]
            }
        }
    }

    enum GenderFilter: String, CaseIterable {
        case any = "Any"
        case male = "Male"
        case female = "Female"

        var sdkValue: BWell.Gender? {
            switch self {
            case .any: return nil
            case .male: return .male
            case .female: return .female
            }
        }
    }

    enum PatientAcceptanceFilter: String, CaseIterable {
        case any = "Any"
        case newPatients = "New Patients"
        case existingPatients = "Existing Patients"

        var sdkValue: [BWell.PatientAcceptance]? {
            switch self {
            case .any: return nil
            case .newPatients: return [.newPatients]
            case .existingPatients: return [.existingPatients]
            }
        }
    }

    func configure(sdk: BWellClient) {
        self.sdk = sdk
        setupTypeahead()
    }

    // MARK: - Typeahead Pipeline

    private func setupTypeahead() {
        $searchText
            .removeDuplicates()
            .receive(on: RunLoop.main)
            .sink { [weak self] term in
                self?.handleSearchTextChange(term)
            }
            .store(in: &cancellables)
    }

    private func handleSearchTextChange(_ term: String) {
        let trimmed = term.trimmingCharacters(in: .whitespaces)

        // Cancel any pending server search
        debounceTask?.cancel()

        if trimmed.isEmpty {
            // Cleared — restore full cached list
            results = cachedResults
            isSearching = false
            return
        }

        // Instant client-side filter
        results = cachedResults.filter { result in
            guard let content = result.content else { return false }
            return content.localizedCaseInsensitiveContains(trimmed)
        }

        // Schedule debounced server search for 3+ chars
        guard trimmed.count >= 3 else { return }

        debounceTask = Task { [weak self] in
            do {
                try await Task.sleep(for: .milliseconds(500))
            } catch { return }
            guard !Task.isCancelled else { return }

            await self?.performServerSearch(term: trimmed)
        }
    }

    // MARK: - Initial Load

    func loadInitialResults() async {
        guard let sdk else { return }
        cachedResults = []
        isLoading = true
        errorMessage = nil

        do {
            let fetched = try await fetchProviders(sdk: sdk, searchTerm: nil)
            cachedResults = fetched
            results = fetched
        } catch {
            errorMessage = "Failed to load providers: \(error.localizedDescription)"
        }
        isLoading = false
        hasInitiallyLoaded = true
    }

    // MARK: - Server Search

    private func performServerSearch(term: String) async {
        guard let sdk else { return }
        isSearching = true

        do {
            let serverResults = try await fetchProviders(sdk: sdk, searchTerm: term)

            // Merge: server results first, then any local-only matches not in server set
            let serverIds = Set(serverResults.compactMap { $0.id })
            let localOnly = cachedResults.filter { result in
                guard let content = result.content, let id = result.id else { return false }
                return content.localizedCaseInsensitiveContains(term) && !serverIds.contains(id)
            }
            results = serverResults + localOnly
        } catch {
            // Keep client-side results on server error — don't disrupt the UX
        }
        isSearching = false
    }

    /// Explicit submit — immediate server search, no debounce
    func submitSearch() async {
        guard let sdk else { return }
        let term = searchText.trimmingCharacters(in: .whitespaces)

        debounceTask?.cancel()

        guard !term.isEmpty else {
            results = cachedResults
            return
        }

        isSearching = true
        do {
            let serverResults = try await fetchProviders(sdk: sdk, searchTerm: term)
            let serverIds = Set(serverResults.compactMap { $0.id })
            let localOnly = cachedResults.filter { result in
                guard let content = result.content, let id = result.id else { return false }
                return content.localizedCaseInsensitiveContains(term) && !serverIds.contains(id)
            }
            results = serverResults + localOnly
        } catch {
            errorMessage = "Search failed: \(error.localizedDescription)"
        }
        isSearching = false
    }

    // MARK: - API Call

    private func fetchProviders(sdk: BWellClient, searchTerm: String?) async throws -> [BWell.SearchHealthResourcesResults.Result] {
        let specialtyCodes: [BWell.Coding]? = {
            let trimmed = specialtyFilter.trimmingCharacters(in: .whitespaces)
            guard !trimmed.isEmpty else { return nil }
            return trimmed.split(separator: ",").map { code in
                BWell.Coding(id: nil, system: "specialty", code: String(code).trimmingCharacters(in: .whitespaces), display: nil)
            }
        }()

        let filter = BWell.SearchHealthResourcesRequest.Filter(
            type: organizationType.filterValue,
            id: nil,
            includePopulatedProaOnly: includeProaOnly,
            gender: genderFilter.sdkValue,
            includeInactive: includeInactive,
            specialty: specialtyCodes,
            patientAcceptance: patientAcceptance.sdkValue
        )

        let radius = Double(radiusText)

        let location = useLocation ? BWell.SearchHealthResourcesRequest.Location(
            latitude: latitude,
            longitude: longitude,
            radius: radius,
            radiusUnit: radius != nil ? .miles : nil
        ) : nil

        let orderBy: [BWell.SearchHealthResourcesRequest.OrderBy] = useLocation ? [
            .init(field: .distance, order: .asc)
        ] : [
            .init(field: .relevance, order: .desc)
        ]

        var filterValues: [BWell.FilterField]? = nil
        var requested: [BWell.FilterField] = []
        if requestSpecialtyFilterValues { requested.append(.specialty) }
        if requestCommunicationFilterValues { requested.append(.communication) }
        if requestInsurancePlanFilterValues { requested.append(.insurancePlan) }
        if !requested.isEmpty { filterValues = requested }

        let request = BWell.SearchHealthResourcesRequest(
            page: 0,
            pageSize: 100,
            search: searchTerm,
            filters: filter,
            orderBy: orderBy,
            location: location,
            filterValues: filterValues
        )

        let searchResults = try await sdk.search.searchHealthResources(request)

        #if DEBUG
        print("=== SHR Response ===")
        print("Total items: \(searchResults.pagingInfo?.totalItems ?? 0)")
        print("Results count: \(searchResults.results?.count ?? 0)")
        if let first = searchResults.results?.first {
            print("First result: id=\(first.id ?? "nil"), content=\(first.content ?? "nil"), type=\(String(describing: first.type))")
            print("  specialty: \(first.specialty?.compactMap { $0.display } ?? [])")
            print("  locations: \(first.location?.count ?? 0)")
            print("  score: \(String(describing: first.score))")
            print("  acceptingNewPatients: \(String(describing: first.acceptingNewPatients))")
            print("  bookable: \(String(describing: first.bookable))")
            print("  isVirtualCare: \(String(describing: first.isVirtualCare))")
            print("  communication: \(first.communication?.compactMap { $0.text } ?? [])")
            print("  insurancePlan: \(String(describing: first.insurancePlan))")
            print("  partOf: \(String(describing: first.partOf))")
            print("  reviewScore: \(String(describing: first.reviewScore))")
            print("  endpoint: \(first.endpoint?.compactMap { $0.name } ?? [])")
        }
        if let filterValues = searchResults.filterValues, !filterValues.isEmpty {
            print("Filter values:")
            for fv in filterValues {
                print("  \(String(describing: fv.field)): \(fv.values?.count ?? 0) values")
            }
        }
        print("====================")
        #endif

        return searchResults.results ?? []
    }

    func submitForReview(institution: String, provider: String, state: String, city: String, sdk: BWellClient) async {
        isLoading = true
        errorMessage = nil
        do {
            let request = BWell.SubmitProviderForReviewRequest(
                institution: institution,
                provider: provider,
                state: state,
                city: city
            )
            _ = try await sdk.search.submitProviderForReview(request)
        } catch {
            errorMessage = "Failed to submit: \(error.localizedDescription)"
        }
        isLoading = false
    }
}
