//
//  EncountersView.swift
//  bwell-swift-ios
//
//  Encounters organized chronologically with expandable detail.
//  Title is visit type (Office Visit, Emergency, etc.), subtitle is reason/chief complaint.
//  Class-aware icons distinguish ambulatory, emergency, inpatient, and virtual visits.
//

import SwiftUI
import BWell

struct EncountersView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var encounters: [BWell.Encounter] = []
    @State private var isLoading = false
    @State private var hasFetched = false

    var body: some View {
        List {
            if isLoading && encounters.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !isLoading && encounters.isEmpty && hasFetched {
                ContentUnavailableView("No Encounters",
                    systemImage: "person.2.wave.2",
                    description: Text("No encounter records found."))
            } else {
                ForEach(groupedByMonth, id: \.key) { section in
                    Section(header: Text(section.key).font(.subheadline).fontWeight(.semibold)) {
                        ForEach(section.encounters, id: \.id) { encounter in
                            EncounterRow(encounter: encounter)
                        }
                    }
                }
            }
        }
        .listStyle(.plain)
        .navigationTitle("Encounters")
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable { await fetchAll() }
        .task {
            if !hasFetched { await fetchAll() }
        }
    }

    private func fetchAll() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        do {
            let request = BWell.HealthDataRequest(page: 0, pageSize: 100)
            let response = try await sdk.health.getEncounters(request)
            let all = response.entry?.compactMap { $0.resource } ?? []
            encounters = all.sorted { ($0.period?.start ?? "") > ($1.period?.start ?? "") }
        } catch {
            NSLog("[Encounters] Error: %@", error.localizedDescription)
            encounters = []
        }
        isLoading = false
        hasFetched = true
    }

    private struct MonthSection {
        let key: String
        let encounters: [BWell.Encounter]
    }

    private var groupedByMonth: [MonthSection] {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM yyyy"
        formatter.locale = Locale(identifier: "en_US")

        var sections: [String: [BWell.Encounter]] = [:]
        var order: [String] = []

        for encounter in encounters {
            let monthKey: String
            if let dateStr = encounter.period?.start, let date = dateStr.fhirDate() {
                monthKey = formatter.string(from: date)
            } else {
                monthKey = "Unknown Date"
            }
            if sections[monthKey] == nil {
                order.append(monthKey)
            }
            sections[monthKey, default: []].append(encounter)
        }

        return order.map { MonthSection(key: $0, encounters: sections[$0] ?? []) }
    }
}

// MARK: - Encounter Row

private struct EncounterRow: View {
    let encounter: BWell.Encounter
    @State private var isExpanded = false

    // Visit type as title (Office Visit, Emergency, etc.)
    private var visitType: String {
        encounter.type?.first?.coding?.first?.display
            ?? encounter.type?.first?.text
            ?? encounter.class?.display
            ?? classCodeLabel
            ?? "Visit"
    }

    // Reason/chief complaint as subtitle
    private var reasonText: String? {
        let texts = encounter.reasonCode?.compactMap { $0.coding?.first?.display ?? $0.text } ?? []
        guard !texts.isEmpty else { return nil }
        return texts.joined(separator: ", ")
    }

    private var practitionerName: String? {
        for p in encounter.participant ?? [] {
            if case .practitioner(let doc) = p.individual?.resource {
                if let name = doc.name?.first {
                    let given = name.given?.joined(separator: " ") ?? ""
                    let family = name.family ?? ""
                    let prefix = name.prefix?.first ?? ""
                    let parts = [prefix, given, family].filter { !$0.isEmpty }
                    if !parts.isEmpty { return parts.joined(separator: " ") }
                }
            }
            if let display = p.individual?.display, !display.isEmpty {
                return display
            }
        }
        return nil
    }

    private var facilityName: String? {
        encounter.serviceProvider?.resource?.name
            ?? encounter.location?.first?.location?.resource?.name
            ?? encounter.serviceProvider?.display
    }

    private var statusText: String {
        encounter.status?.capitalizingFirstLetter() ?? "Unknown"
    }

    private var isFinished: Bool {
        encounter.status?.lowercased() == "finished"
    }

    // Map FHIR act encounter class codes to icons
    private var classIcon: String {
        switch encounter.class?.code?.lowercased() {
        case "amb", "ambulatory": return "figure.walk"
        case "emer", "emergency": return "cross.case.fill"
        case "imp", "inpatient", "acute", "nonacute":
            return "building.fill"
        case "vr", "virtual": return "video.fill"
        case "hh", "home": return "house.fill"
        case "obsenc", "observation": return "eye.fill"
        default: return "stethoscope"
        }
    }

    // Readable label from class code when display is missing
    private var classCodeLabel: String? {
        switch encounter.class?.code?.lowercased() {
        case "amb", "ambulatory": return "Ambulatory"
        case "emer", "emergency": return "Emergency"
        case "imp", "inpatient": return "Inpatient"
        case "acute": return "Inpatient Acute"
        case "nonacute": return "Inpatient Non-Acute"
        case "vr", "virtual": return "Virtual"
        case "hh", "home": return "Home Health"
        case "obsenc", "observation": return "Observation"
        default: return nil
        }
    }

    // Icon color based on class
    private var classIconColor: Color {
        switch encounter.class?.code?.lowercased() {
        case "emer", "emergency": return .red
        case "imp", "inpatient", "acute", "nonacute": return .orange
        case "vr", "virtual": return .blue
        default: return .bwellPurple
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: { withAnimation(.easeInOut(duration: 0.25)) { isExpanded.toggle() } }) {
                HStack(alignment: .top, spacing: 12) {
                    // Date column
                    if let dateStr = encounter.period?.start, let date = dateStr.fhirDate() {
                        VStack(spacing: 2) {
                            Text(date, format: .dateTime.day())
                                .font(.title2)
                                .fontWeight(.bold)
                                .foregroundStyle(.bwellPurple)
                            Text(date, format: .dateTime.month(.abbreviated))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        .frame(width: 44)
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        HStack(spacing: 6) {
                            Image(systemName: classIcon)
                                .font(.caption)
                                .foregroundStyle(classIconColor)

                            Text(visitType)
                                .font(.body)
                                .fontWeight(.medium)
                                .foregroundStyle(.primary)
                                .lineLimit(2)
                                .multilineTextAlignment(.leading)
                        }

                        if let reason = reasonText {
                            Text(reason)
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                                .lineLimit(2)
                        }

                        if let provider = practitionerName {
                            Label(provider, systemImage: "person.fill")
                                .font(.caption)
                                .foregroundStyle(.tertiary)
                                .lineLimit(1)
                        }

                        if let facility = facilityName {
                            Label(facility, systemImage: "building.2")
                                .font(.caption)
                                .foregroundStyle(.tertiary)
                                .lineLimit(1)
                        }
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 4) {
                        if !isFinished {
                            Text(statusText)
                                .font(.caption2)
                                .fontWeight(.medium)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(statusColor.opacity(0.12))
                                .foregroundStyle(statusColor)
                                .clipShape(Capsule())
                        }

                        Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                            .font(.caption2)
                            .fontWeight(.semibold)
                            .foregroundStyle(.tertiary)
                    }
                }
            }
            .buttonStyle(.plain)

            if isExpanded {
                Divider().padding(.top, 8)
                EncounterDetailContent(encounter: encounter)
                    .padding(.top, 6)
                    .padding(.leading, 56)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 6)
    }

    private var statusColor: Color {
        switch encounter.status?.lowercased() {
        case "finished": return .green
        case "in-progress", "arrived": return .blue
        case "planned": return .orange
        case "cancelled": return .red
        case "entered-in-error": return .red
        case "triaged": return .purple
        case "onleave": return .yellow
        default: return .gray
        }
    }
}

// MARK: - Encounter Detail

private struct EncounterDetailContent: View {
    let encounter: BWell.Encounter

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            // Type (the visit type detail)
            if let type = encounter.type?.first?.coding?.first?.display ?? encounter.type?.first?.text {
                detailRow("Type", type)
            }

            // Class (ambulatory, emergency, etc.)
            if let classDisplay = encounter.class?.display ?? classCodeLabel {
                detailRow("Class", classDisplay)
            }

            // Status
            detailRow("Status", encounter.status?.capitalizingFirstLetter())

            // Reason codes
            if let reasons = encounter.reasonCode, !reasons.isEmpty {
                let texts = reasons.compactMap { $0.coding?.first?.display ?? $0.text }
                if !texts.isEmpty {
                    detailRow("Reason", texts.joined(separator: "; "))
                }
            }

            // Participants with roles
            ForEach(participantDetails(encounter), id: \.id) { detail in
                detailRow(detail.label, detail.value)
            }

            // Facility
            if let org = encounter.serviceProvider?.resource?.name ?? encounter.serviceProvider?.display {
                detailRow("Facility", org)
            }

            // Location (if different from facility)
            if let loc = encounter.location?.first?.location?.resource?.name,
               loc != encounter.serviceProvider?.resource?.name {
                detailRow("Location", loc)
            }

            // Dates
            detailRow("Date", encounter.period?.start?.dateFormatter())
            if let end = encounter.period?.end,
               end.dateFormatter() != encounter.period?.start?.dateFormatter() {
                detailRow("Through", end.dateFormatter())
            }

            // Duration if both start and end
            if let startStr = encounter.period?.start,
               let endStr = encounter.period?.end,
               let startDate = startStr.fhirDate(),
               let endDate = endStr.fhirDate() {
                let minutes = Int(endDate.timeIntervalSince(startDate) / 60)
                if minutes > 0 && minutes < 1440 {
                    detailRow("Duration", "\(minutes) min")
                } else if minutes >= 1440 {
                    let days = minutes / 1440
                    detailRow("Duration", "\(days) day\(days == 1 ? "" : "s")")
                }
            }

            // Hospitalization discharge
            if let discharge = encounter.hospitalization?.dischargeDisposition {
                detailRow("Discharge", discharge.coding?.first?.display ?? discharge.text)
            }

            // Clinical notes from narrative text
            if let narrative = encounter.text?.div {
                let plainText = narrative.stripHTML()
                if !plainText.isEmpty {
                    Divider().padding(.vertical, 4)
                    Text("Notes")
                        .font(.caption)
                        .fontWeight(.semibold)
                        .foregroundStyle(.secondary)
                    Text(plainText)
                        .font(.caption)
                        .foregroundStyle(.primary)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
        }
    }

    // Readable label from class code when display is missing
    private var classCodeLabel: String? {
        switch encounter.class?.code?.lowercased() {
        case "amb", "ambulatory": return "Ambulatory"
        case "emer", "emergency": return "Emergency"
        case "imp", "inpatient": return "Inpatient"
        case "vr", "virtual": return "Virtual"
        case "hh", "home": return "Home Health"
        default: return nil
        }
    }

    private struct ParticipantDetail: Identifiable {
        let id = UUID()
        let label: String
        let value: String
    }

    private func participantDetails(_ encounter: BWell.Encounter) -> [ParticipantDetail] {
        var results: [ParticipantDetail] = []
        var seen = Set<String>()
        for p in encounter.participant ?? [] {
            if case .practitioner(let doc) = p.individual?.resource,
               let name = doc.name?.first {
                let given = name.given?.joined(separator: " ") ?? ""
                let family = name.family ?? ""
                let prefix = name.prefix?.first ?? ""
                let role = p.type?.first?.coding?.first?.display?.capitalizingFirstLetter() ?? ""
                let nameStr = [prefix, given, family].filter { !$0.isEmpty }.joined(separator: " ")
                if !nameStr.isEmpty && !seen.contains(nameStr) {
                    seen.insert(nameStr)
                    results.append(.init(label: role.isEmpty ? "Provider" : role, value: nameStr))
                }
            }
        }
        return results
    }

    @ViewBuilder
    private func detailRow(_ label: String, _ value: String?) -> some View {
        if let value, !value.isEmpty {
            HStack(alignment: .top, spacing: 6) {
                Text(label)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .frame(width: 70, alignment: .trailing)
                Text(value)
                    .font(.caption)
                    .foregroundStyle(.primary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }
}
