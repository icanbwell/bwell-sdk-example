//
//  VitalSignsView.swift
//  bwell-swift-ios
//
//  Vital Signs organized using FHIR Observation coding (LOINC).
//  Rows show friendly names, latest values, interpretation badges.
//  Tapping navigates to the chart/history view for that vital.
//

import SwiftUI
import BWellSDK

struct VitalSignsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var groups: [BWell.VitalSignGroups] = []
    @State private var isLoading = false
    @State private var hasFetched = false

    // FHIR vital signs panel LOINC codes — anything not in this set
    // is metadata or supplemental and gets filtered to "Other"
    private static let coreVitalCodes: Set<String> = [
        "85354-9",  // Blood pressure panel
        "8480-6",   // Systolic BP
        "8462-4",   // Diastolic BP
        "8867-4",   // Heart rate
        "8310-5",   // Body temperature
        "8331-1",   // Oral temperature
        "9279-1",   // Respiratory rate
        "2708-6",   // Oxygen saturation (SpO2)
        "59408-5",  // Oxygen saturation (pulse ox)
        "29463-7",  // Body weight
        "3141-9",   // Body weight (measured)
        "8302-2",   // Body height
        "8306-3",   // Body height (lying)
        "39156-5",  // BMI
        "9843-4",   // Head circumference
        "3140-1",   // Body surface area
    ]

    private var displayGroups: [BWell.VitalSignGroups] {
        // Filter to core vital LOINC codes; show others only if they have a value
        groups.filter { group in
            if let code = group.coding?.code, VitalSignsView.coreVitalCodes.contains(code) {
                return true
            }
            // Include if it has an actual measured value (not metadata)
            return group.value?.valueQuantity?.value != nil
                || group.value?.valueString != nil
                || (group.component?.isEmpty == false)
        }
    }

    var body: some View {
        List {
            if isLoading && groups.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !isLoading && displayGroups.isEmpty && hasFetched {
                ContentUnavailableView("No Vital Signs",
                    systemImage: "waveform.path.ecg",
                    description: Text("No vital sign records found."))
            } else {
                ForEach(displayGroups, id: \.id) { group in
                    if let id = group.id, let coding = group.coding {
                        NavigationLink(value: AppView.healthGroupItems(
                            category: .vitalSigns,
                            groupCode: BWellHealthDataWrapper(id, coding)
                        )) {
                            VitalSignGroupRow(group: group)
                        }
                    } else {
                        VitalSignGroupRow(group: group)
                    }
                }
            }
        }
        .listStyle(.plain)
        .navigationTitle("Vital Signs")
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
            let request = BWell.HealthDataGroupRequest(page: 0)
            let result = try await sdk.health.getVitalSignGroups(request)
            let all = result.resources ?? []
            groups = all.sorted {
                ($0.effectiveDateTime ?? "") > ($1.effectiveDateTime ?? "")
            }
            viewModel.vitalSignGroups = groups
        } catch {
            NSLog("[VitalSigns] Error: %@", error.localizedDescription)
            groups = []
        }
        isLoading = false
        hasFetched = true
    }
}

// MARK: - Vital Sign Group Row

private struct VitalSignGroupRow: View {
    let group: BWell.VitalSignGroups

    // Friendly name: prefer coding.display (LOINC standard),
    // fall back to group name, with overrides for common cases
    private var name: String {
        let raw = group.coding?.display ?? group.name ?? "Vital Sign"
        return Self.friendlyOverrides[raw.lowercased()] ?? raw
    }

    // Override LOINC display names that are overly clinical
    private static let friendlyOverrides: [String: String] = [
        "oxygen saturation in arterial blood by pulse oximetry": "Blood Oxygen",
        "inhaled oxygen concentration": "Inhaled O₂",
        "body mass index (bmi) [ratio]": "BMI",
        "body height --lying": "Length (Lying)",
    ]

    private var latestValue: String? {
        if let qty = group.value?.valueQuantity, let val = qty.value {
            let unit = qty.unit ?? ""
            return "\(formatNumber(val)) \(unit)".trimmingCharacters(in: .whitespaces)
        }
        if let str = group.value?.valueString, !str.isEmpty {
            return str
        }
        if let components = group.component, !components.isEmpty {
            let parts = components.compactMap { comp -> String? in
                guard let val = comp.value?.valueQuantity?.value else { return nil }
                return formatNumber(val)
            }
            if !parts.isEmpty {
                let unit = components.first?.value?.valueQuantity?.unit ?? ""
                return parts.joined(separator: "/") + (unit.isEmpty ? "" : " \(unit)")
            }
        }
        return nil
    }

    private var interpretationText: String? {
        group.interpretation?.first?.coding?.first?.display
            ?? group.interpretation?.first?.text
    }

    private var isAbnormal: Bool {
        guard let code = group.interpretation?.first?.coding?.first?.code?.lowercased() else { return false }
        return ["h", "hh", "l", "ll", "a", "aa", "high", "low"].contains(code)
    }

    private var dateText: String? {
        group.effectiveDateTime?.dateFormatter()
    }

    private var icon: String {
        let code = group.coding?.code ?? ""
        switch code {
        case "85354-9", "8480-6", "8462-4": return "heart.fill"
        case "8867-4": return "waveform.path.ecg"
        case "8310-5", "8331-1": return "thermometer.medium"
        case "9279-1": return "lungs.fill"
        case "2708-6", "59408-5": return "o2.circle.fill"
        case "29463-7", "3141-9": return "scalemass.fill"
        case "8302-2", "8306-3": return "ruler.fill"
        case "39156-5": return "figure.stand"
        case "9843-4": return "brain.head.profile"
        default:
            // Fall back to name-based matching
            let n = name.lowercased()
            if n.contains("blood pressure") { return "heart.fill" }
            if n.contains("heart") || n.contains("pulse") { return "waveform.path.ecg" }
            if n.contains("temperature") { return "thermometer.medium" }
            if n.contains("respiratory") { return "lungs.fill" }
            if n.contains("oxygen") { return "o2.circle.fill" }
            if n.contains("weight") { return "scalemass.fill" }
            if n.contains("height") { return "ruler.fill" }
            return "waveform.path.ecg.rectangle"
        }
    }

    private var iconColor: Color {
        if isAbnormal { return .red }
        let code = group.coding?.code ?? ""
        switch code {
        case "85354-9", "8480-6", "8462-4", "8867-4": return .red
        case "8310-5", "8331-1": return .orange
        case "9279-1", "2708-6", "59408-5": return .blue
        case "29463-7", "3141-9", "8302-2", "8306-3", "39156-5", "9843-4": return .purple
        default: return .bwellPurple
        }
    }

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundStyle(iconColor)
                .frame(width: 28)

            VStack(alignment: .leading, spacing: 3) {
                Text(name)
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundStyle(.primary)

                if let date = dateText {
                    Text(date)
                        .font(.caption)
                        .foregroundStyle(.tertiary)
                }
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 3) {
                if let value = latestValue {
                    Text(value)
                        .font(.title3)
                        .fontWeight(.semibold)
                        .foregroundStyle(isAbnormal ? .red : .primary)
                }

                if let interp = interpretationText {
                    Text(interp)
                        .font(.caption2)
                        .fontWeight(.medium)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(interpColor.opacity(0.12))
                        .foregroundStyle(interpColor)
                        .clipShape(Capsule())
                }
            }

            Image(systemName: "chart.line.uptrend.xyaxis")
                .font(.caption)
                .foregroundStyle(.tertiary)
        }
        .padding(.vertical, 4)
    }

    private var interpColor: Color {
        guard let code = group.interpretation?.first?.coding?.first?.code?.lowercased() else { return .gray }
        switch code {
        case "n", "normal": return .green
        case "h", "high": return .orange
        case "hh": return .red
        case "l", "low": return .blue
        case "ll": return .red
        case "a", "abnormal": return .orange
        case "aa": return .red
        default: return .gray
        }
    }

    private func formatNumber(_ value: Double) -> String {
        if value == value.rounded() && value < 10000 {
            return String(format: "%.0f", value)
        }
        return String(format: "%.1f", value)
    }
}

// MARK: - Legacy Sheet View

struct VitalSignsSheetView: View {
    var vitalSigns: BWellWrapper.vitalSigns

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(vitalSigns.code?.coding?.first?.display ?? vitalSigns.code?.text ?? "Vital Sign")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

            if let valueQuantity = vitalSigns.valueQuantity, let value = valueQuantity.value {
                let unit = valueQuantity.unit ?? ""
                DetailedItemView(title: "Value: ", content: "\(value) \(unit)".trimmingCharacters(in: .whitespaces))
            } else if let valueString = vitalSigns.valueString {
                DetailedItemView(title: "Value: ", content: valueString)
            }

            if let referenceRange = vitalSigns.referenceRange?.first {
                let low = referenceRange.low?.value.map { "\($0)" } ?? ""
                let high = referenceRange.high?.value.map { "\($0)" } ?? ""
                let unit = referenceRange.low?.unit ?? referenceRange.high?.unit ?? ""
                if !low.isEmpty || !high.isEmpty {
                    DetailedItemView(title: "Reference range: ", content: "\(low) - \(high) \(unit)".trimmingCharacters(in: .whitespaces))
                }
            }

            DetailedItemView(title: "Interpretation: ", content: vitalSigns.interpretation?.first?.text)
            DetailedItemView(title: "Status: ", content: vitalSigns.status)

            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Effective date: ", content: vitalSigns.effectiveDateTime?.dateFormatter())
            }.padding(.bottom, 10)

            if let notes = vitalSigns.note {
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
