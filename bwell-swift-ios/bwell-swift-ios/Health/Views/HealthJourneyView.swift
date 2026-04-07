//
//  HealthJourneyView.swift
//  bwell-swift-ios
//
//  Displays tasks from sdk.activity.getTasks() with To Do / Completed segmented control.
//  Mirrors the b.well MFE ActivitySection pattern: identifier-based titles,
//  extension-based content (when enriched), collapsible detail cards.
//

import SwiftUI
import BWellSDK

struct HealthJourneyView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var tasks: [BWell.Task] = []
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var selectedFilter: TaskFilter = .toDo
    @State private var expandedTaskIds: Set<String> = []

    enum TaskFilter: String, CaseIterable {
        case toDo = "To Do"
        case completed = "Completed"
    }

    // FHIR Task terminal statuses — not actionable
    private static let terminalStatuses: Set<String> = [
        "completed", "cancelled", "rejected", "entered-in-error", "failed"
    ]

    private var filteredTasks: [BWell.Task] {
        switch selectedFilter {
        case .toDo:
            return tasks.filter { task in
                guard let status = task.status?.lowercased() else { return true }
                return !Self.terminalStatuses.contains(status)
            }
        case .completed:
            return tasks.filter { task in
                guard let status = task.status?.lowercased() else { return false }
                return Self.terminalStatuses.contains(status)
            }
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            Picker("Filter", selection: $selectedFilter) {
                ForEach(TaskFilter.allCases, id: \.self) { filter in
                    Text(filter.rawValue).tag(filter)
                }
            }
            .pickerStyle(.segmented)
            .padding()

            if isLoading && tasks.isEmpty {
                ProgressView("Loading tasks...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = errorMessage {
                ContentUnavailableView("Error", systemImage: "exclamationmark.triangle", description: Text(error))
            } else if filteredTasks.isEmpty {
                ContentUnavailableView(
                    selectedFilter == .toDo ? "No Pending Tasks" : "No Completed Tasks",
                    systemImage: selectedFilter == .toDo ? "checklist" : "checkmark.circle",
                    description: Text(selectedFilter == .toDo
                        ? "You're all caught up!"
                        : "No completed tasks yet."))
            } else {
                List {
                    if filteredTasks.count > 1 {
                        HStack {
                            Text("\(filteredTasks.count) tasks")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                            Spacer()
                            Button(expandedTaskIds.count == filteredTasks.count ? "Collapse All" : "Expand All") {
                                withAnimation(.easeInOut(duration: 0.25)) {
                                    if expandedTaskIds.count == filteredTasks.count {
                                        expandedTaskIds.removeAll()
                                    } else {
                                        expandedTaskIds = Set(filteredTasks.compactMap { $0.id })
                                    }
                                }
                            }
                            .font(.subheadline)
                            .fontWeight(.medium)
                        }
                        .listRowSeparator(.hidden)
                    }

                    ForEach(filteredTasks, id: \.id) { task in
                        let taskId = task.id ?? ""
                        let isExpanded = expandedTaskIds.contains(taskId)

                        TaskCardView(
                            task: task,
                            isExpanded: isExpanded,
                            onToggle: {
                                withAnimation(.easeInOut(duration: 0.25)) {
                                    if isExpanded {
                                        expandedTaskIds.remove(taskId)
                                    } else {
                                        expandedTaskIds.insert(taskId)
                                    }
                                }
                            },
                            onMarkComplete: {
                                await markTaskComplete(task)
                            }
                        )
                        .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                        .listRowSeparator(.hidden)
                    }
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Health Journey")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable {
            expandedTaskIds.removeAll()
            await loadTasks()
        }
        .task {
            guard tasks.isEmpty else { return }
            await loadTasks()
        }
    }

    private func loadTasks() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        errorMessage = nil
        do {
            let request = BWell.TasksRequest(page: 0)
            let response = try await sdk.activity.getTasks(request)
            tasks = response.entry?.compactMap { $0.resource } ?? []
        } catch {
            NSLog("[HealthJourney] getTasks error: %@", String(describing: error))
            errorMessage = "Failed to load tasks."
        }
        isLoading = false
    }

    private func markTaskComplete(_ task: BWell.Task) async {
        guard let sdk = sdkManager.sdk, let taskId = task.id else { return }
        do {
            let request = BWell.UpdateTaskStatusRequest(id: taskId, status: "completed")
            _ = try await sdk.activity.updateTaskStatus(request)
            await loadTasks()
        } catch {
            errorMessage = "Failed to update task status."
        }
    }
}

// MARK: - Task Title Extraction (matches MFE identifier-based title pattern)

private func taskTitle(for task: BWell.Task) -> String {
    // 1. Identifier with activityTitle system
    if let idents = task.identifier {
        for ident in idents {
            if let sys = ident.system, sys.contains("activityTitle"), let val = ident.value, !val.isEmpty {
                return val
            }
        }
    }

    // 3. Code display/text
    if let t = task.code?.text, !t.isEmpty { return t }
    if let d = task.code?.coding?.first?.display, !d.isEmpty { return d }

    // 4. Description
    if let d = task.description, !d.isEmpty { return d }

    // 5. Business status as context
    if let bs = task.businessStatus?.text ?? task.businessStatus?.coding?.first?.display, !bs.isEmpty {
        return bs
    }

    // 6. Focus reference type
    if let ft = task.focus?.type, !ft.isEmpty {
        return ft
    }

    // 7. Truncated ID
    if let id = task.id {
        return "Task \(String(id.prefix(8)))..."
    }

    return "Task"
}

private func taskSubtitle(for task: BWell.Task) -> String? {
    // Identifier with activityName system
    if let idents = task.identifier {
        for ident in idents {
            if let sys = ident.system, sys.contains("activityName"), let val = ident.value, !val.isEmpty {
                return val
            }
        }
    }

    // Description if title came from elsewhere
    if let desc = task.description, !desc.isEmpty,
       task.code?.text != nil || task.code?.coding?.first?.display != nil {
        return desc
    }

    // Focus reference
    if let ref = task.focus?.reference, !ref.isEmpty {
        return ref
    }

    return nil
}

/// Content icon URL not available without hp-facade enrichment
private func taskIconURL(for task: BWell.Task) -> String? {
    nil
}

// MARK: - Task Card (collapsible, matches app pattern)

private struct TaskCardView: View {
    typealias FHIRTask = BWell.Task
    let task: FHIRTask
    let isExpanded: Bool
    let onToggle: () -> Void
    let onMarkComplete: () async -> Void
    @State private var showConfirmation = false

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Summary row
            Button(action: onToggle) {
                HStack(alignment: .center, spacing: 12) {
                    // Status icon
                    Image(systemName: statusIcon)
                        .font(.title3)
                        .foregroundStyle(statusColor)
                        .frame(width: 32)

                    // Title and subtitle
                    VStack(alignment: .leading, spacing: 4) {
                        Text(taskTitle(for: task))
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .lineLimit(2)
                            .multilineTextAlignment(.leading)

                        if let subtitle = taskSubtitle(for: task) {
                            Text(subtitle)
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                                .lineLimit(1)
                        }

                        // Status + date row
                        HStack(spacing: 8) {
                            if let status = task.status {
                                Text(status.capitalized)
                                    .font(.caption)
                                    .fontWeight(.medium)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 2)
                                    .background(statusColor.opacity(0.12))
                                    .foregroundStyle(statusColor)
                                    .clipShape(Capsule())
                            }

                            if let priority = task.priority {
                                Text(priority.capitalized)
                                    .font(.caption)
                                    .foregroundStyle(priorityColor(priority))
                            }
                        }
                    }

                    Spacer()

                    if let dateStr = dateString {
                        Text(dateStr)
                            .font(.caption)
                            .foregroundStyle(.tertiary)
                    }

                    Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                        .font(.caption2)
                        .fontWeight(.semibold)
                        .foregroundStyle(.tertiary)
                }
            }
            .buttonStyle(.plain)

            // Expanded detail
            if isExpanded {
                Divider()
                    .padding(.top, 10)

                VStack(alignment: .leading, spacing: 6) {
                    if let desc = task.description, !desc.isEmpty {
                        DetailRow(label: "Description:", value: desc)
                    }
                    if let codeText = task.code?.text ?? task.code?.coding?.first?.display {
                        DetailRow(label: "Code:", value: codeText)
                    }
                    if let codeVal = task.code?.coding?.first?.code {
                        DetailRow(label: "Code Value:", value: codeVal)
                    }
                    if let bs = task.businessStatus?.text ?? task.businessStatus?.coding?.first?.display {
                        DetailRow(label: "Business Status:", value: bs)
                    }
                    if let period = task.executionPeriod {
                        if let start = period.start {
                            DetailRow(label: "Start:", value: start.dateFormatter())
                        }
                        if let end = period.end {
                            DetailRow(label: "End:", value: end.dateFormatter())
                        }
                    }
                    if let lastMod = task.lastModified {
                        DetailRow(label: "Last Modified:", value: lastMod.dateFormatter())
                    }
                    if let focus = task.focus {
                        if let ref = focus.reference {
                            DetailRow(label: "Focus:", value: ref)
                        }
                        if let type = focus.type {
                            DetailRow(label: "Focus Type:", value: type)
                        }
                    }
                    if let taskFor = task.taskFor?.reference {
                        DetailRow(label: "For:", value: taskFor)
                    }
                    if let basedOn = task.basedOn, !basedOn.isEmpty {
                        ForEach(basedOn.indices, id: \.self) { i in
                            if let r = basedOn[i].reference {
                                DetailRow(label: "Based On:", value: r)
                            }
                        }
                    }
                    if let canonical = task.instantiatesCanonical, !canonical.isEmpty {
                        DetailRow(label: "Protocol:", value: canonical)
                    }
                    if let identifiers = task.identifier, !identifiers.isEmpty {
                        ForEach(identifiers.indices, id: \.self) { i in
                            let ident = identifiers[i]
                            if let value = ident.value {
                                let sys = ident.system?.components(separatedBy: "/").last ?? "Identifier"
                                DetailRow(label: "\(sys):", value: value)
                            }
                        }
                    }

                    // Outputs
                    if let outputs = task.output, !outputs.isEmpty {
                        Text("Outputs")
                            .font(.caption)
                            .fontWeight(.semibold)
                            .foregroundStyle(.secondary)
                            .padding(.top, 4)
                        ForEach(outputs.indices, id: \.self) { i in
                            let output = outputs[i]
                            let typeName = output.type?.text ?? output.type?.coding?.first?.display ?? "Output"
                            let value = output.valueString
                                ?? output.valueCodeableConcept?.text
                                ?? output.valueCodeableConcept?.coding?.first?.display
                                ?? "—"
                            DetailRow(label: "\(typeName):", value: value)
                        }
                    }

                    // Mark Complete (only for actionable states, inside detail)
                    if isActionable {
                        Button {
                            showConfirmation = true
                        } label: {
                            HStack(spacing: 4) {
                                Image(systemName: "checkmark.circle")
                                Text("Mark Complete")
                            }
                            .font(.caption)
                            .fontWeight(.medium)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(.bwellPurple.opacity(0.1))
                            .foregroundStyle(.bwellPurple)
                            .clipShape(Capsule())
                        }
                        .buttonStyle(.plain)
                        .padding(.top, 4)
                        .confirmationDialog("Complete Task", isPresented: $showConfirmation) {
                            Button("Mark as Complete") {
                                Swift.Task { await onMarkComplete() }
                            }
                            Button("Cancel", role: .cancel) {}
                        } message: {
                            Text("Mark this task as completed?")
                        }
                    }
                }
                .padding(.top, 8)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 4)
        .padding(.horizontal, 4)
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(isExpanded ? Color(.systemGray6).opacity(0.5) : Color.clear)
        )
    }

    private var statusIcon: String {
        guard let status = task.status?.lowercased() else { return "circle" }
        switch status {
        case "completed": return "checkmark.circle.fill"
        case "in-progress": return "arrow.triangle.2.circlepath"
        case "ready": return "clock.fill"
        case "requested": return "paperplane.fill"
        case "on-hold": return "pause.circle.fill"
        case "cancelled", "failed": return "xmark.circle.fill"
        default: return "circle"
        }
    }

    private var statusColor: Color {
        guard let status = task.status?.lowercased() else { return .gray }
        switch status {
        case "completed": return .green
        case "in-progress": return .blue
        case "ready": return .orange
        case "requested": return .purple
        case "on-hold": return .yellow
        case "cancelled", "failed", "rejected", "entered-in-error": return .red
        default: return .gray
        }
    }

    private var dateString: String? {
        if let start = task.executionPeriod?.start { return start.relativeDate() }
        if let lastMod = task.lastModified { return lastMod.relativeDate() }
        return nil
    }

    private var isActionable: Bool {
        guard let status = task.status?.lowercased() else { return false }
        return ["ready", "in-progress", "requested", "received", "accepted"].contains(status)
    }

    private func priorityColor(_ priority: String?) -> Color {
        switch priority?.lowercased() {
        case "urgent", "asap", "stat": return .red
        case "routine": return .blue
        default: return .gray
        }
    }
}

// MARK: - Detail Row (reused from HealthDataViewFactory pattern)

private struct DetailRow: View {
    let label: String
    let value: String

    var body: some View {
        if !value.isEmpty {
            HStack(alignment: .top, spacing: 4) {
                Text(label)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .frame(width: 100, alignment: .trailing)
                Text(value)
                    .font(.caption)
                    .foregroundStyle(.primary)
                    .textSelection(.enabled)
                Spacer()
            }
        }
    }
}
