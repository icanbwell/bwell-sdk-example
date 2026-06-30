//
//  ConditionsView.swift
//  bwell-swift-ios
//
//  Conditions organized by clinical status (Active/Resolved/Other) with expandable detail.
//  Shows condition name, category, clinical status badge, onset date,
//  severity, body sites, notes, and coding information.
//

import SwiftUI
import BWellSDK

struct ConditionsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var conditions: [BWell.Condition] = []
    @State private var isLoading = false
    @State private var hasFetched = false
    @State private var expandedIds: Set<String> = []
    @State private var errorMessage: String?

    private var activeConditions: [BWell.Condition] {
        conditions.filter { clinicalCode($0) == "active" || clinicalCode($0) == "recurrence" || clinicalCode($0) == "relapse" }
    }

    private var resolvedConditions: [BWell.Condition] {
        conditions.filter { clinicalCode($0) == "resolved" || clinicalCode($0) == "remission" || clinicalCode($0) == "inactive" }
    }

    private var otherConditions: [BWell.Condition] {
        conditions.filter {
            let code = clinicalCode($0)
            return code != "active" && code != "recurrence" && code != "relapse"
                && code != "resolved" && code != "remission" && code != "inactive"
        }
    }

    var body: some View {
        List {
            if isLoading && conditions.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !isLoading && hasFetched && errorMessage != nil {
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundStyle(.orange)
                    Text("Unable to Load Conditions")
                        .font(.headline)
                    Text(errorMessage ?? "An unknown error occurred.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                    Button("Retry") {
                        Task { await fetchAll() }
                    }
                    .buttonStyle(.bordered)
                }
                .frame(maxWidth: .infinity)
                .padding(.top, 40)
                .listRowSeparator(.hidden)
            } else if !isLoading && conditions.isEmpty && hasFetched {
                ContentUnavailableView("No Conditions",
                    systemImage: "heart.text.square",
                    description: Text("No condition records found."))
            } else {
                conditionSection("Active", conditions: activeConditions, dotColor: .red)
                conditionSection("Resolved", conditions: resolvedConditions, dotColor: .green)
                conditionSection("Other", conditions: otherConditions, dotColor: .gray)
            }
        }
        .listStyle(.plain)
        .navigationTitle("Conditions")
        .toolbarColorScheme(.dark, for: .navigationBar)

        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable { await fetchAll() }
        .task {
            if !hasFetched { await fetchAll() }
        }
    }

    @ViewBuilder
    private func conditionSection(_ title: String, conditions: [BWell.Condition], dotColor: Color) -> some View {
        if !conditions.isEmpty {
            Section(header: HStack(spacing: 6) {
                Image(systemName: "circle.fill")
                    .font(.system(size: 8))
                    .foregroundStyle(dotColor)
                Text("\(title) (\(conditions.count))")
                    .font(.subheadline)
                    .fontWeight(.semibold)
            }) {
                ForEach(conditions) { condition in
                    ConditionRow(
                        condition: condition,
                        isExpanded: expandedIds.contains(condition.id),
                        onToggle: { toggleExpanded(condition.id) }
                    )
                }
            }
        }
    }

    private func toggleExpanded(_ id: String) {
        withAnimation(.easeInOut(duration: 0.25)) {
            if expandedIds.contains(id) {
                expandedIds.remove(id)
            } else {
                expandedIds.insert(id)
            }
        }
    }

    private func clinicalCode(_ c: BWell.Condition) -> String {
        (c.clinicalStatus?.coding?.first?.code ?? c.clinicalStatus?.text ?? "").lowercased()
    }

    private func fetchAll() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        errorMessage = nil
        do {
            let request = BWell.HealthDataRequest(page: 0, pageSize: 100)
            let response = try await sdk.health.getConditions(request)
            let all = response.entry?.compactMap { $0.resource } ?? []
            // Sort: active first, then by onset date descending
            conditions = all.sorted {
                let active0 = isActive($0) ? 0 : 1
                let active1 = isActive($1) ? 0 : 1
                if active0 != active1 { return active0 < active1 }
                return ($0.onsetDateTime ?? $0.recordedDate ?? "") > ($1.onsetDateTime ?? $1.recordedDate ?? "")
            }
        } catch {
            let mirror = Mirror(reflecting: error)
            NSLog("[Conditions] Error type: %@", String(describing: type(of: error)))
            NSLog("[Conditions] Error: %@", String(describing: error))
            for child in mirror.children {
                NSLog("[Conditions] Error child: %@ = %@", child.label ?? "?", String(describing: child.value))
            }
            errorMessage = String(describing: error)
            conditions = []
        }
        isLoading = false
        hasFetched = true
    }

    private func isActive(_ c: BWell.Condition) -> Bool {
        let code = clinicalCode(c)
        return code == "active" || code == "recurrence" || code == "relapse"
    }
}

// MARK: - Condition Row

private struct ConditionRow: View {
    let condition: BWell.Condition
    let isExpanded: Bool
    let onToggle: () -> Void

    private var conditionName: String {
        condition.code?.coding?.first?.display
            ?? condition.code?.text
            ?? "Condition"
    }

    private var categoryText: String? {
        condition.category?.compactMap { $0.coding?.first?.display ?? $0.text }.joined(separator: ", ")
    }

    private var clinicalStatusText: String {
        condition.clinicalStatus?.coding?.first?.code?.capitalizingFirstLetter()
            ?? condition.clinicalStatus?.text?.capitalizingFirstLetter()
            ?? "Unknown"
    }

    private var onsetText: String? {
        condition.onsetDateTime?.dateFormatter()
            ?? condition.onsetPeriod?.start?.dateFormatter()
    }

    private var severityText: String? {
        condition.severity?.coding?.first?.display ?? condition.severity?.text
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: onToggle) {
                HStack(alignment: .center, spacing: 10) {
                    Image(systemName: statusIcon)
                        .font(.title3)
                        .foregroundStyle(statusColor)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 3) {
                        Text(conditionName)
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .lineLimit(2)
                            .multilineTextAlignment(.leading)

                        if let category = categoryText, !category.isEmpty {
                            Text(category)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .lineLimit(1)
                        }
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 3) {
                        Text(clinicalStatusText)
                            .font(.caption2)
                            .fontWeight(.medium)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(statusColor.opacity(0.12))
                            .foregroundStyle(statusColor)
                            .clipShape(Capsule())

                        if let onset = onsetText {
                            Text(onset)
                                .font(.caption2)
                                .foregroundStyle(.tertiary)
                        }
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
                ConditionDetailContent(condition: condition)
                    .padding(.top, 6)
                    .padding(.leading, 38)
                    .transition(.opacity)
            }
        }
        .padding(.vertical, 6)
    }

    private var statusIcon: String {
        let code = (condition.clinicalStatus?.coding?.first?.code ?? "").lowercased()
        switch code {
        case "active", "recurrence", "relapse": return "exclamationmark.circle.fill"
        case "resolved", "remission": return "checkmark.circle.fill"
        case "inactive": return "minus.circle.fill"
        default: return "questionmark.circle.fill"
        }
    }

    private var statusColor: Color {
        let code = (condition.clinicalStatus?.coding?.first?.code ?? "").lowercased()
        switch code {
        case "active", "recurrence", "relapse": return .red
        case "resolved", "remission": return .green
        case "inactive": return .orange
        default: return .gray
        }
    }
}

// MARK: - Condition Detail (Expanded)

private struct ConditionDetailContent: View {
    let condition: BWell.Condition

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            detailRow("Clinical", condition.clinicalStatus?.coding?.first?.code?.capitalizingFirstLetter()
                       ?? condition.clinicalStatus?.text)

            if let verification = condition.verificationStatus {
                detailRow("Verification", verification.coding?.first?.code?.capitalizingFirstLetter()
                           ?? verification.text)
            }

            if let severity = condition.severity {
                detailRow("Severity", severity.coding?.first?.display ?? severity.text)
            }

            if let categories = condition.category, !categories.isEmpty {
                let cats = categories.compactMap { $0.coding?.first?.display ?? $0.text }
                if !cats.isEmpty {
                    detailRow("Category", cats.joined(separator: ", "))
                }
            }

            detailRow("Onset", condition.onsetDateTime?.dateFormatter()
                       ?? condition.onsetPeriod?.start?.dateFormatter())

            if let onsetEnd = condition.onsetPeriod?.end {
                detailRow("Onset End", onsetEnd.dateFormatter())
            }

            detailRow("Recorded", condition.recordedDate?.dateFormatter())

            if let abatement = condition.abatementDateTime ?? condition.abatementPeriod?.start {
                detailRow("Abatement", abatement.dateFormatter())
            }

            // Recorder
            if let recorder = condition.recorder {
                if let display = recorder.display, !display.isEmpty {
                    detailRow("Recorder", display)
                } else if case .practitioner(let doc) = recorder.resource {
                    if let name = doc.name?.first {
                        let given = name.given?.joined(separator: " ") ?? ""
                        let family = name.family ?? ""
                        let parts = [given, family].filter { !$0.isEmpty }
                        if !parts.isEmpty {
                            detailRow("Recorder", parts.joined(separator: " "))
                        }
                    }
                }
            }

            // Asserter
            if let asserter = condition.asserter {
                if let display = asserter.display, !display.isEmpty {
                    detailRow("Asserter", display)
                } else if case .practitioner(let doc) = asserter.resource {
                    if let name = doc.name?.first {
                        let given = name.given?.joined(separator: " ") ?? ""
                        let family = name.family ?? ""
                        let parts = [given, family].filter { !$0.isEmpty }
                        if !parts.isEmpty {
                            detailRow("Asserter", parts.joined(separator: " "))
                        }
                    }
                }
            }

            // Body sites
            if let bodySites = condition.bodySite, !bodySites.isEmpty {
                let siteTexts = bodySites.compactMap { $0.coding?.first?.display ?? $0.text }
                if !siteTexts.isEmpty {
                    detailRow("Body Site", siteTexts.joined(separator: "; "))
                }
            }

            // Notes
            if let notes = condition.note, !notes.isEmpty {
                ForEach(notes.indices, id: \.self) { i in
                    detailRow("Note", notes[i].text)
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

// MARK: - Legacy Sheet View

struct ConditionSheetView: View {
    var condition: BWellWrapper.condition

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(condition.code?.coding?.first?.display ?? condition.code?.text ?? "Condition")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

            DetailedItemView(title: "Onset date: ", content: condition.onsetDateTime?.dateFormatter())
            DetailedItemView(title: "Recorded date: ", content: condition.recordedDate?.dateFormatter())
                .padding(.bottom, 10)

            DetailedItemView(title: "Clinical status: ", content: condition.clinicalStatus?.text)
            DetailedItemView(title: "Category: ", content: condition.category?.first?.coding?.first?.display)
            DetailedItemView(title: "Verification status: ", content: condition.verificationStatus?.text ?? condition.verificationStatus?.coding?.first?.display)
            DetailedItemView(title: "Severity: ", content: condition.severity?.text ?? condition.severity?.coding?.first?.display)
                .padding(.bottom, 10)

            if let bodySite = condition.bodySite {
                Text("Body site:")
                    .fontWeight(.semibold)

                ForEach(bodySite, id: \.id) { site in
                    Text(site.coding?.first?.display ?? "")
                }.padding(.bottom, 10)
            }

            if let notes = condition.note {
                Text("Notes:")
                    .font(.title2)
                    .fontWeight(.semibold)
                ForEach(notes, id: \.id) { note in
                    HStack {
                        Text(note.text ?? "Note not available")
                        Spacer()
                        Text(note.time?.dateFormatter() ?? "")
                    }
                }
            }
            Spacer()
        }
        .padding()
        .presentationDragIndicator(.visible)
        .presentationDetents([.medium])
    }
}
