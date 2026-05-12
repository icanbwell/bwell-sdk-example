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
    @State private var isCareTeamLoading = false
    @State private var errorMessage: String?
    @State private var showSubmitReview = false
    @State private var showConnectSuccess = false
    @State private var showCareTeamSuccess = false
    @State private var careTeamSuccessMessage = ""
    @State private var setPCP = false
    @State private var isInCareTeam = false
    @State private var isCheckingCareTeam = true
    @State private var suppressPCPToggle = true
    @State private var showPCPConfirmation = false
    @State private var pendingPCPValue = false

    @State private var institution: String = ""
    @State private var providerName: String = ""
    @State private var reviewState: String = ""
    @State private var reviewCity: String = ""
    @State private var showSubmitSuccess = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
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

                if result.type != .practice {
                    VStack(spacing: 12) {
                        if isCheckingCareTeam {
                            HStack {
                                ProgressView()
                                    .scaleEffect(0.8)
                                Text("Checking care team status...")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        } else if isInCareTeam {
                            Toggle(isOn: $setPCP) {
                                HStack(spacing: 6) {
                                    Image(systemName: "star.fill")
                                        .font(.caption)
                                        .foregroundStyle(.orange)
                                    Text("Primary Care Provider (PCP)")
                                        .font(.subheadline)
                                }
                            }
                            .tint(.bwellPurple)
                            .onChange(of: setPCP) { _, newValue in
                                guard !suppressPCPToggle else { return }
                                pendingPCPValue = newValue
                                suppressPCPToggle = true
                                setPCP = !newValue
                                suppressPCPToggle = false
                                showPCPConfirmation = true
                            }

                            Button {
                                Task { await removeFromCareTeam() }
                            } label: {
                                HStack {
                                    if isCareTeamLoading {
                                        ProgressView().tint(.red)
                                    } else {
                                        Image(systemName: "person.badge.minus")
                                        Text("Remove from Care Team").fontWeight(.semibold)
                                    }
                                }
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.red.opacity(0.1))
                                .foregroundStyle(.red)
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                            }
                            .disabled(isCareTeamLoading)
                        } else {
                            Toggle(isOn: $setPCP) {
                                HStack(spacing: 6) {
                                    Image(systemName: "star.fill")
                                        .font(.caption)
                                        .foregroundStyle(.orange)
                                    Text("Set as Primary Care Provider (PCP)")
                                        .font(.subheadline)
                                }
                            }
                            .tint(.bwellPurple)

                            Button {
                                Task { await addToCareTeam() }
                            } label: {
                                HStack {
                                    if isCareTeamLoading {
                                        ProgressView().tint(.bwellPurple)
                                    } else {
                                        Image(systemName: "person.badge.plus")
                                        Text("Add to Care Team").fontWeight(.semibold)
                                    }
                                }
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.bwellPurple.opacity(0.1))
                                .foregroundStyle(.bwellPurple)
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                            }
                            .disabled(isCareTeamLoading)
                        }
                    }
                    .padding(.horizontal)
                    Divider()
                }

                if let connectionId = result.endpoint?.first?.name {
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
                    Divider()
                }

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
        .task { await checkCareTeamStatus() }
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
            if let errorMessage { Text(errorMessage) }
        }
        .alert("Success", isPresented: $showSubmitSuccess) {
            Button("OK") { showSubmitReview = false }
        } message: {
            Text("Provider submitted for review successfully.")
        }
        .alert("Success", isPresented: $showCareTeamSuccess) {
            Button("OK") {}
        } message: {
            Text(careTeamSuccessMessage)
        }
        .confirmationDialog(
            pendingPCPValue ? "Set as Primary Care Provider?" : "Remove as Primary Care Provider?",
            isPresented: $showPCPConfirmation,
            titleVisibility: .visible
        ) {
            Button(pendingPCPValue ? "Set as PCP" : "Remove PCP", role: pendingPCPValue ? nil : .destructive) {
                Task { await togglePCP(pendingPCPValue) }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            if pendingPCPValue {
                Text("This will set \(result.content ?? "this provider") as your Primary Care Provider. Any existing PCP will be removed.")
            } else {
                Text("\(result.content ?? "This provider") will remain in your care team but will no longer be your PCP.")
            }
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

    private func checkCareTeamStatus() async {
        guard let sdk = sdkManager.sdk, let id = result.id, result.type != .practice else {
            isCheckingCareTeam = false
            suppressPCPToggle = false
            return
        }
        do {
            let request = BWell.CareTeamsRequest(page: 0)
            let response = try await sdk.health.getCareTeams(request)
            let careTeams = response.entry?.compactMap { $0.resource } ?? []
            for team in careTeams {
                guard let participants = team.participant else { continue }
                for participant in participants {
                    let memberId = extractResourceId(from: participant.member)
                    if memberId == id {
                        isInCareTeam = true
                        let roles = participant.role ?? []
                        let hasPCP = roles.contains { concept in
                            if let text = concept.text, text.localizedCaseInsensitiveContains("pcp") { return true }
                            if let codings = concept.coding {
                                return codings.contains { $0.code?.localizedCaseInsensitiveContains("pcp") == true }
                            }
                            return false
                        }
                        setPCP = hasPCP
                        isCheckingCareTeam = false
                        suppressPCPToggle = false
                        return
                    }
                }
            }
        } catch {
            #if DEBUG
            print("checkCareTeamStatus error: \(error.localizedDescription)")
            #endif
        }
        isCheckingCareTeam = false
        suppressPCPToggle = false
    }

    private func extractResourceId(from member: BWell.CareTeam.Participant.MemberReference?) -> String {
        if let reference = member?.reference {
            let parts = reference.split(separator: "/")
            if parts.count >= 2 { return String(parts.last!) }
            return reference
        }
        return member?.id ?? ""
    }

    private func addToCareTeam() async {
        guard let sdk = sdkManager.sdk, let id = result.id else {
            errorMessage = "Cannot add provider without an ID"
            return
        }
        isCareTeamLoading = true
        errorMessage = nil
        do {
            if setPCP { try await removeExistingPCP(sdk: sdk, excludingId: id) }
            let roles = setPCP ? ["PCP"] : []
            let request = BWell.AddCareTeamMemberRequest(id: id, type: .Practitioner, role: roles)
            _ = try await sdk.health.addCareTeamMember(request)
            #if DEBUG
            print("addCareTeamMember success: id=\(id), pcp=\(setPCP)")
            #endif
            let name = result.content ?? "Provider"
            careTeamSuccessMessage = setPCP
                ? "\(name) has been set as your Primary Care Provider and added to your care team."
                : "\(name) has been added to your care team."
            suppressPCPToggle = true
            isInCareTeam = true
            suppressPCPToggle = false
            showCareTeamSuccess = true
        } catch {
            errorMessage = "Failed to add to care team: \(error.localizedDescription)"
        }
        isCareTeamLoading = false
    }

    private func removeFromCareTeam() async {
        guard let sdk = sdkManager.sdk, let id = result.id else { return }
        isCareTeamLoading = true
        errorMessage = nil
        do {
            let request = BWell.RemoveCareTeamMemberRequest(id: id, type: .Practitioner)
            _ = try await sdk.health.removeCareTeamMember(request)
            #if DEBUG
            print("removeFromCareTeam success: id=\(id)")
            #endif
            isInCareTeam = false
            suppressPCPToggle = true
            setPCP = false
            suppressPCPToggle = false
            careTeamSuccessMessage = "\(result.content ?? "Provider") has been removed from your care team."
            showCareTeamSuccess = true
        } catch {
            errorMessage = "Failed to remove from care team: \(error.localizedDescription)"
        }
        isCareTeamLoading = false
    }

    private func togglePCP(_ enabled: Bool) async {
        guard let sdk = sdkManager.sdk, let id = result.id else { return }
        isCareTeamLoading = true
        errorMessage = nil
        do {
            if enabled { try await removeExistingPCP(sdk: sdk, excludingId: id) }
            let roles = enabled ? ["PCP"] : []
            let request = BWell.UpdateCareTeamMemberRequest(id: id, type: .Practitioner, role: roles)
            _ = try await sdk.health.updateCareTeamMember(request)
            #if DEBUG
            print("togglePCP success: id=\(id), pcp=\(enabled)")
            #endif
            suppressPCPToggle = true
            setPCP = enabled
            suppressPCPToggle = false
            let name = result.content ?? "Provider"
            careTeamSuccessMessage = enabled
                ? "\(name) has been set as your Primary Care Provider."
                : "\(name) has been removed as your Primary Care Provider but remains in your care team."
            showCareTeamSuccess = true
        } catch {
            errorMessage = "Failed to update PCP status: \(error.localizedDescription)"
        }
        isCareTeamLoading = false
    }

    private func removeExistingPCP(sdk: BWellClient, excludingId: String) async throws {
        let teamsRequest = BWell.CareTeamsRequest(page: 0)
        let teamsResponse = try await sdk.health.getCareTeams(teamsRequest)
        let careTeams = teamsResponse.entry?.compactMap { $0.resource } ?? []
        for team in careTeams {
            guard let participants = team.participant else { continue }
            for participant in participants {
                let existingId = extractResourceId(from: participant.member)
                guard existingId != excludingId else { continue }
                let roles = participant.role ?? []
                let hasPCP = roles.contains { concept in
                    if let text = concept.text, text.localizedCaseInsensitiveContains("pcp") { return true }
                    if let codings = concept.coding {
                        return codings.contains { $0.code?.localizedCaseInsensitiveContains("pcp") == true }
                    }
                    return false
                }
                if hasPCP {
                    let removeRequest = BWell.UpdateCareTeamMemberRequest(id: existingId, type: .Practitioner, role: [])
                    _ = try await sdk.health.updateCareTeamMember(removeRequest)
                    #if DEBUG
                    print("Removed PCP from existing member: \(existingId)")
                    #endif
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
                institution: institution, provider: providerName, state: reviewState, city: reviewCity
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
