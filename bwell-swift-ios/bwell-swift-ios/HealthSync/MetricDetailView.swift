//
//  MetricDetailView.swift
//  bwell-swift-ios
//
//  One metric type's full history - pushed from a card in the Metrics grid.
//  Ported from ui-platform's mfe-devices DeviceHistoryContainer/
//  DeviceHistoryCard: an expandable row per record, collapsed showing
//  date+time+zone | source and the value, expanded revealing Category
//  (joined CodeableConcept display names), Sync Date (the record's
//  `issued` field - distinct from its `effectiveDateTime`), and "From
//  <source>". No pagination here (see file header on
//  MetricDetailViewModel) - a single generously-sized page is fetched once.
//

import BWellSDK
import SwiftUI

/// Navigation value for pushing to a metric's history - `groupCode` is the
/// same code getDeviceMetricsGroups() returned for this card, passed
/// straight through to getDeviceMetrics(groupCode:).
struct MetricDetailRoute: Hashable {
    let title: String
    let groupCode: String?
}

enum MetricDetailState {
    case loading
    case loaded([BWell.Observation])
    case empty(errorMessage: String?)
}

@MainActor
final class MetricDetailViewModel: ObservableObject {
    @Published var state: MetricDetailState = .loading

    // getDeviceMetrics has no paging-info in its response (confirmed against
    // BWellSDK's DeviceMetricResponse), so there's no reliable way to offer
    // a real "Load More" - one generously-sized page stands in for the RN
    // app's paginated history instead.
    private static let pageSize = 50

    func load(client: BWellClient, groupCode: String?) async {
        state = .loading
        do {
            let token = groupCode.map { BWell.SearchToken(value: .init(code: $0)) }
            let response = try await client.health.getDeviceMetrics(
                .init(page: 0, pageSize: Self.pageSize, groupCode: token)
            )
            let observations = (response.entry ?? []).compactMap(\.resource)
            state = observations.isEmpty ? .empty(errorMessage: nil) : .loaded(observations)
        } catch {
            state = .empty(errorMessage: "Couldn't load history: \(error.localizedDescription)")
        }
    }
}

struct MetricDetailView: View {
    let route: MetricDetailRoute

    @EnvironmentObject private var sdkManager: SDKManager
    @StateObject private var viewModel = MetricDetailViewModel()
    @State private var expandedIDs: Set<String> = []

    var body: some View {
        Group {
            switch viewModel.state {
            case .loading:
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            case .empty(let errorMessage):
                Text(errorMessage ?? "No history yet for this metric.")
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .loaded(let observations):
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(observations) { observation in
                            MetricHistoryRow(
                                observation: observation,
                                isExpanded: Binding(
                                    get: { expandedIDs.contains(observation.id) },
                                    set: { isExpanded in
                                        if isExpanded {
                                            expandedIDs.insert(observation.id)
                                        } else {
                                            expandedIDs.remove(observation.id)
                                        }
                                    }
                                )
                            )
                        }
                    }
                    .padding()
                }
            }
        }
        .background(Color(.systemGroupedBackground))
        .navigationTitle(route.title)
        .navigationBarTitleDisplayMode(.inline)
        .task {
            guard let client = sdkManager.sdk else { return }
            await viewModel.load(client: client, groupCode: route.groupCode)
        }
    }
}

// MARK: - History row

private struct MetricHistoryRow: View {
    let observation: BWell.Observation
    @Binding var isExpanded: Bool

    private static let ownerSecuritySystem = "https://www.icanbwell.com/owner"

    private var dateText: String {
        observation.effectiveDateTime?.dateTimeWithZoneFormatted() ?? "Unknown date"
    }

    private var sourceText: String? {
        observation.meta?.security?.first { $0.system == Self.ownerSecuritySystem }?.code
    }

    private var valueText: String? { formattedQuantity(observation.valueQuantity) }

    private var categoryText: String? {
        let names = (observation.category ?? []).compactMap { $0.text ?? $0.coding?.first?.display }
        return names.isEmpty ? nil : names.joined(separator: " | ")
    }

    private var syncDateText: String? { observation.issued?.dateTimeWithZoneFormatted() }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Button {
                withAnimation(.easeInOut(duration: 0.2)) { isExpanded.toggle() }
            } label: {
                HStack(alignment: .top, spacing: 12) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(sourceText.map { "\(dateText) | \($0)" } ?? dateText)
                            .font(.subheadline)
                            .foregroundStyle(.primary)
                            .multilineTextAlignment(.leading)
                        if let valueText {
                            Text(valueText)
                                .font(.subheadline).fontWeight(.semibold)
                                .foregroundStyle(.primary)
                        }
                    }
                    Spacer()
                    Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                        .foregroundStyle(.secondary)
                }
            }
            .buttonStyle(.plain)

            if isExpanded {
                VStack(alignment: .leading, spacing: 12) {
                    if let categoryText {
                        field(label: "Category", value: categoryText)
                    }
                    if let syncDateText {
                        field(label: "Sync Date", value: syncDateText)
                    }
                    if let sourceText {
                        HStack(spacing: 4) {
                            Image(systemName: "link")
                            Text("From \(sourceText)")
                        }
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                    }
                }
            }
        }
        .padding()
        .background(Color(.secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private func field(label: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.caption).foregroundStyle(.secondary)
            Text(value).font(.subheadline).fontWeight(.semibold).foregroundStyle(.primary)
        }
    }
}
