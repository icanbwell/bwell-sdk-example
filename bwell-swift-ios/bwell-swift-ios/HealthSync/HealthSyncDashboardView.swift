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
        .onAppear {
            dashboardViewModel.attach(client: client)
            playgroundViewModel.attach(client: client)
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
            // DeviceMetricsGroup.id is optional (server-provided FHIR id, not
            // guaranteed) - index-based identity is fine for a read-only list.
            List(Array(groups.enumerated()), id: \.offset) { _, group in
                HealthCardRow(content: rowContent(for: group))
            }
            .listStyle(.plain)
            .refreshable { await viewModel.refreshMetrics() }
        }
    }

    private func rowContent(for group: BWell.DeviceMetricsGroup) -> HealthDataRowContent {
        HealthDataRowContent(
            title: group.coding?.display ?? group.name ?? "Metric",
            subtitle: group.sourceDisplay?.first,
            date: group.effectiveDateTime,
            value: formattedQuantity(group.value?.valueQuantity)
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
                VStack(spacing: 16) {
                    bioAgeCard(score)
                    ForEach(Array((score.bodySystems ?? []).enumerated()), id: \.offset) { _, system in
                        HealthCardRow(content: HealthDataRowContent(
                            title: system.title ?? system.bodySystemId ?? "Body system",
                            subtitle: system.grade.map { "Grade \($0)" },
                            value: system.score.map { String(format: "%.0f", $0) }
                        ))
                        .padding()
                        .background(Color(.secondarySystemGroupedBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                }
                .padding()
            }
            .refreshable { await viewModel.refreshBodyScore() }
        }
    }

    private func bioAgeCard(_ score: BWell.HealthScore) -> some View {
        VStack(spacing: 8) {
            Text("Biological Age").font(.subheadline).foregroundStyle(.secondary)
            Text(score.bioAge.map { String(format: "%.0f", $0) } ?? "—")
                .font(.system(size: 44, weight: .bold))
            if let actualAge = score.actualAge, let bioAge = score.bioAge {
                Text(bioAge <= actualAge ? "Younger than actual age (\(Int(actualAge)))" : "Older than actual age (\(Int(actualAge)))")
                    .font(.caption).foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
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
                if let processingData = progress.processingData {
                    Text("Syncing with b.well…").font(.headline)
                    ProcessingGaugeView(data: processingData)
                } else {
                    Image(systemName: "arrow.triangle.2.circlepath")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)
                    Text("Syncing with b.well…").font(.headline)
                    Text("Reading from your device…")
                        .font(.subheadline).foregroundStyle(.secondary)
                    ProgressView()
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
    }
}

// MARK: - Formatting

private func formattedQuantity(_ quantity: BWell.Quantity?) -> String? {
    guard let quantity, let value = quantity.value else { return nil }
    let formatted = value.truncatingRemainder(dividingBy: 1) == 0
        ? String(format: "%.0f", value)
        : String(format: "%.1f", value)
    return "\(formatted) \(quantity.unit ?? "")".trimmingCharacters(in: .whitespaces)
}
