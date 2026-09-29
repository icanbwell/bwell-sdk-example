//
//  HealthSyncDashboardView.swift
//  bwell-swift-ios
//
//  Shown instead of the raw Playground when an on-device adapter is
//  configured (BWellHealthSyncType.isConfigured) - a polished Metrics/Body
//  Score view for the data that adapter actually produces, plus the
//  Playground itself as a third tab for anyone who still wants the raw
//  test bench. With no adapter configured, HealthSyncPlaygroundRootView
//  skips this entirely and shows the Playground directly, unchanged.
//

import BWellSDK
import SwiftUI

struct HealthSyncDashboardView: View {
    let client: BWellClient

    @StateObject private var dashboardViewModel = HealthSyncDashboardViewModel()
    @StateObject private var playgroundViewModel = HealthSyncPlaygroundViewModel()
    @State private var selectedTab: Tab = .metrics

    private enum Tab: String, CaseIterable, Identifiable {
        case metrics = "Metrics"
        case bodyScore = "Body Score"
        case playground = "Playground"
        var id: String { rawValue }
    }

    var body: some View {
        VStack(spacing: 0) {
            Picker("", selection: $selectedTab) {
                ForEach(Tab.allCases) { tab in
                    Text(tab.rawValue).tag(tab)
                }
            }
            .pickerStyle(.segmented)
            .padding()

            switch selectedTab {
            case .metrics:
                HealthMetricsTabView(viewModel: dashboardViewModel)
            case .bodyScore:
                HealthBodyScoreTabView(viewModel: dashboardViewModel)
            case .playground:
                HealthSyncPlaygroundView(viewModel: playgroundViewModel)
            }
        }
        // One consistent background behind the picker and every tab's
        // content - without this, the picker's padding sits on the default
        // white background while each tab's own .systemGroupedBackground
        // starts right below it, showing as a thin seam at the boundary.
        .background(Color(.systemGroupedBackground))
        .onAppear {
            dashboardViewModel.attach(client: client)
            playgroundViewModel.attach(client: client)
        }
        .navigationDestination(for: MetricDetailRoute.self) { route in
            MetricDetailView(route: route)
        }
        .navigationDestination(for: BodySystemDetailRoute.self) { route in
            BodySystemDetailView(route: route)
        }
    }
}

// MARK: - Metrics tab

private struct HealthMetricsTabView: View {
    @ObservedObject var viewModel: HealthSyncDashboardViewModel

    var body: some View {
        switch viewModel.metricsState {
        case .loading:
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        case .empty(let errorMessage):
            SyncPromptView(progress: nil, errorMessage: errorMessage, action: viewModel.syncMetrics)
        case .syncing(let progress):
            SyncPromptView(progress: progress, errorMessage: nil, action: viewModel.syncMetrics)
        case .loaded(let groups):
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 24) {
                    ForEach(groupMetricsByCategory(groups)) { category in
                        VStack(alignment: .leading, spacing: 12) {
                            Text(category.label)
                                .font(.headline)
                                .foregroundStyle(.primary)
                            MetricCardGrid(items: category.items) { item in
                                NavigationLink(value: MetricDetailRoute(
                                    title: title(for: item.group),
                                    groupCode: item.group.coding?.code
                                )) {
                                    card(for: item.group)
                                }
                                .buttonStyle(CardPressButtonStyle())
                            }
                        }
                    }
                }
                .padding()
            }
            .refreshable { await viewModel.refreshMetrics() }
        }
    }

    private func title(for group: BWell.DeviceMetricsGroup) -> String {
        group.coding?.display ?? group.name ?? "Metric"
    }

    private func card(for group: BWell.DeviceMetricsGroup) -> some View {
        let parts = formattedQuantityParts(group.value?.valueQuantity)
        return MetricCardView(
            title: title(for: group),
            value: parts?.value,
            unit: parts?.unit,
            footer: group.effectiveDateTime.map { "Updated on \($0.dateFormatter())" }
        )
    }
}

// MARK: - Body Score tab

private struct HealthBodyScoreTabView: View {
    @ObservedObject var viewModel: HealthSyncDashboardViewModel

    var body: some View {
        switch viewModel.bodyScoreState {
        case .loading:
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        case .empty(let errorMessage):
            SyncPromptView(progress: nil, errorMessage: errorMessage, action: viewModel.syncBodyScore)
        case .syncing(let progress):
            SyncPromptView(progress: progress, errorMessage: nil, action: viewModel.syncBodyScore)
        case .loaded(let score):
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text("Health Score").font(.title2).fontWeight(.bold)

                    bioAgeCard(score)

                    if let trendDirection = score.trendDirection {
                        HealthTrendAlertCard(
                            label: "health",
                            trendDirection: trendDirection,
                            trendRate: score.trendRate,
                            periodStart: score.periodStart
                        )
                    }

                    Text("Body Systems").font(.headline)

                    VStack(spacing: 12) {
                        ForEach(bodySystemItems(score)) { item in
                            NavigationLink(value: BodySystemDetailRoute(
                                bodySystemId: item.system.bodySystemId ?? "",
                                title: item.system.title ?? "Body system"
                            )) {
                                bodySystemRow(item.system)
                            }
                            .buttonStyle(CardPressButtonStyle())
                        }
                    }

                    PeriodMetadataView(
                        calculationDate: score.calculationDate,
                        periodStart: score.periodStart,
                        periodEnd: score.periodEnd
                    )
                }
                .padding()
            }
            .refreshable { await viewModel.refreshBodyScore() }
        }
    }

    private func bodySystemItems(_ score: BWell.HealthScore) -> [BodySystemGridItem] {
        (score.bodySystems ?? []).enumerated().map { index, system in
            BodySystemGridItem(id: system.bodySystemId ?? "\(index)", system: system)
        }
    }

    private func bioAgeCard(_ score: BWell.HealthScore) -> some View {
        let grade = score.overallScore.map(HealthGrade.from)
        return HStack(spacing: 16) {
            VStack(alignment: .leading, spacing: 4) {
                Text("Biological Age").font(.subheadline).foregroundStyle(.secondary)
                HStack(alignment: .firstTextBaseline, spacing: 4) {
                    Text(score.bioAge.map { String(format: "%.0f", $0) } ?? "—")
                        .font(.system(size: 40, weight: .bold))
                    Text("years old").font(.caption).foregroundStyle(.secondary)
                }
                if let grade {
                    HealthGradeBadge(grade: grade)
                }
            }
            Spacer()
        }
        .padding()
        .frame(maxWidth: .infinity)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .strokeBorder(Color(.separator), lineWidth: 1)
        )
    }

    private func bodySystemRow(_ system: BWell.BodySystemSummary) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Image(systemName: HealthMetricIcon.symbolName(for: system.title))
                    .font(.footnote)
                    .frame(width: 32, height: 32)
                    .background(Color(.systemGray5))
                    .foregroundStyle(.primary)
                    .clipShape(Circle())
                Text(system.title ?? system.bodySystemId ?? "Body system")
                    .font(.subheadline).fontWeight(.semibold)
                    .foregroundStyle(.primary)
                Spacer()
                Image(systemName: "chevron.right")
                    .foregroundStyle(.tertiary)
            }
            if let score = system.score {
                HealthBarView(score: score)
            }
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color(.secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .healthCardBorderStyle()
    }
}

// MARK: - Shared "no data yet" / "syncing" prompt

/// `progress` non-nil means actively syncing. Redesigned to match
/// ui-platform's DeviceProcessingState: once sync() returns real per-type
/// record counts, a simulated processing gauge ticks (record-weighted
/// across resource types, see HealthSyncProcessing) instead of a bare
/// "Checking (n/30)" counter - only real data arrival (the poll this view
/// doesn't own) ever actually completes the wait.
private struct SyncPromptView: View {
    let progress: SyncProgress?
    let errorMessage: String?
    let action: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            if let progress {
                // "Syncing with b.well…" and the gauge container appear the
                // instant sync starts - only the gauge's own content morphs
                // from an indeterminate spinner to the numbered fill once
                // sync() actually returns real per-type counts, instead of
                // swapping to a visually distinct "reading" screen first.
                Text("Syncing with b.well…").font(.headline)
                if let processingData = progress.processingData {
                    ProcessingGaugeView(data: processingData)
                } else {
                    IndeterminateGaugeView()
                    Text("Reading from your device…")
                        .font(.subheadline).foregroundStyle(.secondary)
                }
            } else {
                Image(systemName: "arrow.triangle.2.circlepath")
                    .font(.largeTitle)
                    .foregroundStyle(.secondary)
                Text(errorMessage != nil ? "Sync incomplete" : "No data yet")
                    .font(.headline)
                if let errorMessage {
                    Text(errorMessage)
                        .font(.caption).foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                }
                Button("Sync with b.well", action: action)
                    .buttonStyle(.playgroundPrimary)
                    .frame(maxWidth: 240)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding()
    }
}

/// Ticks a 500ms clock against `data` and renders the current simulated
/// processing state - the filling circle gauge (record total centered) plus
/// the current resource type's "processing…"/"processed ✓" line. Ported
/// from ui-platform's ProcessingFillGauge/DeviceProcessingState (the
/// animated wave crest is simplified away; the record-weighted timing logic
/// it visualizes is the part that matters here).
private struct ProcessingGaugeView: View {
    let data: ProcessingData

    @State private var now = Date()

    private static let tickInterval: TimeInterval = 0.5

    var body: some View {
        let progress = HealthSyncProcessing.computeProgress(data, now: now)

        Group {
            switch progress {
            case .inactive:
                Text("Waiting for b.well to process your data…")
                    .font(.subheadline).foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                ProgressView()
            case .active(let total, let percent, let currentLabel, let currentState):
                FillGauge(percent: percent, total: total)
                HStack(spacing: 6) {
                    Text("\(currentLabel) \(currentState == .processed ? "processed" : "processing")\(currentState == .processed ? "" : "…")")
                        .font(.subheadline).foregroundStyle(.secondary)
                    if currentState == .processed {
                        Image(systemName: "checkmark")
                            .font(.caption.bold())
                            .foregroundStyle(Color(red: 0.07, green: 0.46, blue: 0.42))
                    }
                }
            }
        }
        .onReceive(Timer.publish(every: Self.tickInterval, on: .main, in: .common).autoconnect()) { now = $0 }
    }
}

/// The "filling circle" gauge: a soft circle that fills bottom-up with green
/// as `percent` rises, with `total` centered.
private struct FillGauge: View {
    let percent: Int
    let total: Int

    var body: some View {
        ZStack {
            Circle()
                .fill(
                    RadialGradient(
                        colors: [.white, Color(red: 0.93, green: 0.95, blue: 0.94)],
                        center: .top, startRadius: 1, endRadius: 90
                    )
                )
            GeometryReader { geometry in
                Rectangle()
                    .fill(
                        LinearGradient(
                            colors: [Color(red: 0.07, green: 0.72, blue: 0.42), Color(red: 0.82, green: 0.98, blue: 0.88)],
                            startPoint: .top, endPoint: .bottom
                        )
                    )
                    .frame(height: geometry.size.height * CGFloat(percent) / 100)
                    .frame(maxHeight: .infinity, alignment: .bottom)
            }
            .clipShape(Circle())
            .animation(.easeOut(duration: 0.7), value: percent)
            Text("\(total)")
                .font(.system(size: 28, weight: .semibold))
                .foregroundStyle(Color(red: 0.02, green: 0.47, blue: 0.28))
        }
        .frame(width: 120, height: 120)
        .clipShape(Circle())
        .shadow(color: .black.opacity(0.18), radius: 14, x: 0, y: 8)
    }
}

/// Same size/shape/shadow as FillGauge above, shown before sync() has
/// returned real per-type counts - keeps the sync screen's layout
/// continuous (title + circle + subtext) instead of jump-cutting to a
/// visually distinct "reading from device" screen.
private struct IndeterminateGaugeView: View {
    var body: some View {
        ZStack {
            Circle()
                .fill(
                    RadialGradient(
                        colors: [.white, Color(red: 0.93, green: 0.95, blue: 0.94)],
                        center: .top, startRadius: 1, endRadius: 90
                    )
                )
            ProgressView()
        }
        .frame(width: 120, height: 120)
        .clipShape(Circle())
        .shadow(color: .black.opacity(0.18), radius: 14, x: 0, y: 8)
    }
}

// MARK: - Formatting

/// Shared by the Metrics/Body Score tabs and their detail pages.
func formattedQuantity(_ quantity: BWell.Quantity?) -> String? {
    guard let parts = formattedQuantityParts(quantity) else { return nil }
    return "\(parts.value) \(parts.unit)".trimmingCharacters(in: .whitespaces)
}

/// Same rounding rule as formattedQuantity, but value/unit kept separate -
/// the Metrics grid card renders the unit smaller, below the value.
func formattedQuantityParts(_ quantity: BWell.Quantity?) -> (value: String, unit: String)? {
    guard let quantity, let value = quantity.value else { return nil }
    let formatted = value.truncatingRemainder(dividingBy: 1) == 0
        ? String(format: "%.0f", value)
        : String(format: "%.1f", value)
    return (value: formatted, unit: quantity.unit ?? "")
}
