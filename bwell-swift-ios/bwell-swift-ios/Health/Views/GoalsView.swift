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
    @State private var expandedIds: Set<String> = []

    var body: some View {
        ZStack {
            if isLoading && goals.isEmpty {
                ProgressView("Loading goals...")
            } else if let error = errorMessage {
                ContentUnavailableView {
                    Label("Error", systemImage: "exclamationmark.triangle")
                } description: {
                    Text(error)
                } actions: {
                    Button("Retry") { Task { await loadGoals() } }
                }
            } else if goals.isEmpty {
                ContentUnavailableView("No Goals", systemImage: "target", description: Text("No health goals found."))
            } else {
                List(goals, id: \.id) { goal in
                    goalRow(goal)
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

    @ViewBuilder
    private func goalRow(_ goal: BWell.Goal) -> some View {
        let id = goal.id ?? UUID().uuidString
        let isExpanded = expandedIds.contains(id)

        VStack(alignment: .leading, spacing: 0) {
            Button(action: {
                withAnimation(.easeInOut(duration: 0.25)) {
                    if expandedIds.contains(id) { expandedIds.remove(id) }
                    else { expandedIds.insert(id) }
                }
            }) {
                HStack(alignment: .center, spacing: 10) {
                    Image(systemName: "target")
                        .foregroundStyle(FHIRGoalStatus(rawStatus: goal.lifecycleStatus)?.color ?? .gray)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 3) {
                        Text(goal.description?.text ?? goal.description?.coding?.first?.display ?? "Goal")
                            .font(.subheadline)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .fixedSize(horizontal: false, vertical: true)

                        if let priority = goal.priority?.text ?? goal.priority?.coding?.first?.display {
                            Text("Priority: \(priority)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }

                    Spacer()

                    if let status = goal.lifecycleStatus {
                        let color = FHIRGoalStatus(rawStatus: status)?.color ?? .gray
                        Text(status.capitalized)
                            .font(.caption2)
                            .fontWeight(.medium)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(color.opacity(0.15))
                            .foregroundStyle(color)
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
                goalDetail(goal)
                    .padding(.top, 6)
                    .padding(.leading, 38)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 6)
    }

    @ViewBuilder
    private func goalDetail(_ goal: BWell.Goal) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            if let achievement = goal.achievementStatus?.text ?? goal.achievementStatus?.coding?.first?.display {
                detailRow("Achievement", achievement)
            }
            if let category = goal.category?.first?.text ?? goal.category?.first?.coding?.first?.display {
                detailRow("Category", category)
            }
            if let startDate = goal.startDate {
                detailRow("Start", startDate.dateFormatter())
            }

            if let targets = goal.target, !targets.isEmpty {
                Divider().padding(.vertical, 4)
                Text("Targets")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundStyle(.secondary)
                ForEach(targets.indices, id: \.self) { index in
                    let target = targets[index]
                    if let measure = target.measure?.text ?? target.measure?.coding?.first?.display {
                        HStack(alignment: .top, spacing: 6) {
                            Text("•")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(measure)
                                    .font(.caption)
                                    .foregroundStyle(.primary)
                                if let due = target.dueDate {
                                    Text("Due: \(due.dateFormatter())")
                                        .font(.caption2)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func detailRow(_ label: String, _ value: String) -> some View {
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

}
