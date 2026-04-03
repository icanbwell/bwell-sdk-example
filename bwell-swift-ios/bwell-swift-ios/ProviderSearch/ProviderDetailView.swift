//
//  ProviderDetailView.swift
//  bwell-swift-ios
//

import SwiftUI
import BWell

struct ProviderDetailView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @Environment(\.dismiss) private var dismiss
    let result: BWell.SearchHealthResourcesResults.Result

    @State private var showWebView = false
    @State private var oauthURL: String?
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showSubmitReview = false
    @State private var showConnectSuccess = false

    // Submit for review form state
    @State private var institution: String = ""
    @State private var providerName: String = ""
    @State private var reviewState: String = ""
    @State private var reviewCity: String = ""
    @State private var showSubmitSuccess = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                // Provider name and type
                VStack(alignment: .leading, spacing: 8) {
                    Text(result.content ?? "Unknown Provider")
                        .font(.title)
                        .fontWeight(.bold)

                    if let type = result.type {
                        Text(providerTypeLabel(type))
                            .font(.subheadline)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(Color.bwellPurple.opacity(0.1))
                            .foregroundStyle(.bwellPurple)
                            .clipShape(Capsule())
                    }

                    if let specialties = result.specialty, !specialties.isEmpty {
                        let names = specialties.compactMap { $0.display }
                        if !names.isEmpty {
                            Text(names.joined(separator: ", "))
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .padding(.horizontal)

                Divider()

                // Locations
                if let locations = result.location, !locations.isEmpty {
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Locations")
                            .font(.headline)
                            .padding(.horizontal)

                        ForEach(locations.indices, id: \.self) { index in
                            LocationCard(location: locations[index])
                                .padding(.horizontal)
                        }
                    }

                    Divider()
                }

                // Endpoints
                if let endpoints = result.endpoint, !endpoints.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Endpoints")
                            .font(.headline)
                            .padding(.horizontal)

                        ForEach(endpoints.indices, id: \.self) { index in
                            if let name = endpoints[index].name {
                                HStack {
                                    Image(systemName: "link")
                                        .foregroundStyle(.secondary)
                                    Text(name)
                                        .font(.subheadline)
                                }
                                .padding(.horizontal)
                            }
                        }
                    }

                    Divider()
                }

                // Connect Button
                if let connectionId = result.endpoint?.first?.name {
                    VStack(spacing: 12) {
                        Button {
                            Task { await connectToProvider(connectionId: connectionId) }
                        } label: {
                            HStack {
                                if isLoading {
                                    ProgressView()
                                        .tint(.white)
                                } else {
                                    Text("Connect to Provider")
                                        .fontWeight(.semibold)
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(.bwellBlue)
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .disabled(isLoading)
                    }
                    .padding(.horizontal)

                    Divider()
                }

                // Can't find your provider section
                VStack(alignment: .leading, spacing: 12) {
                    Text("Can't find your provider?")
                        .font(.headline)

                    Text("Submit a provider for review and we'll work to add them to our network.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)

                    Button {
                        showSubmitReview = true
                    } label: {
                        Text("Submit Provider for Review")
                            .fontWeight(.semibold)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.bwellGreen.opacity(0.1))
                            .foregroundStyle(.bwellGreen)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
                .padding(.horizontal)
                .padding(.bottom, 24)
            }
            .padding(.top)
        }
        .navigationTitle("Provider Details")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button("Done") { dismiss() }
            }
        }
        .sheet(isPresented: $showWebView) {
            if let urlString = oauthURL, let url = URL(string: urlString) {
                WebViewWrapper(url: url) {
                    showWebView = false
                    showConnectSuccess = true
                }
            }
        }
        .alert("Connected!", isPresented: $showConnectSuccess) {
            Button("OK") { dismiss() }
        } message: {
            Text("Successfully connected to provider. Your health data will be available shortly.")
        }
        .sheet(isPresented: $showSubmitReview) {
            submitReviewSheet
                .presentationDetents([.medium, .large])
                .presentationDragIndicator(.visible)
        }
        .alert("Error", isPresented: .constant(errorMessage != nil)) {
            Button("OK") { errorMessage = nil }
        } message: {
            if let errorMessage = errorMessage {
                Text(errorMessage)
            }
        }
        .alert("Success", isPresented: $showSubmitSuccess) {
            Button("OK") {
                showSubmitReview = false
            }
        } message: {
            Text("Provider submitted for review successfully.")
        }
    }

    private var submitReviewSheet: some View {
        NavigationView {
            Form {
                Section(header: Text("Provider Information")) {
                    TextField("Institution Name", text: $institution)
                    TextField("Provider Name", text: $providerName)
                    TextField("State", text: $reviewState)
                    TextField("City", text: $reviewCity)
                }

                Section {
                    Button {
                        Task { await submitProviderReview() }
                    } label: {
                        HStack {
                            Spacer()
                            if isLoading {
                                ProgressView()
                            } else {
                                Text("Submit for Review")
                                    .fontWeight(.semibold)
                            }
                            Spacer()
                        }
                    }
                    .disabled(institution.isEmpty || isLoading)
                }
            }
            .navigationTitle("Submit Provider")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { showSubmitReview = false }
                }
            }
        }
    }

    private func connectToProvider(connectionId: String) async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        errorMessage = nil
        do {
            let request = BWell.OAuthURLRequest(connectionId: connectionId)
            let oauthResult = try await sdk.connection.getOAuthURL(request)
            oauthURL = oauthResult.redirectURL
            showWebView = true
        } catch {
            errorMessage = "Failed to get OAuth URL: \(error.localizedDescription)"
        }
        isLoading = false
    }

    private func submitProviderReview() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        errorMessage = nil
        do {
            let request = BWell.SubmitProviderForReviewRequest(
                institution: institution,
                provider: providerName,
                state: reviewState,
                city: reviewCity
            )
            _ = try await sdk.search.submitProviderForReview(request)
            showSubmitSuccess = true
        } catch {
            errorMessage = "Failed to submit provider for review: \(error.localizedDescription)"
        }
        isLoading = false
    }

    private func providerTypeLabel(_ type: BWell.ProviderType) -> String {
        switch type {
        case .practice: return "Practice"
        case .practitioner: return "Practitioner"
        case .insurance: return "Insurance"
        case .laboratory: return "Laboratory"
        case .pharmacy: return "Pharmacy"
        case .unknown(let value): return value.capitalized
        }
    }
}

// MARK: - Location Card

private struct LocationCard: View {
    let location: BWell.SearchHealthResourcesResults.Result.Location

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            if let name = location.name {
                Text(name)
                    .font(.subheadline)
                    .fontWeight(.semibold)
            }

            if let address = location.address {
                if let line = address.line?.joined(separator: "\n"), !line.isEmpty {
                    Text(line)
                        .font(.body)
                }
                HStack(spacing: 4) {
                    if let city = address.city { Text(city) }
                    if let state = address.state { Text(", \(state)") }
                    if let postalCode = address.postalCode { Text(postalCode) }
                }
                .font(.body)
            }

            if let distance = location.distanceInMiles {
                HStack(spacing: 4) {
                    Image(systemName: "location.fill")
                        .font(.caption)
                    Text(String(format: "%.1f miles away", distance))
                        .font(.caption)
                }
                .foregroundStyle(.bwellBlue)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
