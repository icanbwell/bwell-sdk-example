//
//  HealthMatchConsentView.swift
//  bwell-swift-ios
//
//  Bottom sheet for managing Health Match consent.
//

import SwiftUI
import BWellSDK

struct HealthMatchConsentView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @Environment(\.dismiss) private var dismiss
    @State private var isSubmitting = false
    @State private var errorMessage: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Health Match")
                .font(.title2)
                .fontWeight(.bold)

            Text("Health Match uses your health data to provide personalized recommendations and insights. By enabling Health Match, you consent to b.well analyzing your health records.")
                .font(.body)
                .foregroundStyle(.secondary)

            if let error = errorMessage {
                Text(error)
                    .font(.caption)
                    .foregroundStyle(.red)
            }

            Spacer()

            VStack(spacing: 12) {
                Button {
                    Task { await submitConsent(type: .permit) }
                } label: {
                    Text("Enable Health Match")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(.bwellPurple)
                        .foregroundStyle(.white)
                        .fontWeight(.semibold)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .disabled(isSubmitting)

                Button {
                    Task { await submitConsent(type: .deny) }
                } label: {
                    Text("No Thanks")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .foregroundStyle(.primary)
                        .fontWeight(.medium)
                }
                .disabled(isSubmitting)
            }
        }
        .padding()
        .presentationDetents([.medium])
        .presentationDragIndicator(.visible)
    }

    private func submitConsent(type: BWell.ConsentProvisionInput.ConsentProvisionType) async {
        guard let sdk = sdkManager.sdk else { return }
        isSubmitting = true
        do {
            let request = BWell.CreateConsentRequest(
                status: .active,
                provision: .init(type: type),
                category: .healthMatch
            )
            _ = try await sdk.user.createConsent(request)
            dismiss()
        } catch {
            errorMessage = "Failed to update consent."
            isSubmitting = false
        }
    }
}
