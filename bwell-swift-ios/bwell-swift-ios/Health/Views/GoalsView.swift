//
//  GoalsView.swift
//  bwell-swift-ios
//
//  Demonstrates the HealthDataManager.getGoals() operation (EA-2153).
//

import SwiftUI
import BWellSDK

struct GoalsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel

    var body: some View {
        List {
            if viewModel.goals.isEmpty && !viewModel.isLoading {
                VStack(spacing: 16) {
                    Image(systemName: "target")
                        .font(.system(size: 48))
                        .foregroundColor(.secondary)
                    Text("No Goals")
                        .font(.title2)
                        .fontWeight(.semibold)
                    Text("No health goals found")
                        .font(.body)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 40)
                .listRowSeparator(.hidden)
            } else {
                ForEach(viewModel.goals, id: \.id) { goal in
                    GoalRowView(goal: goal)
                }
            }

            if let error = viewModel.errorMessage {
                Section {
                    Text(error)
                        .foregroundColor(.red)
                        .font(.caption)
                }
            }
        }
        .navigationTitle("Goals")
        .listStyle(.plain)
        .overlay {
            if viewModel.isLoading {
                ProgressView()
            }
        }
        .task {
            if viewModel.goals.isEmpty {
                await viewModel.getGoals()
            }
        }
    }
}

// MARK: - Goal Row
private struct GoalRowView: View {
    let goal: BWell.Goal

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(goal.description?.text ?? "Unknown Goal")
                .font(.headline)

            if let status = goal.lifecycleStatus {
                HStack(spacing: 4) {
                    Circle()
                        .fill(statusColor(status))
                        .frame(width: 8, height: 8)
                    Text("Status: \(status)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }

            if let achievement = goal.achievementStatus?.text {
                Text("Achievement: \(achievement)")
                    .font(.caption)
                    .foregroundColor(.accentColor)
            }

            if let category = goal.category?.first?.text {
                Text("Category: \(category)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            if let startDate = goal.startDate {
                Text("Start: \(startDate)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            if let statusReason = goal.statusReason {
                Text("Reason: \(statusReason)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 4)
    }

    private func statusColor(_ status: String) -> Color {
        switch status.lowercased() {
        case "active": return .green
        case "completed": return .blue
        case "cancelled": return .red
        case "on-hold": return .orange
        default: return .gray
        }
    }
}
