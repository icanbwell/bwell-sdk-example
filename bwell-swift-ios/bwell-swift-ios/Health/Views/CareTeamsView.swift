//
//  CareTeamsView.swift
//  bwell-swift-ios
//
//  Displays care team members with expandable detail rows.
//

import SwiftUI
import BWellSDK

struct CareTeamsView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var careTeams: [BWell.CareTeam] = []
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var expandedIds: Set<String> = []

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
                    CareTeamRow(
                        team: team,
                        isExpanded: expandedIds.contains(team.id ?? ""),
                        onToggle: {
                            withAnimation(.easeInOut(duration: 0.25)) {
                                let id = team.id ?? ""
                                if expandedIds.contains(id) {
                                    expandedIds.remove(id)
                                } else {
                                    expandedIds.insert(id)
                                }
                            }
                        }
                    )
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Care Teams")
        .toolbarColorScheme(.dark, for: .navigationBar)

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

// MARK: - Care Team Row

private struct CareTeamRow: View {
    let team: BWell.CareTeam
    let isExpanded: Bool
    let onToggle: () -> Void

    private var participantCount: Int {
        team.participant?.count ?? 0
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: onToggle) {
                HStack(alignment: .center, spacing: 10) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text(team.name ?? "Unknown Team")
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .lineLimit(2)
                            .multilineTextAlignment(.leading)

                        if participantCount > 0 {
                            Text("\(participantCount) member\(participantCount == 1 ? "" : "s")")
                                .font(.caption2)
                                .foregroundStyle(.tertiary)
                        }
                    }

                    Spacer()

                    if let status = team.status {
                        Text(status)
                            .font(.caption2)
                            .fontWeight(.medium)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(.bwellGreen.opacity(0.15))
                            .foregroundStyle(.bwellGreen)
                            .clipShape(Capsule())
                    }

                    Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                        .font(.caption2)
                        .fontWeight(.semibold)
                        .foregroundStyle(.tertiary)
                }
            }
            .buttonStyle(.plain)

            if isExpanded {
                Divider().padding(.top, 8)
                CareTeamDetail(team: team)
                    .padding(.top, 6)
                    .padding(.leading, 20)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 6)
    }
}

// MARK: - Care Team Detail (Expanded)

private struct CareTeamDetail: View {
    let team: BWell.CareTeam

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            if let categories = team.category, !categories.isEmpty {
                let categoryText = categories.compactMap { $0.text ?? $0.coding?.first?.display }.joined(separator: ", ")
                if !categoryText.isEmpty {
                    detailRow("Category", categoryText)
                }
            }

            // Participants
            if let participants = team.participant, !participants.isEmpty {
                Divider().padding(.vertical, 4)
                Text("Participants")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundStyle(.secondary)

                ForEach(Array(participants.enumerated()), id: \.offset) { _, participant in
                    VStack(alignment: .leading, spacing: 2) {
                        HStack(alignment: .top, spacing: 6) {
                            Image(systemName: "person.fill")
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                                .frame(width: 14)
                                .padding(.top, 1)
                            VStack(alignment: .leading, spacing: 1) {
                                Text(participant.member?.display ?? "Unknown")
                                    .font(.caption)
                                    .foregroundStyle(.primary)
                                if let roles = participant.role, !roles.isEmpty {
                                    let roleText = roles.compactMap { $0.text ?? $0.coding?.first?.display }.joined(separator: ", ")
                                    if !roleText.isEmpty {
                                        Text(roleText)
                                            .font(.caption2)
                                            .foregroundStyle(.secondary)
                                    }
                                }
                            }
                        }
                    }
                    .padding(.vertical, 2)
                }
            }

        }
    }

    @ViewBuilder
    private func detailRow(_ label: String, _ value: String?) -> some View {
        if let value, !value.isEmpty {
            HStack(alignment: .top, spacing: 6) {
                Text(label)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .frame(width: 80, alignment: .trailing)
                Text(value)
                    .font(.caption)
                    .foregroundStyle(.primary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }
}
