//
//  HealthDataListView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 14/11/25.
//

import Foundation
import SwiftUI
import BWellSDK

// MARK: - Row Content Model

struct HealthDataRowContent {
    let title: String?
    let subtitle: String?
    let date: String?
    let value: String?
    let valueColor: Color?

    init(title: String?, subtitle: String? = nil, date: String? = nil, value: String? = nil, valueColor: Color? = nil) {
        self.title = title
        self.subtitle = subtitle
        self.date = date
        self.value = value
        self.valueColor = valueColor
    }
}

// MARK: - Group List View (Category → Groups)

struct HealthDataGroupListView<Group, ID: Hashable>: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @State private var isLoading = false
    @State private var hasFetched = false

    let groups: [Group]
    let id: KeyPath<Group, ID>
    let fetch: () async -> Void
    let rowContent: (Group) -> HealthDataRowContent
    let destination: (Group) -> AppView?

    var body: some View {
        List {
            if isLoading && groups.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !isLoading && groups.isEmpty && hasFetched {
                ContentUnavailableView("No Records Found",
                    systemImage: "tray",
                    description: Text("There are no records available in this category."))
            } else {
                ForEach(groups, id: id) { group in
                    if let dest = destination(group) {
                        NavigationLink(value: dest) {
                            HealthCardRow(content: rowContent(group))
                        }
                    } else {
                        HealthCardRow(content: rowContent(group))
                    }
                }
            }
        }
        .listStyle(.plain)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable {
            await fetchData()
        }
        .task {
            if groups.isEmpty && !hasFetched {
                await fetchData()
            }
        }
        .onAppear {
            clearHealthDataDetails()
        }
    }

    private func fetchData() async {
        isLoading = true
        await fetch()
        isLoading = false
        hasFetched = true
    }

    private func clearHealthDataDetails() {
        viewModel.allergyIntolerances = []
        viewModel.carePlans = []
        viewModel.conditions = []
        viewModel.immunizations = []
        viewModel.encounters = []
        viewModel.labs = []
        viewModel.medications = []
        viewModel.procedures = []
        viewModel.vitalSigns = []
    }
}

// MARK: - Observable Items Store

@MainActor
class ItemsStore<Entry>: ObservableObject {
    @Published var entries: [Entry] = []
    @Published var isLoading = false
    @Published var hasFetched = false

    func load(using fetch: () async -> [Entry]) async {
        guard !hasFetched else { return }
        isLoading = true
        let result = await fetch()
        entries = result
        isLoading = false
        hasFetched = true
        NSLog("[HealthData] ItemsStore loaded %d entries", result.count)
    }

    func reload(using fetch: () async -> [Entry]) async {
        isLoading = true
        let result = await fetch()
        entries = result
        isLoading = false
        NSLog("[HealthData] ItemsStore reloaded %d entries", result.count)
    }
}

// MARK: - Group Items View (Group → Individual Records with Collapsible Cards)

struct HealthDataGroupItemsView<Entry, ID: Hashable, Detail: View>: View {
    @StateObject private var store = ItemsStore<Entry>()
    @State private var expandedItems: Set<AnyHashable> = []

    let id: KeyPath<Entry, ID>
    let rowContent: (Entry) -> HealthDataRowContent
    let fetch: () async -> [Entry]
    let detailView: (Entry) -> Detail

    private var allExpanded: Bool {
        !store.entries.isEmpty && expandedItems.count == store.entries.count
    }

    var body: some View {
        List {
            if store.isLoading && store.entries.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !store.isLoading && store.entries.isEmpty && store.hasFetched {
                ContentUnavailableView("No Records",
                    systemImage: "doc.text",
                    description: Text("No individual records found in this group."))
            } else {
                // Expand/Collapse All header
                if store.entries.count > 1 {
                    HStack {
                        Text("\(store.entries.count) records")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                        Spacer()
                        Button(allExpanded ? "Collapse All" : "Expand All") {
                            withAnimation(.easeInOut(duration: 0.25)) {
                                if allExpanded {
                                    expandedItems.removeAll()
                                } else {
                                    expandedItems = Set(store.entries.map { AnyHashable($0[keyPath: id]) })
                                }
                            }
                        }
                        .font(.subheadline)
                        .fontWeight(.medium)
                    }
                    .listRowSeparator(.hidden)
                }

                ForEach(store.entries, id: id) { item in
                    let itemId = AnyHashable(item[keyPath: id])
                    let isExpanded = expandedItems.contains(itemId)

                    CollapsibleHealthCard(
                        content: rowContent(item),
                        isExpanded: isExpanded,
                        onToggle: {
                            withAnimation(.easeInOut(duration: 0.25)) {
                                if isExpanded {
                                    expandedItems.remove(itemId)
                                } else {
                                    expandedItems.insert(itemId)
                                }
                            }
                        },
                        detailContent: {
                            detailView(item)
                        }
                    )
                    .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                    .listRowSeparator(.hidden)
                }
            }
        }
        .listStyle(.plain)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable {
            expandedItems.removeAll()
            await store.reload(using: fetch)
        }
        .task {
            await store.load(using: fetch)
        }
    }
}

// MARK: - Collapsible Health Card

struct CollapsibleHealthCard<DetailContent: View>: View {
    let content: HealthDataRowContent
    let isExpanded: Bool
    let onToggle: () -> Void
    @ViewBuilder let detailContent: () -> DetailContent

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Summary row (always visible, tap to toggle)
            Button(action: onToggle) {
                HStack(alignment: .center, spacing: 12) {
                    HealthCardRow(content: content)

                    Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                        .font(.caption2)
                        .fontWeight(.semibold)
                        .foregroundStyle(.tertiary)
                        .animation(.easeInOut(duration: 0.2), value: isExpanded)
                }
            }
            .buttonStyle(.plain)

            // Expanded detail content
            if isExpanded {
                Divider()
                    .padding(.top, 10)

                detailContent()
                    .padding(.top, 8)
                    .transition(.opacity)
            }
        }
        .padding(.vertical, 4)
        .padding(.horizontal, 4)
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(isExpanded ? Color(.systemGray6).opacity(0.5) : Color.clear)
        )
    }
}

// MARK: - Health Card Row

struct HealthCardRow: View {
    let content: HealthDataRowContent

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(content.title?.capitalizingFirstLetter() ?? "Unknown")
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundStyle(.primary)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)

                if let subtitle = content.subtitle {
                    Text(subtitle)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }

                if let date = content.date {
                    Text(date.dateFormatter())
                        .font(.caption)
                        .foregroundStyle(.tertiary)
                }
            }

            Spacer()

            if let value = content.value, !value.isEmpty {
                Text(value)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundStyle(content.valueColor ?? .secondary)
            }
        }
    }
}

// MARK: - Skeleton Row

struct SkeletonRow: View {
    @State private var shimmer = false

    var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 8) {
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color(.systemGray5))
                    .frame(width: 180, height: 14)
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color(.systemGray6))
                    .frame(width: 120, height: 12)
            }
            Spacer()
            RoundedRectangle(cornerRadius: 4)
                .fill(Color(.systemGray5))
                .frame(width: 50, height: 14)
        }
        .opacity(shimmer ? 0.4 : 1.0)
        .animation(.easeInOut(duration: 1.0).repeatForever(autoreverses: true), value: shimmer)
        .onAppear { shimmer = true }
    }
}

// MARK: - Observation Items View with Chart Header

enum ObservationChartType {
    case line
    case bar
}

struct ObservationChartItemsView<ID: Hashable, Detail: View>: View {
    @StateObject private var store = ItemsStore<BWell.Observation>()
    @State private var expandedItems: Set<AnyHashable> = []

    let chartType: ObservationChartType
    let chartTitle: String
    let id: KeyPath<BWell.Observation, ID>
    let rowContent: (BWell.Observation) -> HealthDataRowContent
    let fetch: () async -> [BWell.Observation]
    let detailView: (BWell.Observation) -> Detail

    private var allExpanded: Bool {
        !store.entries.isEmpty && expandedItems.count == store.entries.count
    }

    var body: some View {
        List {
            if store.isLoading && store.entries.isEmpty {
                ForEach(0..<5, id: \.self) { _ in
                    SkeletonRow()
                }
                .listRowSeparator(.hidden)
            } else if !store.isLoading && store.entries.isEmpty && store.hasFetched {
                ContentUnavailableView("No Records",
                    systemImage: "doc.text",
                    description: Text("No individual records found in this group."))
            } else {
                // Chart header
                if store.entries.count >= 2 {
                    Section {
                        switch chartType {
                        case .line:
                            VitalSignChartView(observations: store.entries, title: chartTitle)
                        case .bar:
                            LabTrendChartView(observations: store.entries, title: chartTitle)
                        }
                    }
                    .listRowInsets(EdgeInsets(top: 8, leading: 0, bottom: 8, trailing: 0))
                    .listRowSeparator(.hidden)
                }

                // Latest result hero card
                if let latest = store.entries.sorted(by: { ($0.effectiveDateTime ?? "") > ($1.effectiveDateTime ?? "") }).first {
                    Section {
                        LatestResultCard(observation: latest)
                    }
                    .listRowInsets(EdgeInsets(top: 4, leading: 0, bottom: 4, trailing: 0))
                    .listRowSeparator(.hidden)
                }

                // Expand/Collapse All header
                if store.entries.count > 1 {
                    HStack {
                        Text("\(store.entries.count) records")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                        Spacer()
                        Button(allExpanded ? "Collapse All" : "Expand All") {
                            withAnimation(.easeInOut(duration: 0.25)) {
                                if allExpanded {
                                    expandedItems.removeAll()
                                } else {
                                    expandedItems = Set(store.entries.map { AnyHashable($0[keyPath: id]) })
                                }
                            }
                        }
                        .font(.subheadline)
                        .fontWeight(.medium)
                    }
                    .listRowSeparator(.hidden)
                }

                ForEach(store.entries, id: id) { item in
                    let itemId = AnyHashable(item[keyPath: id])
                    let isExpanded = expandedItems.contains(itemId)

                    CollapsibleHealthCard(
                        content: rowContent(item),
                        isExpanded: isExpanded,
                        onToggle: {
                            withAnimation(.easeInOut(duration: 0.25)) {
                                if isExpanded {
                                    expandedItems.remove(itemId)
                                } else {
                                    expandedItems.insert(itemId)
                                }
                            }
                        },
                        detailContent: {
                            detailView(item)
                        }
                    )
                    .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                    .listRowSeparator(.hidden)
                }
            }
        }
        .listStyle(.plain)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable {
            expandedItems.removeAll()
            await store.reload(using: fetch)
        }
        .task {
            await store.load(using: fetch)
        }
    }
}

// MARK: - Latest Result Card (hero card for observations)

struct LatestResultCard: View {
    let observation: BWell.Observation

    private var valueText: String? {
        let qty = observation.valueQuantity ?? observation.component?.first?.valueQuantity
        guard let qty, let value = qty.value else {
            return observation.valueString
        }
        let formatted = value.truncatingRemainder(dividingBy: 1) == 0
            ? String(format: "%.0f", value)
            : String(format: "%.1f", value)
        let unit = qty.unit ?? ""
        return "\(formatted) \(unit)".trimmingCharacters(in: .whitespaces)
    }

    private var isInRange: Bool {
        let qty = observation.valueQuantity ?? observation.component?.first?.valueQuantity
        guard let value = qty?.value else { return true }
        if let refRange = observation.referenceRange?.first {
            if let low = refRange.low?.value, value < low { return false }
            if let high = refRange.high?.value, value > high { return false }
        }
        return true
    }

    private var referenceRangeText: String? {
        guard let refRange = observation.referenceRange?.first else { return nil }
        if let text = refRange.text { return text }
        guard let low = refRange.low?.value, let high = refRange.high?.value else { return nil }
        let unit = refRange.low?.unit ?? refRange.high?.unit ?? ""
        return "Normal: \(formatNum(low)) – \(formatNum(high)) \(unit)".trimmingCharacters(in: .whitespaces)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("Latest Result")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundStyle(.secondary)
                Spacer()
                if let date = observation.effectiveDateTime {
                    Text(date.relativeDate())
                        .font(.caption)
                        .foregroundStyle(.tertiary)
                }
            }

            HStack(alignment: .firstTextBaseline, spacing: 8) {
                if let value = valueText {
                    Text(value)
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundStyle(isInRange ? Color.primary : Color.red)
                }

                Text(isInRange ? "In Range" : "Out of Range")
                    .font(.caption)
                    .fontWeight(.medium)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background((isInRange ? Color.green : Color.red).opacity(0.15))
                    .foregroundStyle(isInRange ? .green : .red)
                    .clipShape(Capsule())
            }

            if let rangeText = referenceRangeText {
                Text(rangeText)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
    }

    private func formatNum(_ v: Double) -> String {
        v.truncatingRemainder(dividingBy: 1) == 0 ? String(format: "%.0f", v) : String(format: "%.1f", v)
    }
}

// MARK: - Empty State (for standalone use)

struct EmptyHealthStateView: View {
    let icon: String
    let title: String
    let message: String

    var body: some View {
        ContentUnavailableView(title, systemImage: icon, description: Text(message))
    }
}
