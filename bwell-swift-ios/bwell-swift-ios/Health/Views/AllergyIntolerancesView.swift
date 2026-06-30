//
//  AllergyIntolerancesView.swift
//  bwell-swift-ios
//
//  Allergies organized by criticality (High/Low/Unknown) with expandable detail.
//  Shows allergen name, category, criticality badge, clinical status,
//  reactions with manifestations and severity.
//

import SwiftUI
import BWellSDK

struct AllergyIntolerancesView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var allergies: [BWell.AllergyIntolerance] = []
    @State private var isLoading = false
    @State private var hasFetched = false
    @State private var expandedIds: Set<String> = []

    private var highCrit: [BWell.AllergyIntolerance] {
        allergies.filter { ($0.criticality ?? "").lowercased() == "high" }
    }

    private var lowCrit: [BWell.AllergyIntolerance] {
        allergies.filter { ($0.criticality ?? "").lowercased() == "low" }
    }

    private var otherCrit: [BWell.AllergyIntolerance] {
        allergies.filter {
            let c = ($0.criticality ?? "").lowercased()
            return c != "high" && c != "low"
        }
    }

    var body: some View {
        List {
            if isLoading && allergies.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !isLoading && allergies.isEmpty && hasFetched {
                ContentUnavailableView("No Allergies",
                    systemImage: "allergens",
                    description: Text("No allergy or intolerance records found."))
            } else {
                allergySection("High Criticality", allergies: highCrit, dotColor: .red)
                allergySection("Low Criticality", allergies: lowCrit, dotColor: .orange)
                allergySection("Other", allergies: otherCrit, dotColor: .gray)
            }
        }
        .listStyle(.plain)
        .navigationTitle("Allergies")
        .toolbarColorScheme(.dark, for: .navigationBar)

        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable { await fetchAll() }
        .task {
            if !hasFetched { await fetchAll() }
        }
    }

    @ViewBuilder
    private func allergySection(_ title: String, allergies: [BWell.AllergyIntolerance], dotColor: Color) -> some View {
        if !allergies.isEmpty {
            Section(header: HStack(spacing: 6) {
                Image(systemName: "circle.fill")
                    .font(.system(size: 8))
                    .foregroundStyle(dotColor)
                Text("\(title) (\(allergies.count))")
                    .font(.subheadline)
                    .fontWeight(.semibold)
            }) {
                ForEach(allergies, id: \.id) { allergy in
                    AllergyRow(
                        allergy: allergy,
                        isExpanded: expandedIds.contains(allergy.id ?? ""),
                        onToggle: { toggleExpanded(allergy.id) }
                    )
                }
            }
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
            let response = try await sdk.health.getAllergyIntolerances(request)
            let all = response.entry?.compactMap { $0.resource } ?? []
            // Sort: active first, then high criticality first
            allergies = all.sorted {
                let active0 = isActive($0) ? 0 : 1
                let active1 = isActive($1) ? 0 : 1
                if active0 != active1 { return active0 < active1 }
                let crit0 = critOrder($0)
                let crit1 = critOrder($1)
                return crit0 < crit1
            }
        } catch {
            NSLog("[Allergies] Error: %@", error.localizedDescription)
            allergies = []
        }
        isLoading = false
        hasFetched = true
    }

    private func isActive(_ a: BWell.AllergyIntolerance) -> Bool {
        let status = a.clinicalStatus?.coding?.first?.code?.lowercased()
            ?? a.clinicalStatus?.text?.lowercased() ?? ""
        return status == "active"
    }

    private func critOrder(_ a: BWell.AllergyIntolerance) -> Int {
        switch (a.criticality ?? "").lowercased() {
        case "high": return 0
        case "low": return 1
        default: return 2
        }
    }
}

// MARK: - Allergy Row

private struct AllergyRow: View {
    let allergy: BWell.AllergyIntolerance
    let isExpanded: Bool
    let onToggle: () -> Void

    private var allergenName: String {
        allergy.code?.coding?.first?.display
            ?? allergy.code?.text
            ?? "Allergen"
    }

    private var categoryText: String? {
        allergy.category?.compactMap { $0.capitalizingFirstLetter() }.joined(separator: ", ")
    }

    private var criticalityText: String {
        allergy.criticality?.capitalizingFirstLetter() ?? "Unknown"
    }

    private var clinicalStatusText: String? {
        allergy.clinicalStatus?.coding?.first?.code?.capitalizingFirstLetter()
            ?? allergy.clinicalStatus?.text?.capitalizingFirstLetter()
    }

    private var manifestationSummary: String? {
        let manifests = allergy.reaction?.flatMap { $0.manifestation ?? [] }
            .compactMap { $0.coding?.first?.display ?? $0.text }
        guard let manifests, !manifests.isEmpty else { return nil }
        return manifests.joined(separator: ", ")
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: onToggle) {
                HStack(alignment: .center, spacing: 10) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.title3)
                        .foregroundStyle(critColor)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 3) {
                        Text(allergenName)
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .lineLimit(2)
                            .multilineTextAlignment(.leading)

                        if let manifest = manifestationSummary {
                            Text(manifest)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .lineLimit(1)
                        } else if let category = categoryText {
                            Text(category)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .lineLimit(1)
                        }
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 3) {
                        Text(criticalityText)
                            .font(.caption2)
                            .fontWeight(.medium)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(critColor.opacity(0.12))
                            .foregroundStyle(critColor)
                            .clipShape(Capsule())

                        if let status = clinicalStatusText {
                            Text(status)
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
                AllergyDetailContent(allergy: allergy)
                    .padding(.top, 6)
                    .padding(.leading, 38)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 6)
    }

    private var critColor: Color {
        FHIRAllergyCriticality(rawValue: allergy.criticality)?.color ?? .gray
    }
}

// MARK: - Allergy Detail (Expanded)

private struct AllergyDetailContent: View {
    let allergy: BWell.AllergyIntolerance

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            detailRow("Criticality", allergy.criticality?.capitalizingFirstLetter())

            if let status = allergy.clinicalStatus {
                detailRow("Clinical", status.coding?.first?.code?.capitalizingFirstLetter() ?? status.text)
            }

            if let verification = allergy.verificationStatus {
                detailRow("Verification", verification.coding?.first?.code?.capitalizingFirstLetter() ?? verification.text)
            }

            if let categories = allergy.category, !categories.isEmpty {
                let cats = categories.compactMap { $0.capitalizingFirstLetter() }
                if !cats.isEmpty {
                    detailRow("Category", cats.joined(separator: ", "))
                }
            }

            detailRow("Type", allergy.type?.capitalizingFirstLetter())
            detailRow("Onset", allergy.onsetDateTime?.dateFormatter())
            detailRow("Recorded", allergy.recordedDate?.dateFormatter())
            detailRow("Last Occur.", allergy.lastOccurrence?.dateFormatter())

            if let recorder = allergy.recorder {
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

            // Reactions
            if let reactions = allergy.reaction, !reactions.isEmpty {
                ForEach(reactions.indices, id: \.self) { i in
                    let reaction = reactions[i]
                    Divider().padding(.vertical, 2)

                    if let manifests = reaction.manifestation, !manifests.isEmpty {
                        let texts = manifests.compactMap { $0.coding?.first?.display ?? $0.text }
                        detailRow("Reaction", texts.joined(separator: ", "))
                    }

                    if let severity = reaction.severity {
                        detailRow("Severity", severity.capitalizingFirstLetter())
                    }

                    if let desc = reaction.description {
                        detailRow("Description", desc)
                    }

                    if let onset = reaction.onset {
                        detailRow("Onset", onset.dateFormatter())
                    }
                }
            }

            // Notes
            if let notes = allergy.note, !notes.isEmpty {
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

struct AllergySheetView: View {
    var allergy: BWellWrapper.allergyIntolerances

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 5) {
                Text(allergy.code?.coding?.first?.display ?? "Title unavailable")
                    .font(.headline)
                    .fontWeight(.medium)
                    .padding(.vertical, 20)

                VStack(alignment: .leading, spacing: 10) {
                    DetailedItemView(title: "Category: ", content: allergy.category?.first?.capitalizingFirstLetter())
                    DetailedItemView(title: "Clinical status: ", content: allergy.clinicalStatus?.text ?? allergy.clinicalStatus?.coding?.first?.display)
                    DetailedItemView(title: "Verification status: ", content: allergy.verificationStatus?.text ?? allergy.verificationStatus?.coding?.first?.display)
                    DetailedItemView(title: "Criticality: ", content: allergy.criticality)
                    DetailedItemView(title: "Type: ", content: allergy.type)
                    DetailedItemView(title: "Onset date: ", content: allergy.onsetDateTime?.dateFormatter())
                    DetailedItemView(title: "Recorded date: ", content: allergy.recordedDate?.dateFormatter())
                }
                .padding(.bottom, 10)

                if let reactions = allergy.reaction, !reactions.isEmpty {
                    Text("Reactions:")
                        .font(.title2)
                        .fontWeight(.semibold)
                    ForEach(reactions, id: \.id) { reaction in
                        VStack(alignment: .leading, spacing: 4) {
                            if let manifestations = reaction.manifestation {
                                ForEach(manifestations, id: \.id) { manifestation in
                                    Text(manifestation.coding?.first?.display ?? manifestation.text ?? "")
                                }
                            }
                            if let severity = reaction.severity {
                                DetailedItemView(title: "Severity: ", content: severity)
                            }
                        }
                        .padding(.bottom, 4)
                    }
                }

                if let notes = allergy.note, !notes.isEmpty {
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
            .padding(10)
        }
        .presentationDragIndicator(.visible)
        .presentationDetents([.medium, .large])
    }
}

extension AllergySheetView {
    func getStatusColor(of criticality: String) -> Color {
        if criticality == "high" {
            return .bwellRed
        } else if criticality == "low" {
            return .bwellGreen
        } else if criticality == "unable-to-assess" || criticality == "unknown" {
            return .gray
        } else {
            return .gray
        }
    }
}
