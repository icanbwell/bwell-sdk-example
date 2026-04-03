//
//  CareTeamsView.swift
//  bwell-swift-ios
//
//  Displays care team members.
//

import SwiftUI
import BWell

struct CareTeamsView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var careTeams: [BWell.CareTeam] = []
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        ZStack {
            if isLoading && careTeams.isEmpty {
                ProgressView("Loading care teams...")
            } else if let error = errorMessage {
                VStack(spacing: 8) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)
                    Text(error)
                        .foregroundStyle(.red)
                }
            } else if careTeams.isEmpty {
                VStack(spacing: 8) {
                    Image(systemName: "person.3")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)
                    Text("No care teams found.")
                        .foregroundStyle(.secondary)
                }
            } else {
                List(careTeams, id: \.id) { team in
                    VStack(alignment: .leading, spacing: 6) {
                        Text(team.name ?? "Unknown Team")
                            .font(.headline)

                        if let status = team.status {
                            Text(status)
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(.bwellGreen.opacity(0.2))
                                .clipShape(Capsule())
                        }

                        if let participants = team.participant, !participants.isEmpty {
                            ForEach(participants.indices, id: \.self) { index in
                                let participant = participants[index]
                                HStack {
                                    Image(systemName: "person.fill")
                                        .foregroundStyle(.secondary)
                                        .frame(width: 20)
                                    VStack(alignment: .leading) {
                                        Text(participant.member?.display ?? "Unknown")
                                            .font(.subheadline)
                                        if let role = participant.role?.first?.text {
                                            Text(role)
                                                .font(.caption)
                                                .foregroundStyle(.secondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Care Teams")
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .task {
            guard careTeams.isEmpty else { return }
            await loadCareTeams()
        }
    }

    private func loadCareTeams() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        do {
            let request = BWell.CareTeamsRequest(page: 0)
            let response = try await sdk.health.getCareTeams(request)
            careTeams = response.entry?.compactMap { $0.resource } ?? []
        } catch {
            errorMessage = "Failed to load care teams."
        }
        isLoading = false
    }
}
