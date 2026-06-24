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
          icon: "share.extension",
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

    /// Raw consents returned by the SDK — keyed by category code string for O(1) lookup.
    @State private var consentsByCategory: [String: BWell.GetConsentBundleEntry.GetConsentResource] = [:]
    @State private var isLoading = false
    @State private var errorMessage: String?
    /// Category currently being submitted (to show per-row spinner).
    @State private var submitting: Set<String> = []
    @State private var toastMessage: String?
    /// Optimistic provision overrides — updated immediately on successful createConsent
    /// since getConsents is unavailable in the pre-v1.4.3 binary (DCON-4083).
    @State private var localProvisions: [String: String] = [:]

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
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .task {
            // getConsents leaks its continuation in the pre-v1.4.3 SDK binary (DCON-4083).
            // Skip the load so the screen is usable; createConsent still works for QA.
            isLoading = false
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
                Section {
                    Label("Current consent status unavailable — getConsents requires SDK v1.4.3 (DCON-4083). Permit/Deny still works for QA.", systemImage: "exclamationmark.triangle")
                        .font(.caption)
                        .foregroundStyle(.orange)
                }
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
        let provisionType = existing?.provision?.type ?? localProvisions[key]
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

                    if let consentId = existing?.id {
                        Spacer()
                        Text("ID: \(consentId.prefix(8))…")
                            .font(.caption2)
                            .foregroundStyle(.tertiary)
                            .monospaced()
                    }
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

    private func loadConsents() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        errorMessage = nil
        do {
            // getConsents(nil) leaks its continuation in the pre-v1.4.3 SDK binary,
            // causing an infinite hang. Race it against a timeout so we fail fast.
            // TODO: remove timeout wrapper once swift-sdk-v1.4.3 is linked.
            let result = try await withTimeout(seconds: 8) {
                try await sdk.user.getConsents(nil)
            }
            var map: [String: BWell.GetConsentBundleEntry.GetConsentResource] = [:]
            for entry in result?.entry ?? [] {
                guard let resource = entry.resource else { continue }
                if let code = resource.category?.first?.coding?.first?.code {
                    map[code] = resource
                }
            }
            consentsByCategory = map
        } catch is SDKTimeoutError {
            errorMessage = "Consent data unavailable — SDK binary must be updated to v1.4.3 to fix this. (DCON-4083)"
        } catch {
            errorMessage = "Failed to load consents."
        }
        isLoading = false
    }

    /// Races `operation` against a deadline. Throws `SDKTimeoutError` if the deadline fires first.
    private func withTimeout<T: Sendable>(
        seconds: Double,
        operation: @Sendable @escaping () async throws -> T
    ) async throws -> T {
        try await withThrowingTaskGroup(of: T.self) { group in
            group.addTask { try await operation() }
            group.addTask {
                try await Task.sleep(nanoseconds: UInt64(seconds * 1_000_000_000))
                throw SDKTimeoutError()
            }
            let result = try await group.next()!
            group.cancelAll()
            return result
        }
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
            // Optimistically update local state — getConsents hangs in pre-v1.4.3 binary (DCON-4083)
            localProvisions[key] = type == .permit ? "permit" : "deny"
            withAnimation {
                toastMessage = "\(type == .permit ? "✓ Permitted" : "✗ Denied"): \(displayName(for: category))"
            }
        } catch {
            withAnimation {
                toastMessage = "Failed to update consent."
            }
        }
    }

    // MARK: - Helpers

    /// Maps a CategoryCode to the wire string the SDK sends (mirrors SDK's description() / rawValue logic).
    private func categoryKey(_ code: BWell.CategoryCode) -> String {
        switch code {
        case .tos:                          return "TOS"
        case .healthMatch:                  return "healthMatch"
        case .iasImportRecords:             return "ias:import:records"
        case .communicationPreferencesPHI:  return "communicationPreferences:includePHI"
        case .dataSharing:                  return "dataSharing"
        case .personalizedHealthOffersAndADS: return "personalizedHealthOffersAndAds"
        case .mobileCommunicationPreferences: return "mobileCommunicationPreferences"
        case .proaAttestation:              return "proaAttestation"
case .healthCircleAdolescent:       return "healthCircleAdolescent"
        case .healthCircleMinor:            return "healthCircleMinor"
        case .unknown:                      return "unknown"
        }
    }

    private func displayName(for code: BWell.CategoryCode) -> String {
        allCategories.first { $0.id == code }?.displayName ?? "\(code)"
    }
}

private struct SDKTimeoutError: Error {}
