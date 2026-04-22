//
//  LabsView.swift
//  bwell-swift-ios
//
//  Lab results organized by collection date with expandable detail.
//  Shows all results chronologically, grouped by date (like a lab report).
//  Each expanded result shows performer, interpretation, reference range,
//  and a "View Trend" link that navigates to the chart for that test type.
//

import SwiftUI
import Charts
import BWellSDK

struct LabsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var labs: [BWell.Observation] = []
    @State private var isLoading = false
    @State private var hasFetched = false
    @State private var expandedIds: Set<String> = []

    var body: some View {
        List {
            if isLoading && labs.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !isLoading && labs.isEmpty && hasFetched {
                ContentUnavailableView("No Lab Results",
                    systemImage: "testtube.2",
                    description: Text("No lab results found."))
            } else {
                // Abnormal results summary
                let abnormal = labs.filter { !$0.isInReferenceRange }
                if !abnormal.isEmpty {
                    Section {
                        AbnormalSummaryCard(count: abnormal.count, total: labs.count)
                    }
                    .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
                    .listRowSeparator(.hidden)
                }

                ForEach(groupedByDate, id: \.key) { section in
                    Section(header: Text(section.key).font(.subheadline).fontWeight(.semibold)) {
                        ForEach(section.labs, id: \.id) { lab in
                            LabResultRow(
                                lab: lab,
                                isExpanded: expandedIds.contains(lab.id),
                                trendGroupCode: findGroupCode(for: lab),
                                onToggle: {
                                    withAnimation(.easeInOut(duration: 0.25)) {
                                        if expandedIds.contains(lab.id) {
                                            expandedIds.remove(lab.id)
                                        } else {
                                            expandedIds.insert(lab.id)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        .listStyle(.plain)
        .navigationTitle("Labs")
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
            // Fetch labs and groups in parallel
            async let labsFetch: Void = {
                let request = BWell.HealthDataRequest(page: 0, pageSize: 100)
                let response = try await sdk.health.getLabs(request)
                let all = response.entry?.compactMap { $0.resource } ?? []
                self.labs = all.sorted { ($0.effectiveDateTime ?? "") > ($1.effectiveDateTime ?? "") }
            }()
            async let groupsFetch: Void = viewModel.getLabGroups(sdk: sdk)
            _ = try await (labsFetch, groupsFetch)
        } catch {
            NSLog("[Labs] Error: %@", error.localizedDescription)
            labs = []
        }
        isLoading = false
        hasFetched = true
    }

    /// Match an observation to its lab group for "View Trend" navigation
    private func findGroupCode(for lab: BWell.Observation) -> (id: String, coding: BWell.Coding)? {
        let labCode = lab.code?.coding?.first?.code
        let labDisplay = lab.code?.coding?.first?.display ?? lab.code?.text
        for group in viewModel.labGroups {
            if let coding = group.coding {
                // Match by code if available
                if let code = coding.code, let lc = labCode, code == lc {
                    if let id = group.id { return (id, coding) }
                }
                // Fallback: match by display name
                if let display = coding.display, let ld = labDisplay, display == ld {
                    if let id = group.id { return (id, coding) }
                }
            }
        }
        return nil
    }

    private struct DateSection {
        let key: String
        let labs: [BWell.Observation]
    }

    private var groupedByDate: [DateSection] {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM d, yyyy"
        formatter.locale = Locale(identifier: "en_US")

        var sections: [String: [BWell.Observation]] = [:]
        var order: [String] = []

        for lab in labs {
            let dateKey: String
            if let dateStr = lab.effectiveDateTime, let date = dateStr.fhirDate() {
                dateKey = formatter.string(from: date)
            } else {
                dateKey = "Unknown Date"
            }
            if sections[dateKey] == nil {
                order.append(dateKey)
            }
            sections[dateKey, default: []].append(lab)
        }

        return order.map { DateSection(key: $0, labs: sections[$0] ?? []) }
    }
}

// MARK: - Abnormal Summary Card

private struct AbnormalSummaryCard: View {
    let count: Int
    let total: Int

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.title3)
                .foregroundStyle(.orange)
            VStack(alignment: .leading, spacing: 2) {
                Text("\(count) result\(count == 1 ? "" : "s") outside normal range")
                    .font(.subheadline)
                    .fontWeight(.medium)
                Text("out of \(total) total results")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
        }
        .padding()
        .background(Color.orange.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// MARK: - Lab Result Row

private struct LabResultRow: View {
    let lab: BWell.Observation
    let isExpanded: Bool
    let trendGroupCode: (id: String, coding: BWell.Coding)?
    let onToggle: () -> Void

    private var testName: String {
        lab.code?.coding?.first?.display ?? lab.code?.text ?? "Lab Test"
    }

    private var valueText: String? {
        let qty = lab.valueQuantity ?? lab.component?.first?.valueQuantity
        if let qty, let value = qty.value {
            let formatted = value.truncatingRemainder(dividingBy: 1) == 0
                ? String(format: "%.0f", value)
                : String(format: "%.1f", value)
            let unit = qty.unit ?? ""
            return "\(formatted) \(unit)".trimmingCharacters(in: .whitespaces)
        }
        if let str = lab.valueString { return str }
        if let cc = lab.valueCodeableConcept {
            return cc.text ?? cc.coding?.first?.display
        }
        return nil
    }

    private var isInRange: Bool { lab.isInReferenceRange }

    private var hasReferenceRange: Bool {
        lab.referenceRange?.first != nil
    }

    /// Short numeric range text only (skip long prose descriptions)
    private var rangeText: String? {
        guard let refRange = lab.referenceRange?.first else { return nil }
        let low = refRange.low?.value
        let high = refRange.high?.value
        let unit = refRange.low?.unit ?? refRange.high?.unit ?? ""
        if let low, let high {
            return "\(formatNum(low))-\(formatNum(high)) \(unit)".trimmingCharacters(in: .whitespaces)
        } else if let low {
            return ">\(formatNum(low)) \(unit)".trimmingCharacters(in: .whitespaces)
        } else if let high {
            return "<\(formatNum(high)) \(unit)".trimmingCharacters(in: .whitespaces)
        }
        if let text = refRange.text, text.count <= 30 {
            return text
        }
        return nil
    }

    private var interpretationText: String? {
        guard let raw = lab.interpretation?.first?.text ?? lab.interpretation?.first?.coding?.first?.display else { return nil }
        // Keep it short for the badge
        if raw.count > 12 {
            let lower = raw.lowercased()
            if lower.contains("high") { return "High" }
            if lower.contains("low") { return "Low" }
            if lower.contains("abnormal") { return "Abnormal" }
            if lower.contains("normal") { return "Normal" }
            return String(raw.prefix(12)) + "..."
        }
        return raw
    }

    private var performerName: String? {
        for p in lab.performer ?? [] {
            if case .practitioner(let doc) = p.resource {
                if let name = doc.name?.first {
                    let given = name.given?.joined(separator: " ") ?? ""
                    let family = name.family ?? ""
                    let parts = [given, family].filter { !$0.isEmpty }
                    if !parts.isEmpty { return parts.joined(separator: " ") }
                }
            }
            if let display = p.display, !display.isEmpty {
                return display
            }
        }
        return nil
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: onToggle) {
                HStack(alignment: .center, spacing: 10) {
                    Circle()
                        .fill(rangeIndicatorColor)
                        .frame(width: 10, height: 10)

                    VStack(alignment: .leading, spacing: 3) {
                        Text(testName)
                            .font(.subheadline)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .fixedSize(horizontal: false, vertical: true)
                            .multilineTextAlignment(.leading)

                        if let range = rangeText {
                            Text(range)
                                .font(.caption2)
                                .foregroundStyle(.tertiary)
                        }
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 3) {
                        if let value = valueText {
                            Text(value)
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundStyle(hasReferenceRange && !isInRange ? Color.red : Color.primary)
                        }

                        if let interp = interpretationText {
                            let interpColor = interpretationBadgeColor
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

                    Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                        .font(.caption2)
                        .fontWeight(.semibold)
                        .foregroundStyle(.tertiary)
                }
            }
            .buttonStyle(.plain)

            if isExpanded {
                Divider().padding(.top, 8)
                LabResultDetail(lab: lab, performerName: performerName, trendGroupCode: trendGroupCode)
                    .padding(.top, 6)
                    .padding(.leading, 20)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 6)
    }

    private var rangeIndicatorColor: Color {
        guard hasReferenceRange else { return Color.gray.opacity(0.3) }
        return isInRange ? Color.green : Color.red
    }

    private var interpretationBadgeColor: Color {
        let code = lab.interpretation?.first?.coding?.first?.code
        if let fhir = FHIRInterpretation(code: code) {
            return fhir.color
        }
        // Fallback to text-based matching
        let text = (interpretationText ?? "").lowercased()
        if text.contains("high") || text.contains("abnormal") { return .red }
        if text.contains("low") { return .orange }
        if text.contains("normal") { return .green }
        return .secondary
    }

    private func formatNum(_ v: Double) -> String {
        v.truncatingRemainder(dividingBy: 1) == 0 ? String(format: "%.0f", v) : String(format: "%.1f", v)
    }
}

// MARK: - Lab Result Detail (Expanded)

private struct LabResultDetail: View {
    let lab: BWell.Observation
    let performerName: String?
    let trendGroupCode: (id: String, coding: BWell.Coding)?

    private var hasAnyDetail: Bool {
        lab.valueQuantity?.value != nil
        || lab.valueString != nil
        || lab.component?.isEmpty == false
        || lab.referenceRange?.first != nil
        || lab.interpretation?.first != nil
        || performerName != nil
        || lab.status != nil
        || lab.effectiveDateTime != nil
        || lab.issued != nil
        || lab.note?.isEmpty == false
        || lab.text?.div != nil
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            if !hasAnyDetail {
                Text("No additional details available")
                    .font(.caption)
                    .foregroundStyle(.tertiary)
            }

            if let qty = lab.valueQuantity ?? lab.component?.first?.valueQuantity, let value = qty.value {
                detailRow("Value", "\(value) \(qty.unit ?? "")".trimmingCharacters(in: .whitespaces))
            } else if let str = lab.valueString {
                detailRow("Value", str)
            }

            if let refRange = lab.referenceRange?.first {
                if let text = refRange.text {
                    detailRow("Ref Range", text)
                } else if let low = refRange.low?.value, let high = refRange.high?.value {
                    let unit = refRange.low?.unit ?? refRange.high?.unit ?? ""
                    detailRow("Ref Range", "\(low) - \(high) \(unit)".trimmingCharacters(in: .whitespaces))
                }
            }

            if let interp = lab.interpretation?.first {
                detailRow("Interpretation", interp.text ?? interp.coding?.first?.display)
            }

            if let performer = performerName {
                detailRow("Performed By", performer)
            }

            detailRow("Status", lab.status?.capitalizingFirstLetter())
            detailRow("Collected", lab.effectiveDateTime?.dateFormatter())

            if let issued = lab.issued {
                detailRow("Reported", issued.dateFormatter())
            }

            // Components (e.g., blood pressure systolic/diastolic)
            if let components = lab.component, components.count > 1 {
                Divider().padding(.vertical, 4)
                Text("Components")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundStyle(.secondary)
                ForEach(components.indices, id: \.self) { i in
                    let comp = components[i]
                    let name = comp.code?.coding?.first?.display ?? comp.code?.text ?? "Component"
                    if let qty = comp.valueQuantity, let value = qty.value {
                        detailRow(name, "\(value) \(qty.unit ?? "")".trimmingCharacters(in: .whitespaces))
                    }
                }
            }

            if let notes = lab.note, !notes.isEmpty {
                Divider().padding(.vertical, 4)
                Text("Notes")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundStyle(.secondary)
                ForEach(notes.indices, id: \.self) { i in
                    if let text = notes[i].text {
                        Text(text)
                            .font(.caption)
                            .foregroundStyle(.primary)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                }
            }

            // Narrative text
            if let narrative = lab.text?.div {
                let plainText = narrative.stripHTML()
                if !plainText.isEmpty {
                    Divider().padding(.vertical, 4)
                    Text("Summary")
                        .font(.caption)
                        .fontWeight(.semibold)
                        .foregroundStyle(.secondary)
                    Text(plainText)
                        .font(.caption)
                        .foregroundStyle(.primary)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }

            // View Trend link (navigates to chart for this test type)
            if let group = trendGroupCode {
                Divider().padding(.vertical, 4)
                NavigationLink(value: AppView.healthGroupItems(
                    category: .labs,
                    groupCode: BWellHealthDataWrapper(group.id, group.coding)
                )) {
                    HStack(spacing: 6) {
                        Image(systemName: "chart.line.uptrend.xyaxis")
                        Text("View Trend")
                    }
                    .font(.caption)
                    .fontWeight(.medium)
                    .foregroundStyle(.bwellPurple)
                }
                .buttonStyle(.plain)
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

// MARK: - Observation Reference Range Helper

extension BWell.Observation {
    var isInReferenceRange: Bool {
        let qty = valueQuantity ?? component?.first?.valueQuantity
        guard let value = qty?.value else { return true }
        guard let refRange = referenceRange?.first else { return true }
        if let low = refRange.low?.value, value < low { return false }
        if let high = refRange.high?.value, value > high { return false }
        return true
    }
}
