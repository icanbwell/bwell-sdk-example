//
//  ProceduresView.swift
//  bwell-swift-ios
//
//  Procedures organized chronologically by date with expandable detail.
//  Each row shows procedure name, body site, status badge, and date.
//  Expanded detail shows performer, reason, outcome, notes, and coding.
//

import SwiftUI
import BWell

struct ProceduresView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var procedures: [BWell.Procedure] = []
    @State private var isLoading = false
    @State private var hasFetched = false
    @State private var expandedIds: Set<String> = []

    private var groupedByDate: [(String, [BWell.Procedure])] {
        let grouped = Dictionary(grouping: procedures) { proc -> String in
            if let dt = proc.performedDateTime {
                // Group by month/year
                if let date = parseFHIRDate(dt) {
                    let formatter = DateFormatter()
                    formatter.dateFormat = "MMMM yyyy"
                    return formatter.string(from: date)
                }
            }
            if let start = proc.performedPeriod?.start {
                if let date = parseFHIRDate(start) {
                    let formatter = DateFormatter()
                    formatter.dateFormat = "MMMM yyyy"
                    return formatter.string(from: date)
                }
            }
            return "Unknown Date"
        }
        return grouped.sorted { pair1, pair2 in
            let date1 = pair1.1.first?.performedDateTime ?? pair1.1.first?.performedPeriod?.start ?? ""
            let date2 = pair2.1.first?.performedDateTime ?? pair2.1.first?.performedPeriod?.start ?? ""
            return date1 > date2
        }
    }

    var body: some View {
        List {
            if isLoading && procedures.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !isLoading && procedures.isEmpty && hasFetched {
                ContentUnavailableView("No Procedures",
                    systemImage: "cross.case",
                    description: Text("No procedure records found."))
            } else {
                ForEach(groupedByDate, id: \.0) { month, procs in
                    Section(header: Text(month)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                    ) {
                        ForEach(procs, id: \.id) { proc in
                            ProcedureRow(
                                procedure: proc,
                                isExpanded: expandedIds.contains(proc.id ?? ""),
                                onToggle: { toggleExpanded(proc.id) }
                            )
                        }
                    }
                }
            }
        }
        .listStyle(.plain)
        .navigationTitle("Procedures")
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable { await fetchAll() }
        .task {
            if !hasFetched { await fetchAll() }
        }
    }

    private func toggleExpanded(_ id: String?) {
        guard let id else { return }
        withAnimation(.easeInOut(duration: 0.25)) {
            if expandedIds.contains(id) {
                expandedIds.remove(id)
            } else {
                expandedIds.insert(id)
            }
        }
    }

    private func fetchAll() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        do {
            let request = BWell.HealthDataRequest(page: 0, pageSize: 100)
            let response = try await sdk.health.getProcedures(request)
            let all = response.entry?.compactMap { $0.resource } ?? []
            procedures = all.sorted {
                ($0.performedDateTime ?? $0.performedPeriod?.start ?? "") >
                ($1.performedDateTime ?? $1.performedPeriod?.start ?? "")
            }
        } catch {
            NSLog("[Procedures] Error: %@", error.localizedDescription)
            procedures = []
        }
        isLoading = false
        hasFetched = true
    }

    private func parseFHIRDate(_ string: String) -> Date? {
        let formats = ["yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                       "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"]
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        for format in formats {
            formatter.dateFormat = format
            if let date = formatter.date(from: string) { return date }
        }
        return nil
    }
}

// MARK: - Procedure Row

private struct ProcedureRow: View {
    let procedure: BWell.Procedure
    let isExpanded: Bool
    let onToggle: () -> Void

    private var procName: String {
        procedure.code?.coding?.first?.display
            ?? procedure.code?.text
            ?? "Procedure"
    }

    private var bodySiteText: String? {
        procedure.bodySite?.first?.text ?? procedure.bodySite?.first?.coding?.first?.display
    }

    private var statusText: String {
        procedure.status?.capitalizingFirstLetter() ?? "Unknown"
    }

    private var dateText: String? {
        procedure.performedDateTime?.dateFormatter()
            ?? procedure.performedPeriod?.start?.dateFormatter()
            ?? procedure.performedString
    }

    private var performerName: String? {
        for p in procedure.performer ?? [] {
            if let actor = p.actor {
                if let display = actor.display, !display.isEmpty { return display }
                if case .practitioner(let doc) = actor.resource {
                    if let name = doc.name?.first {
                        let given = name.given?.joined(separator: " ") ?? ""
                        let family = name.family ?? ""
                        let parts = [given, family].filter { !$0.isEmpty }
                        if !parts.isEmpty { return parts.joined(separator: " ") }
                    }
                }
                if case .organization(let orgName) = actor.resource {
                    if let orgName, !orgName.isEmpty { return orgName }
                }
            }
        }
        return nil
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: onToggle) {
                HStack(alignment: .center, spacing: 10) {
                    Image(systemName: "cross.case.fill")
                        .font(.title3)
                        .foregroundStyle(.bwellPurple)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 3) {
                        Text(procName)
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .lineLimit(2)
                            .multilineTextAlignment(.leading)

                        if let site = bodySiteText {
                            Text(site)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .lineLimit(1)
                        }
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 3) {
                        Text(statusText)
                            .font(.caption2)
                            .fontWeight(.medium)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(statusColor.opacity(0.12))
                            .foregroundStyle(statusColor)
                            .clipShape(Capsule())

                        if let date = dateText {
                            Text(date)
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
                ProcedureDetailContent(procedure: procedure, performerName: performerName)
                    .padding(.top, 6)
                    .padding(.leading, 38)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 6)
    }

    private var statusColor: Color {
        switch procedure.status?.lowercased() {
        case "completed": return .green
        case "in-progress": return .blue
        case "preparation": return .orange
        case "not-done": return .red
        case "on-hold": return .yellow
        case "stopped": return .orange
        case "entered-in-error": return .red
        default: return .gray
        }
    }
}

// MARK: - Procedure Detail (Expanded)

private struct ProcedureDetailContent: View {
    let procedure: BWell.Procedure
    let performerName: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            detailRow("Status", procedure.status?.capitalizingFirstLetter())
            detailRow("Performed", procedure.performedDateTime?.dateFormatter()
                       ?? procedure.performedString)

            if let period = procedure.performedPeriod {
                detailRow("Start", period.start?.dateFormatter())
                detailRow("End", period.end?.dateFormatter())
            }

            detailRow("Performer", performerName)

            if let bodySites = procedure.bodySite, !bodySites.isEmpty {
                let siteTexts = bodySites.compactMap { $0.text ?? $0.coding?.first?.display }
                if !siteTexts.isEmpty {
                    detailRow("Body Site", siteTexts.joined(separator: "; "))
                }
            }

            if let reasons = procedure.reasonCode, !reasons.isEmpty {
                let reasonTexts = reasons.compactMap { $0.text ?? $0.coding?.first?.display }
                if !reasonTexts.isEmpty {
                    detailRow("Reason", reasonTexts.joined(separator: "; "))
                }
            }

            if let outcome = procedure.outcome {
                detailRow("Outcome", outcome.text ?? outcome.coding?.first?.display)
            }

            if let followUps = procedure.followUp, !followUps.isEmpty {
                let texts = followUps.compactMap { $0.text ?? $0.coding?.first?.display }
                if !texts.isEmpty {
                    detailRow("Follow-up", texts.joined(separator: "; "))
                }
            }

            if let complications = procedure.complication, !complications.isEmpty {
                let texts = complications.compactMap { $0.text ?? $0.coding?.first?.display }
                if !texts.isEmpty {
                    detailRow("Complications", texts.joined(separator: "; "))
                }
            }

            if let notes = procedure.note, !notes.isEmpty {
                ForEach(notes.indices, id: \.self) { i in
                    detailRow("Note", notes[i].text)
                }
            }

            if let code = procedure.code?.coding?.first {
                if let system = code.system, let codeVal = code.code {
                    let shortSystem = system.components(separatedBy: "/").last ?? system
                    detailRow("Code", "\(shortSystem): \(codeVal)")
                }
            }

            if let category = procedure.category {
                detailRow("Category", category.text ?? category.coding?.first?.display)
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
                    .frame(width: 95, alignment: .trailing)
                Text(value)
                    .font(.caption)
                    .foregroundStyle(.primary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }
}

struct ProceduresSheetView: View {
    var procedures: BWellWrapper.procedures

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(procedures.code?.coding?.first?.display ?? procedures.code?.text ?? "Procedure")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Performed date: ", content: procedures.performedDateTime?.dateFormatter())
                DetailedItemView(title: "Performed period: ", content: procedures.performedPeriod?.start?.dateFormatter())
            }.padding(.bottom, 10)

            DetailedItemView(title: "Status: ", content: procedures.status)
            DetailedItemView(title: "Reason: ", content: procedures.reasonCode?.first?.text ?? procedures.reasonCode?.first?.coding?.first?.display)
            DetailedItemView(title: "Body site: ", content: procedures.bodySite?.first?.text ?? procedures.bodySite?.first?.coding?.first?.display)
            DetailedItemView(title: "Outcome: ", content: procedures.outcome?.text ?? procedures.outcome?.coding?.first?.display)

            if let notes = procedures.note {
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
