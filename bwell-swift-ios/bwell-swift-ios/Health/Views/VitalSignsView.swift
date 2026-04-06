//
//  VitalSignsView.swift
//  bwell-swift-ios
//
//  Vital Signs as a category list showing latest reading per vital type.
//  Each row shows the vital name, latest value with unit, interpretation badge,
//  and date. Tapping navigates to the chart/history view for that vital.
//

import SwiftUI
import BWellSDK

struct VitalSignsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var groups: [BWell.VitalSignGroups] = []
    @State private var isLoading = false
    @State private var hasFetched = false

    var body: some View {
        List {
            if isLoading && groups.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !isLoading && groups.isEmpty && hasFetched {
                ContentUnavailableView("No Vital Signs",
                    systemImage: "waveform.path.ecg",
                    description: Text("No vital sign records found."))
            } else {
                ForEach(groups, id: \.id) { group in
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

    private var name: String {
        group.name ?? group.coding?.display ?? "Vital Sign"
    }

    private var latestValue: String? {
        // Direct value
        if let qty = group.value?.valueQuantity, let val = qty.value {
            let unit = qty.unit ?? ""
            return "\(formatNumber(val)) \(unit)".trimmingCharacters(in: .whitespaces)
        }
        if let str = group.value?.valueString, !str.isEmpty {
            return str
        }
        // Component values (e.g., blood pressure systolic/diastolic)
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
        if let interp = group.interpretation?.first {
            return interp.coding?.first?.display ?? interp.text
        }
        return nil
    }

    private var isAbnormal: Bool {
        guard let code = group.interpretation?.first?.coding?.first?.code?.lowercased() else { return false }
        return code == "h" || code == "hh" || code == "l" || code == "ll"
            || code == "a" || code == "aa" || code == "high" || code == "low"
    }

    private var dateText: String? {
        group.effectiveDateTime?.dateFormatter()
    }

    private var icon: String {
        let nameLower = (group.name ?? "").lowercased()
        if nameLower.contains("blood pressure") { return "heart.fill" }
        if nameLower.contains("heart rate") || nameLower.contains("pulse") { return "waveform.path.ecg" }
        if nameLower.contains("temperature") { return "thermometer.medium" }
        if nameLower.contains("respiratory") || nameLower.contains("breathing") { return "lungs.fill" }
        if nameLower.contains("oxygen") || nameLower.contains("spo2") || nameLower.contains("o2") { return "o2.circle.fill" }
        if nameLower.contains("weight") { return "scalemass.fill" }
        if nameLower.contains("height") || nameLower.contains("stature") { return "ruler.fill" }
        if nameLower.contains("bmi") || nameLower.contains("body mass") { return "figure.stand" }
        if nameLower.contains("head circumference") { return "brain.head.profile" }
        return "waveform.path.ecg.rectangle"
    }

    private var iconColor: Color {
        if isAbnormal { return .red }
        let nameLower = (group.name ?? "").lowercased()
        if nameLower.contains("blood pressure") || nameLower.contains("heart") { return .red }
        if nameLower.contains("temperature") { return .orange }
        if nameLower.contains("oxygen") || nameLower.contains("respiratory") { return .blue }
        return .bwellPurple
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
                    .lineLimit(2)

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
