//
//  MedicationsView.swift
//  bwell-swift-ios
//
//  Medications organized by status (Active vs Past) with expandable detail.
//  Each row shows medication name, dosage, status, and date range.
//  Expanded detail shows full dosage info, reason, period, and a link
//  to the MedicationDetailView for Knowledge and Pricing tabs.
//

import SwiftUI
import BWellSDK

struct MedicationsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var medications: [BWell.MedicationStatement] = []
    @State private var isLoading = false
    @State private var hasFetched = false
    @State private var expandedIds: Set<String> = []

    private var activeMeds: [BWell.MedicationStatement] {
        medications.filter { $0.status?.lowercased() == "active" }
    }

    private var pastMeds: [BWell.MedicationStatement] {
        medications.filter { $0.status?.lowercased() != "active" }
    }

    var body: some View {
        List {
            if isLoading && medications.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !isLoading && medications.isEmpty && hasFetched {
                ContentUnavailableView("No Medications",
                    systemImage: "pills",
                    description: Text("No medication records found."))
            } else {
                // Active medications section
                if !activeMeds.isEmpty {
                    Section(header: HStack(spacing: 6) {
                        Image(systemName: "circle.fill")
                            .font(.system(size: 8))
                            .foregroundStyle(.green)
                        Text("Active (\(activeMeds.count))")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                    }) {
                        ForEach(activeMeds, id: \.id) { med in
                            MedicationRow(
                                medication: med,
                                isExpanded: expandedIds.contains(med.id ?? ""),
                                onToggle: { toggleExpanded(med.id) }
                            )
                        }
                    }
                }

                // Past medications section
                if !pastMeds.isEmpty {
                    Section(header: HStack(spacing: 6) {
                        Image(systemName: "circle.fill")
                            .font(.system(size: 8))
                            .foregroundStyle(.gray)
                        Text("Past (\(pastMeds.count))")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                    }) {
                        ForEach(pastMeds, id: \.id) { med in
                            MedicationRow(
                                medication: med,
                                isExpanded: expandedIds.contains(med.id ?? ""),
                                onToggle: { toggleExpanded(med.id) }
                            )
                        }
                    }
                }
            }
        }
        .listStyle(.plain)
        .navigationTitle("Medications")
        .toolbarColorScheme(.dark, for: .navigationBar)
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
            let response = try await sdk.health.getMedicationStatements(request)
            let all = response.entry?.compactMap { $0.resource } ?? []
            // Sort: active first, then by start date descending
            medications = all.sorted {
                let s0 = $0.status?.lowercased() == "active" ? 0 : 1
                let s1 = $1.status?.lowercased() == "active" ? 0 : 1
                if s0 != s1 { return s0 < s1 }
                return ($0.effectivePeriod?.start ?? "") > ($1.effectivePeriod?.start ?? "")
            }
        } catch {
            NSLog("[Medications] Error: %@", error.localizedDescription)
            medications = []
        }
        isLoading = false
        hasFetched = true
    }
}

// MARK: - Medication Row

private struct MedicationRow: View {
    let medication: BWell.MedicationStatement
    let isExpanded: Bool
    let onToggle: () -> Void

    private var medName: String {
        medication.medicationCodeableConcept?.coding?.first?.display
            ?? medication.medicationCodeableConcept?.text
            ?? "Medication"
    }

    private var dosageText: String? {
        medication.dosage?.first?.text
    }

    private var isActive: Bool {
        medication.status?.lowercased() == "active"
    }

    private var statusText: String {
        medication.status?.capitalizingFirstLetter() ?? "Unknown"
    }

    private var dateRange: String? {
        let start = medication.effectivePeriod?.start?.dateFormatter()
        let end = medication.effectivePeriod?.end?.dateFormatter()
        if let start, let end {
            return "\(start) - \(end)"
        } else if let start {
            return "Since \(start)"
        }
        return nil
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: onToggle) {
                HStack(alignment: .center, spacing: 10) {
                    Image(systemName: isActive ? "pill.fill" : "pill")
                        .font(.title3)
                        .foregroundStyle(isActive ? .bwellPurple : .gray)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 3) {
                        Text(medName)
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .lineLimit(2)
                            .multilineTextAlignment(.leading)

                        if let dosage = dosageText {
                            Text(dosage)
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

                        if let range = dateRange {
                            Text(range)
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
                MedicationDetailContent(medication: medication)
                    .padding(.top, 6)
                    .padding(.leading, 38)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 6)
    }

    private var statusColor: Color {
        switch medication.status?.lowercased() {
        case "active": return .green
        case "completed": return .blue
        case "stopped": return .orange
        case "on-hold": return .yellow
        case "entered-in-error": return .red
        default: return .gray
        }
    }
}

// MARK: - Medication Detail (Expanded)

private struct MedicationDetailContent: View {
    let medication: BWell.MedicationStatement

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            detailRow("Status", medication.status?.capitalizingFirstLetter())
            detailRow("Start", medication.effectivePeriod?.start?.dateFormatter())
            detailRow("End", medication.effectivePeriod?.end?.dateFormatter())

            if let reasons = medication.reasonCode, !reasons.isEmpty {
                let reasonTexts = reasons.compactMap { $0.text ?? $0.coding?.first?.display }
                if !reasonTexts.isEmpty {
                    detailRow("Reason", reasonTexts.joined(separator: "; "))
                }
            }

            if let dosages = medication.dosage, !dosages.isEmpty {
                ForEach(dosages.indices, id: \.self) { i in
                    let dose = dosages[i]
                    detailRow("Dosage", dose.text)
                    if let route = dose.route?.text ?? dose.route?.coding?.first?.display {
                        detailRow("Route", route)
                    }
                    if let instruction = dose.patientInstruction {
                        detailRow("Instructions", instruction)
                    }
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
                    .frame(width: 70, alignment: .trailing)
                Text(value)
                    .font(.caption)
                    .foregroundStyle(.primary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }
}

struct MedicationsSheetView: View {
    var medications: BWellWrapper.medications

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(medications.medicationCodeableConcept?.text ?? "Title unavailable")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Period start: ", content: medications.effectivePeriod?.start?.dateFormatter())
                DetailedItemView(title: "Period end: ", content: medications.effectivePeriod?.end?.dateFormatter())
            }.padding(.bottom, 10)

            DetailedItemView(title: "Status: ", content: medications.status)
            if let reasonCode = medications.reasonCode {
                ForEach(reasonCode, id: \.id) { reason in
                    DetailedItemView(title: "Reason for medication: ", content: reason.text)
                }
            }

            if let dosage = medications.dosage {
                ForEach(dosage, id: \.id) { dose in
                    DetailedItemView(title: "Dosage: ", content: dose.text)
                }
            }
            Spacer()
        }
        .padding()
        .presentationDragIndicator(.visible)
        .presentationDetents([.medium])
    }
}
