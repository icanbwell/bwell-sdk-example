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
import BWell

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
    @Published var organizationType: OrganizationType = .all
    @Published var useLocation = true
    @Published var latitude: Double = 39.2848102
    @Published var longitude: Double = -76.702898

    private var cachedResults: [BWell.SearchHealthResourcesResults.Result] = []
    private var cancellables = Set<AnyCancellable>()
    private var debounceTask: Task<Void, Never>?
    private weak var sdk: BWellSDK?

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

    func configure(sdk: BWellSDK) {
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
        guard cachedResults.isEmpty, let sdk else { return }
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

    private func fetchProviders(sdk: BWellSDK, searchTerm: String?) async throws -> [BWell.SearchHealthResourcesResults.Result] {
        let filter = BWell.SearchHealthResourcesRequest.Filter(
            type: organizationType.filterValue,
            id: nil,
            includePopulatedProaOnly: includeProaOnly,
            gender: nil,
            includeInactive: false
        )

        let location = useLocation ? BWell.SearchHealthResourcesRequest.Location(
            latitude: latitude,
            longitude: longitude
        ) : nil

        let orderBy: [BWell.SearchHealthResourcesRequest.OrderBy] = useLocation ? [
            .init(field: .distance, order: .asc)
        ] : [
            .init(field: .content, order: .asc)
        ]

        let request = BWell.SearchHealthResourcesRequest(
            page: 0,
            pageSize: 100,
            search: searchTerm,
            filters: filter,
            orderBy: orderBy,
            location: location
        )

        let searchResults = try await sdk.search.searchHealthResources(request)
        return searchResults.results ?? []
    }

    func submitForReview(institution: String, provider: String, state: String, city: String, sdk: BWellSDK) async {
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
