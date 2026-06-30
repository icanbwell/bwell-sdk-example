//
//  ConsentsView.swift
//  bwell-swift-ios
//
//  Displays all consent records for the authenticated user and allows
//  creating / updating consent provisions per category.
//  Added for DCON-4083 QA — validates Swift SDK consent category fixes.
//

import SwiftUI
import BWellSDK

// MARK: - Known consent categories with display metadata

private struct ConsentCategoryInfo: Identifiable {
    let id: BWell.CategoryCode
    let displayName: String
    let icon: String
    let description: String
}

private let allCategories: [ConsentCategoryInfo] = [
    .init(id: .tos,
          displayName: "Terms of Service",
          icon: "doc.text",
          description: "Agreement to b.well terms and conditions."),
    .init(id: .iasImportRecords,
          displayName: "IAS Import Records",
          icon: "arrow.down.doc",
          description: "Permission to import health records via Smart Connect."),
    .init(id: .healthMatch,
          displayName: "Health Match",
          icon: "heart.text.square",
          description: "Personalized recommendations using your health data."),
    .init(id: .dataSharing,
          displayName: "Data Sharing",
          icon: "square.and.arrow.up",
          description: "Sharing health data with authorized parties."),
    .init(id: .communicationPreferencesPHI,
          displayName: "Communication Preferences (PHI)",
          icon: "envelope",
          description: "Communications that include protected health information."),
    .init(id: .mobileCommunicationPreferences,
          displayName: "Mobile Communications",
          icon: "iphone",
          description: "Push notifications and mobile messaging preferences."),
    .init(id: .personalizedHealthOffersAndADS,
          displayName: "Personalized Offers & Ads",
          icon: "tag",
          description: "Personalized health offers and advertisements."),
    .init(id: .proaAttestation,
          displayName: "PROA Attestation",
          icon: "checkmark.seal",
          description: "Patient-requested online access attestation."),
    .init(id: .healthCircleAdolescent,
          displayName: "Health Circle (Adolescent)",
          icon: "person.2",
          description: "Health circle access for adolescent users."),
    .init(id: .healthCircleMinor,
          displayName: "Health Circle (Minor)",
          icon: "person.2.fill",
          description: "Health circle access for minor users."),
]

// MARK: - View

struct ConsentsView: View {
    @EnvironmentObject private var sdkManager: SDKManager

    // MARK: - Session-level cache
    // @State resets when SwiftUI destroys and recreates the view (every NavigationStack push/pop).
    // These static properties survive view recreation, so the optimistic consent state set in
    // one navigation is still visible on the next. The fresh .task load then confirms or updates.
    private static var cachedConsents: [String: BWell.GetConsentBundleEntry.GetConsentResource] = [:]
    private static var cachedProvisions: [String: String] = [:]

    /// Raw consents returned by the SDK — keyed by category code string for O(1) lookup.
    @State private var consentsByCategory: [String: BWell.GetConsentBundleEntry.GetConsentResource] = ConsentsView.cachedConsents
    @State private var isLoading = false
    @State private var errorMessage: String?
    /// Category currently being submitted (to show per-row spinner).
    @State private var submitting: Set<String> = []
    @State private var toastMessage: String?
    /// Optimistic provision overrides — applied immediately after createConsent
    /// while the background getConsents refresh is in-flight.
    @State private var localProvisions: [String: String] = ConsentsView.cachedProvisions

    var body: some View {
        ZStack(alignment: .bottom) {
            content

            if let toast = toastMessage {
                Text(toast)
                    .font(.footnote)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 10))
                    .padding(.bottom, 20)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .task(id: toastMessage) {
                        try? await Task.sleep(for: .seconds(2.5))
                        withAnimation { toastMessage = nil }
                    }
            }
        }
        .navigationTitle("Consents")
        .toolbarColorScheme(.dark, for: .navigationBar)

        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .task {
            await loadConsents()
        }
    }

    // MARK: - Content

    @ViewBuilder
    private var content: some View {
        if isLoading && consentsByCategory.isEmpty {
            ProgressView("Loading consents...")
        } else if let error = errorMessage, consentsByCategory.isEmpty {
            ContentUnavailableView {
                Label("Error", systemImage: "exclamationmark.triangle")
            } description: {
                Text(error)
            } actions: {
                Button("Retry") { Task { await loadConsents() } }
            }
        } else {
            List {
                ForEach(allCategories) { info in
                    consentRow(info)
                }
            }
            .listStyle(.insetGrouped)
        }
    }

    // MARK: - Consent Row

    @ViewBuilder
    private func consentRow(_ info: ConsentCategoryInfo) -> some View {
        let key = categoryKey(info.id)
        let existing = consentsByCategory[key]
        let provisionType = localProvisions[key] ?? existing?.provision?.type
        let isSubmittingThis = submitting.contains(key)

        VStack(alignment: .leading, spacing: 8) {
            // Header
            HStack(spacing: 12) {
                Image(systemName: info.icon)
                    .font(.title3)
                    .foregroundStyle(.bwellPurple)
                    .frame(width: 28, alignment: .center)

                VStack(alignment: .leading, spacing: 2) {
                    Text(info.displayName)
                        .font(.subheadline)
                        .fontWeight(.medium)
                    Text(info.description)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .fixedSize(horizontal: false, vertical: true)
                }

                Spacer()

                if isSubmittingThis {
                    ProgressView()
                        .controlSize(.small)
                } else {
                    provisionBadge(provisionType)
                }
            }

            // Action Buttons
            if !isSubmittingThis {
                HStack(spacing: 8) {
                    Button {
                        Task { await submitConsent(category: info.id, type: .permit) }
                    } label: {
                        Label("Permit", systemImage: "checkmark")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(provisionType == "permit" ? Color.green : Color.green.opacity(0.12))
                            .foregroundStyle(provisionType == "permit" ? .white : .green)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                    .buttonStyle(.plain)

                    Button {
                        Task { await submitConsent(category: info.id, type: .deny) }
                    } label: {
                        Label("Deny", systemImage: "xmark")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(provisionType == "deny" ? Color.red : Color.red.opacity(0.12))
                            .foregroundStyle(provisionType == "deny" ? .white : .red)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                    .buttonStyle(.plain)

                }
            }
        }
        .padding(.vertical, 6)
    }

    // MARK: - Provision Badge

    @ViewBuilder
    private func provisionBadge(_ type: String?) -> some View {
        switch type {
        case "permit":
            Label("Permit", systemImage: "checkmark.circle.fill")
                .font(.caption2)
                .fontWeight(.semibold)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.green.opacity(0.15))
                .foregroundStyle(.green)
                .clipShape(Capsule())
        case "deny":
            Label("Deny", systemImage: "xmark.circle.fill")
                .font(.caption2)
                .fontWeight(.semibold)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.red.opacity(0.15))
                .foregroundStyle(.red)
                .clipShape(Capsule())
        default:
            Label("None", systemImage: "minus.circle")
                .font(.caption2)
                .fontWeight(.medium)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color.secondary.opacity(0.12))
                .foregroundStyle(.secondary)
                .clipShape(Capsule())
        }
    }

    // MARK: - Data Loading

    /// Workaround: GetConsentsRequest has no public init in the binary SDK (memberwise init is
    /// internal). Its only stored property is `category: BWell.CategoryCode`, so its memory
    /// layout IS a CategoryCode — reinterpret as GetConsentsRequest directly (same technique
    /// used for DeregisterDeviceRequest in DeviceRegistrationView).
    private func makeGetConsentsRequest(_ category: BWell.CategoryCode) -> BWell.GetConsentsRequest {
        withUnsafePointer(to: category) { ptr in
            ptr.withMemoryRebound(to: BWell.GetConsentsRequest.self, capacity: 1) { $0.pointee }
        }
    }

    private func loadConsents(showLoading: Bool = true) async {
        guard let sdk = sdkManager.sdk else { return }
        if showLoading {
            isLoading = true
            errorMessage = nil
        }
        do {
            // Workaround for DCON-4298: getConsents(nil) leaks its continuation in the
            // v1.4.3-beta binary. Fetch per-category with non-nil requests to avoid the
            // nil path in GraphQLUserManager. Remove once SDK binary is rebuilt with fix.
            var map: [String: BWell.GetConsentBundleEntry.GetConsentResource] = [:]
            let categories: [BWell.CategoryCode] = [
                .tos, .iasImportRecords, .healthMatch, .dataSharing,
                .communicationPreferencesPHI, .mobileCommunicationPreferences,
                .personalizedHealthOffersAndADS, .proaAttestation,
                .healthCircleAdolescent, .healthCircleMinor
            ]
            for category in categories {
                let result = try await sdk.user.getConsents(makeGetConsentsRequest(category))
                // createConsent appends new records rather than updating in place, so the
                // server may return multiple entries for the same category (e.g. an old
                // "deny" plus a newer "permit"). The server returns entries in insertion
                // order (oldest first), so .last is the most recently created consent.
                // meta.lastUpdated is nil on these records so we can't sort by timestamp.
                // Key by categoryKey (same string consentRow uses) — server coding values
                // differ from our lookup keys and would cause every lookup to miss.
                if let resource = (result?.entry ?? []).last?.resource {
                    map[categoryKey(category)] = resource
                }
            }
            consentsByCategory = map
            ConsentsView.cachedConsents = map
            // Only evict a key from the optimistic cache when the server has caught up to
            // the value we set. If the server still returns the old value (mutation hasn't
            // propagated yet), keep the local override so the badge doesn't snap back.
            for key in map.keys {
                guard let pending = localProvisions[key] else { continue }
                if map[key]?.provision?.type == pending {
                    localProvisions.removeValue(forKey: key)
                    ConsentsView.cachedProvisions.removeValue(forKey: key)
                }
            }
        } catch {
            if showLoading { errorMessage = "Failed to load consents." }
        }
        if showLoading { isLoading = false }
    }

    // MARK: - Consent Submission

    private func submitConsent(
        category: BWell.CategoryCode,
        type: BWell.ConsentProvisionInput.ConsentProvisionType
    ) async {
        guard let sdk = sdkManager.sdk else { return }
        let key = categoryKey(category)
        submitting.insert(key)
        defer { submitting.remove(key) }
        do {
            let request = BWell.CreateConsentRequest(
                status: .active,
                provision: .init(type: type),
                category: category
            )
            _ = try await sdk.user.createConsent(request)
            // Optimistic update while background refresh is in-flight.
            // Also write to the static cache so the state survives navigation
            // (SwiftUI resets @State when the view is destroyed on pop).
            let provisionValue = type == .permit ? "permit" : "deny"
            localProvisions[key] = provisionValue
            ConsentsView.cachedProvisions[key] = provisionValue
            withAnimation {
                toastMessage = "\(type == .permit ? "✓ Permitted" : "✗ Denied"): \(displayName(for: category))"
            }
            // Refresh from server to get authoritative state
            Task { await loadConsents(showLoading: false) }
        } catch {
            withAnimation {
                toastMessage = "Failed to update consent."
            }
        }
    }

    // MARK: - Helpers

    /// Maps a CategoryCode to the wire string the SDK sends.
    private func categoryKey(_ code: BWell.CategoryCode) -> String {
        switch code {
        case .tos:                             return "TOS"
        case .healthMatch:                     return "healthMatch"
        case .iasImportRecords:                return "ias:import:records"
        case .communicationPreferencesPHI:     return "communicationPreferences:includePHI"
        case .dataSharing:                     return "dataSharing"
        case .personalizedHealthOffersAndADS:  return "personalizedHealthOffersAndAds"
        case .mobileCommunicationPreferences:  return "mobileCommunicationPreferences"
        case .proaAttestation:                 return "proaAttestation"
        case .healthCircleAdolescent:          return "healthCircleAdolescent"
        case .healthCircleMinor:               return "healthCircleMinor"
        case .unknown:                         return "unknown"
        @unknown default:                      return "unknown"
        }
    }

    private func displayName(for code: BWell.CategoryCode) -> String {
        allCategories.first { $0.id == code }?.displayName ?? "\(code)"
    }
}
