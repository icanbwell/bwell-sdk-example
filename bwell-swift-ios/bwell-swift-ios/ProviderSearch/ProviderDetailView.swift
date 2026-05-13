//
//  ProviderDetailView.swift
//  bwell-swift-ios
//

import SwiftUI
import BWellSDK

struct ProviderDetailView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @Environment(\.dismiss) private var dismiss
    let result: BWell.SearchHealthResourcesResults.Result

    @State private var showWebView = false
    @State private var oauthURL: String?
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showErrorAlert = false
    @State private var showSubmitReview = false
    @State private var showConnectSuccess = false
    @State private var isPCP: Bool
    @State private var isInCareTeam: Bool

    init(result: BWell.SearchHealthResourcesResults.Result, isPCP: Bool = false, isInCareTeam: Bool = false) {
        self.result = result
        _isPCP = State(initialValue: isPCP)
        _isInCareTeam = State(initialValue: isInCareTeam)
    }

    @State private var institution = ""
    @State private var providerName = ""
    @State private var reviewState = ""
    @State private var reviewCity = ""
    @State private var showSubmitSuccess = false


    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                headerSection
                Divider()

                if let locations = result.location, !locations.isEmpty {
                    locationsSection(locations)
                    Divider()
                }

                if let endpoints = result.endpoint, !endpoints.isEmpty {
                    endpointsSection(endpoints)
                    Divider()
                }

                if result.type != .practice {
                    careTeamSection
                    Divider()
                }

                if let connectionId = result.endpoint?.first?.name {
                    connectSection(connectionId)
                    Divider()
                }

                submitProviderSection
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
        .alert("Error", isPresented: $showErrorAlert) {
            Button("OK") { errorMessage = nil }
        } message: {
            if let errorMessage { Text(errorMessage) }
        }
        .onChange(of: errorMessage) { _, newValue in
            showErrorAlert = newValue != nil
        }
        .alert("Success", isPresented: $showSubmitSuccess) {
            Button("OK") { showSubmitReview = false }
        } message: {
            Text("Provider submitted for review successfully.")
        }
    }

    // MARK: - Sections

    private var headerSection: some View {
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
    }

    private func locationsSection(_ locations: [BWell.SearchHealthResourcesResults.Result.Location]) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Locations")
                .font(.headline)
                .padding(.horizontal)

            ForEach(locations.indices, id: \.self) { index in
                LocationCard(location: locations[index])
                    .padding(.horizontal)
            }
        }
    }

    private func endpointsSection(_ endpoints: [BWell.SearchHealthResourcesResults.Result.Endpoint]) -> some View {
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
    }

    private var careTeamSection: some View {
        VStack(spacing: 12) {
            Toggle(isOn: Binding(
                get: { isPCP },
                set: { newValue in
                    isPCP = newValue
                    Task { await togglePCP(newValue) }
                }
            )) {
                HStack(spacing: 6) {
                    Image(systemName: "star.fill")
                        .font(.caption)
                        .foregroundStyle(.orange)
                    Text("Set as Primary Care Provider (PCP)")
                        .font(.subheadline)
                }
            }
            .tint(.bwellPurple)

            if isInCareTeam {
                Button {
                    Task { await removeFromCareTeam() }
                } label: {
                    HStack {
                        Image(systemName: "person.badge.minus")
                        Text("Remove from Care Team").fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.red.opacity(0.1))
                    .foregroundStyle(.red)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            } else {
                Button {
                    Task { await addToCareTeam() }
                } label: {
                    HStack {
                        Image(systemName: "person.badge.plus")
                        Text("Add to Care Team").fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.bwellPurple.opacity(0.1))
                    .foregroundStyle(.bwellPurple)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
        }
        .padding(.horizontal)
    }

    private func connectSection(_ connectionId: String) -> some View {
        VStack(spacing: 12) {
            Button {
                Task { await connectToProvider(connectionId: connectionId) }
            } label: {
                HStack {
                    if isLoading {
                        ProgressView().tint(.white)
                    } else {
                        Text("Connect to Provider").fontWeight(.semibold)
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
    }

    private var submitProviderSection: some View {
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
                            if isLoading { ProgressView() } else { Text("Submit for Review").fontWeight(.semibold) }
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

    // MARK: - Actions

    private func addToCareTeam() async {
        guard let sdk = sdkManager.sdk, let id = result.id else {
            errorMessage = "Cannot add provider without an ID"
            return
        }
        errorMessage = nil
        do {
            let roles = isPCP ? ["PCP"] : []
            let request = BWell.AddCareTeamMemberRequest(id: id, type: .Practitioner, role: roles)
            _ = try await sdk.health.addCareTeamMember(request)
            isInCareTeam = true
            NotificationCenter.default.post(name: .careTeamUpdated, object: nil, userInfo: ["id": id, "isPCP": isPCP])
        } catch {
            errorMessage = "Failed to add to care team: \(error.localizedDescription)"
        }
    }

    private func removeFromCareTeam() async {
        guard let sdk = sdkManager.sdk, let id = result.id else { return }
        errorMessage = nil
        do {
            let request = BWell.RemoveCareTeamMemberRequest(id: id, type: .Practitioner)
            _ = try await sdk.health.removeCareTeamMember(request)
            isInCareTeam = false
            isPCP = false
            NotificationCenter.default.post(name: .careTeamMemberRemoved, object: nil, userInfo: ["id": id])
        } catch {
            errorMessage = "Failed to remove from care team: \(error.localizedDescription)"
        }
    }

    private func togglePCP(_ enabled: Bool) async {
        guard let sdk = sdkManager.sdk, let id = result.id else {
            isPCP = !enabled
            return
        }
        errorMessage = nil
        do {
            if isInCareTeam {
                let roles = enabled ? ["PCP"] : []
                let request = BWell.UpdateCareTeamMemberRequest(id: id, type: .Practitioner, role: roles)
                _ = try await sdk.health.updateCareTeamMember(request)
            } else {
                let request = BWell.AddCareTeamMemberRequest(id: id, type: .Practitioner, role: ["PCP"])
                _ = try await sdk.health.addCareTeamMember(request)
                isInCareTeam = true
            }
            NotificationCenter.default.post(name: .careTeamUpdated, object: nil, userInfo: ["id": id, "isPCP": enabled])
        } catch {
            isPCP = !enabled
            errorMessage = "Failed to update PCP status: \(error.localizedDescription)"
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
                institution: institution, provider: providerName, state: reviewState, city: reviewCity
            )
            _ = try await sdk.search.submitProviderForReview(request)
            showSubmitSuccess = true
        } catch {
            errorMessage = "Failed to submit provider for review: \(error.localizedDescription)"
        }
        isLoading = false
    }


    // MARK: - Helpers

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
                Text(name).font(.subheadline).fontWeight(.semibold)
            }
            if let address = location.address {
                if let line = address.line?.joined(separator: "\n"), !line.isEmpty {
                    Text(line).font(.body)
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
                    Image(systemName: "location.fill").font(.caption)
                    Text(String(format: "%.1f miles away", distance)).font(.caption)
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
