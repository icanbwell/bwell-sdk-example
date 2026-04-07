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

    private var displayGroups: [BWell.VitalSignGroups] {
        // Only show observations with recognized vital sign LOINC codes.
        // The API returns all observations (labs, social history, etc.) —
        // we filter to the FHIR vital signs panel codes only.
        groups.filter { group in
            guard let code = group.coding?.code else { return false }
            return LoincVitalSign(rawValue: code) != nil
        }
    }

    // Group vitals by Apple Health-style categories using LOINC codes
    private var categorizedGroups: [(category: VitalSignCategory, items: [BWell.VitalSignGroups])] {
        var buckets: [VitalSignCategory: [BWell.VitalSignGroups]] = [:]
        for group in displayGroups {
            let cat: VitalSignCategory
            if let code = group.coding?.code, let loinc = LoincVitalSign(rawValue: code) {
                cat = loinc.category
            } else {
                cat = .otherVitals
            }
            buckets[cat, default: []].append(group)
        }
        // Return in Apple Health order, only categories that have data
        return VitalSignCategory.allCases.compactMap { cat in
            guard let items = buckets[cat], !items.isEmpty else { return nil }
            return (category: cat, items: items)
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
                ForEach(categorizedGroups, id: \.category) { section in
                    Section {
                        ForEach(section.items, id: \.id) { group in
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
                    } header: {
                        HStack(spacing: 6) {
                            Image(systemName: section.category.icon)
                                .foregroundStyle(section.category.color)
                            Text(section.category.rawValue)
                        }
                        .font(.subheadline)
                        .fontWeight(.semibold)
                    }
                }
            }
        }
        .listStyle(.insetGrouped)
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
            for g in all {
                NSLog("[VitalSigns] name=%@ code=%@ display=%@ value=%@ date=%@",
                      g.name ?? "nil",
                      g.coding?.code ?? "nil",
                      g.coding?.display ?? "nil",
                      g.value?.valueQuantity?.value.map { String($0) } ?? g.value?.valueString ?? "nil",
                      g.effectiveDateTime ?? "nil")
            }
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

    private var loincVital: LoincVitalSign? {
        guard let code = group.coding?.code else { return nil }
        return LoincVitalSign(rawValue: code)
    }

    private var name: String {
        // Prefer LOINC-mapped friendly name, fall back to coding display
        if let vital = loincVital { return vital.friendlyName }
        return group.coding?.display ?? group.name ?? "Vital Sign"
    }

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

    private var interpretation: FHIRInterpretation? {
        FHIRInterpretation(code: group.interpretation?.first?.coding?.first?.code)
    }

    private var interpretationText: String? {
        group.interpretation?.first?.coding?.first?.display
            ?? group.interpretation?.first?.text
    }

    private var dateText: String? {
        group.effectiveDateTime?.dateFormatter()
    }

    private var icon: String {
        loincVital?.icon ?? "waveform.path.ecg.rectangle"
    }

    private var iconColor: Color {
        if interpretation?.isAbnormal == true { return .red }
        return loincVital?.color ?? .bwellPurple
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
                        .foregroundStyle(interpretation?.isAbnormal == true ? .red : .primary)
                }

                if let text = interpretationText, let interp = interpretation {
                    Text(text)
                        .font(.caption2)
                        .fontWeight(.medium)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(interp.color.opacity(0.12))
                        .foregroundStyle(interp.color)
                        .clipShape(Capsule())
                }
            }

            Image(systemName: "chart.line.uptrend.xyaxis")
                .font(.caption)
                .foregroundStyle(.tertiary)
        }
        .padding(.vertical, 4)
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
