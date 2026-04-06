//
//  GoalsView.swift
//  bwell-swift-ios
//
//  Displays health goals using sdk.health.getGoals().
//

import SwiftUI
import BWellSDK

struct GoalsView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var goals: [BWell.Goal] = []
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        ZStack {
            if isLoading && goals.isEmpty {
                ProgressView("Loading goals...")
            } else if let error = errorMessage {
                ContentUnavailableView("Error", systemImage: "exclamationmark.triangle", description: Text(error))
            } else if goals.isEmpty {
                ContentUnavailableView("No Goals", systemImage: "target", description: Text("No health goals found."))
            } else {
                List(goals, id: \.id) { goal in
                    VStack(alignment: .leading, spacing: 8) {
                        Text(goal.description?.text ?? goal.description?.coding?.first?.display ?? "Goal")
                            .font(.headline)

                        if let status = goal.lifecycleStatus {
                            Text(status)
                                .font(.caption)
                                .fontWeight(.medium)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(goalStatusColor(status).opacity(0.15))
                                .foregroundStyle(goalStatusColor(status))
                                .clipShape(Capsule())
                        }

                        if let achievement = goal.achievementStatus?.text ?? goal.achievementStatus?.coding?.first?.display {
                            HStack(spacing: 4) {
                                Text("Achievement:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(achievement)
                                    .font(.caption)
                                    .fontWeight(.medium)
                            }
                        }

                        if let category = goal.category?.first?.text ?? goal.category?.first?.coding?.first?.display {
                            HStack(spacing: 4) {
                                Text("Category:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(category)
                                    .font(.caption)
                            }
                        }

                        if let startDate = goal.startDate {
                            Text("Start: \(startDate.dateFormatter())")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }

                        if let targets = goal.target, !targets.isEmpty {
                            ForEach(targets.indices, id: \.self) { index in
                                let target = targets[index]
                                if let measure = target.measure?.text ?? target.measure?.coding?.first?.display {
                                    HStack(spacing: 4) {
                                        Text("Target:")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                        Text(measure)
                                            .font(.caption)
                                    }
                                }
                            }
                        }

                        if let priority = goal.priority?.text ?? goal.priority?.coding?.first?.display {
                            HStack(spacing: 4) {
                                Text("Priority:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(priority)
                                    .font(.caption)
                                    .fontWeight(.medium)
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Goals")
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable {
            await loadGoals()
        }
        .task {
            guard goals.isEmpty else { return }
            await loadGoals()
        }
    }

    private func loadGoals() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        errorMessage = nil
        do {
            let request = BWell.GoalRequest(page: 0)
            let response = try await sdk.health.getGoals(request)
            goals = response.entry?.compactMap { $0.resource } ?? []
        } catch {
            errorMessage = "Failed to load goals."
        }
        isLoading = false
    }

    private func goalStatusColor(_ status: String) -> Color {
        switch status.lowercased() {
        case "active": return .green
        case "completed": return .blue
        case "cancelled": return .red
        case "on-hold": return .orange
        default: return .gray
        }
    }
}
