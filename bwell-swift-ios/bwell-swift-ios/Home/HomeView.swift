//
//  HomeView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 10/11/25.
//
import Foundation
import SwiftUI
import BWellSDK

struct HomeView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @StateObject private var viewModel = HomeViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Greeting
                VStack(alignment: .leading, spacing: 4) {
                    Text("Welcome, \(viewModel.firstName)!")
                        .font(.title)
                        .fontWeight(.bold)
                    Text(Date.now, format: .dateTime.weekday(.wide).month(.wide).day())
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }

                // Highlights Section
                if !viewModel.recentVitals.isEmpty || !viewModel.recentLabs.isEmpty || viewModel.recentEncounter != nil {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Highlights")
                            .font(.headline)

                        // Latest vital sign
                        if let vital = viewModel.recentVitals.first {
                            HealthHighlightCard(
                                icon: "waveform.path.ecg.rectangle",
                                title: vital.name,
                                value: vital.displayValue,
                                subtitle: vital.date,
                                color: .pink
                            )
                        }

                        // Out-of-range lab
                        if let lab = viewModel.flaggedLab {
                            HealthHighlightCard(
                                icon: "testtube.2",
                                title: lab.name,
                                value: lab.displayValue,
                                subtitle: lab.interpretation ?? "Review Needed",
                                color: .orange
                            )
                        }

                        // Recent encounter
                        if let encounter = viewModel.recentEncounter {
                            HealthHighlightCard(
                                icon: "person.2.wave.2",
                                title: "Last Visit",
                                value: encounter.typeName,
                                subtitle: encounter.date,
                                color: .bwellBlue
                            )
                        }
                    }
                }

                // Favorites with Sparklines
                if !viewModel.recentVitals.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Trends")
                            .font(.headline)

                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(viewModel.sparklineData, id: \.name) { series in
                                    SparklineCard(
                                        title: series.name,
                                        value: series.latestValue,
                                        unit: series.unit,
                                        dataPoints: series.values,
                                        color: series.color
                                    )
                                }
                            }
                        }
                    }
                }

                // Health at a glance
                if viewModel.healthSummaryCount > 0 {
                    VStack(alignment: .leading, spacing: 12) {
                        HStack {
                            Text("Health at a Glance")
                                .font(.headline)
                            Spacer()
                            NavigationLink(value: AppView.healthSummary) {
                                Text("View All")
                                    .font(.subheadline)
                                    .foregroundStyle(.bwellBlue)
                            }
                        }

                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                            ForEach(viewModel.summaryResources.filter { $0.total > 0 }, id: \.category) { resource in
                                HealthGlanceCard(
                                    count: resource.total,
                                    label: resource.category.displayName,
                                    icon: resource.category.icon,
                                    color: resource.category.color
                                )
                            }
                        }
                    }
                }

                // Health Journey / Tasks
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        HStack(spacing: 8) {
                            Image(systemName: "checklist")
                                .font(.title2)
                                .foregroundStyle(.bwellPurple)
                            Text("Your Tasks")
                                .font(.title3)
                                .fontWeight(.bold)
                        }
                        Spacer()
                        NavigationLink(value: AppView.healthJourney) {
                            Text("View All")
                                .font(.subheadline)
                                .foregroundStyle(.bwellBlue)
                        }
                    }

                    if viewModel.pendingTasks.isEmpty && !viewModel.isLoadingTasks {
                        HStack(spacing: 8) {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundStyle(.green)
                            Text("You're all caught up!")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                        .padding(.vertical, 8)
                    } else if viewModel.isLoadingTasks {
                        ProgressView()
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 8)
                    } else {
                        ForEach(viewModel.pendingTasks.prefix(3), id: \.id) { task in
                            HomeTaskRow(task: task)
                        }
                    }
                }

                // Quick Actions
                VStack(alignment: .leading, spacing: 12) {
                    Text("Quick Actions")
                        .font(.headline)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            QuickActionButton(icon: "magnifyingglass", title: "Find Provider", color: .bwellBlue, destination: .providerSearch)
                            QuickActionButton(icon: "rectangle.connected.to.line.below", title: "Connections", color: .teal, destination: .manageConnections)
                            QuickActionButton(icon: "creditcard", title: "Insurance", color: .bwellPurple, destination: .financial)
                            QuickActionButton(icon: "arrow.down.doc", title: "Export Data", color: .orange, destination: .accountSettings)
                        }
                    }
                }

                // Banners
                if !viewModel.hasConnections {
                    ActionBanner(
                        title: "Connect Your Records",
                        message: "Link your health providers to see all your data in one place.",
                        buttonTitle: "Get Started",
                        gradient: [.bwellBlue, .bwellPurple.opacity(0.8)],
                        destination: .manageConnections
                    )
                }

                if viewModel.healthMatchConsented == false {
                    ActionBanner(
                        title: "Enable Health Match",
                        message: "Get personalized health insights and recommendations.",
                        buttonTitle: "Learn More",
                        gradient: [.teal, .bwellGreen]
                    ) {
                        viewModel.showHealthMatchSheet = true
                    }
                }
            }
            .padding(.horizontal)
            .padding(.top, 8)
        }
        .navigationTitle("Home")
        .navigationBarTitleDisplayMode(.large)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable {
            if let sdk = sdkManager.sdk {
                viewModel.reset()
                viewModel.loadProfile(sdk: sdk)
                await viewModel.loadHomeData(sdk: sdk)
            }
        }
        .task {
            if let sdk = sdkManager.sdk {
                viewModel.loadProfile(sdk: sdk)
                await viewModel.loadHomeData(sdk: sdk)
            }
        }
        .sheet(isPresented: $viewModel.showHealthMatchSheet) {
            HealthMatchConsentView()
        }
    }
}

// MARK: - Home Task Row (from real SDK data)
private struct HomeTaskRow: View {
    let task: BWell.Task

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: taskIcon)
                .font(.body)
                .foregroundStyle(.bwellPurple)
                .frame(width: 32, height: 32)
                .background(Color.bwellPurple.opacity(0.1))
                .clipShape(RoundedRectangle(cornerRadius: 8))

            VStack(alignment: .leading, spacing: 2) {
                Text(homeTaskTitle(task))
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .lineLimit(1)
                if let status = task.status {
                    HStack(spacing: 4) {
                        Circle()
                            .fill(homeTaskStatusColor(status))
                            .frame(width: 6, height: 6)
                        Text(status.capitalized)
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                }
            }

            Spacer()

            if let priority = task.priority {
                Text(priority.capitalized)
                    .font(.caption2)
                    .fontWeight(.semibold)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(priorityColor(priority).opacity(0.15))
                    .foregroundStyle(priorityColor(priority))
                    .clipShape(Capsule())
            }
        }
        .padding(.vertical, 4)
    }

    private var taskIcon: String {
        let code = task.code?.coding?.first?.code?.lowercased() ?? ""
        let focusType = task.focus?.type?.lowercased() ?? ""
        if code.contains("medication") || focusType.contains("medication") { return "pills" }
        if code.contains("lab") || focusType.contains("observation") { return "testtube.2" }
        if code.contains("appointment") || focusType.contains("appointment") { return "calendar" }
        if code.contains("immunization") { return "syringe" }
        if focusType.contains("questionnaire") { return "doc.questionmark" }
        if focusType.contains("servicerequest") { return "cross.case" }
        return "checklist"
    }

    private func homeTaskTitle(_ task: BWell.Task) -> String {
        // Check identifier with activityTitle system (hp-facade enrichment)
        if let idents = task.identifier {
            for ident in idents {
                if let sys = ident.system, sys.contains("activityTitle"),
                   let val = ident.value, !val.isEmpty {
                    return val
                }
            }
        }
        if let t = task.code?.text, !t.isEmpty { return t }
        if let d = task.code?.coding?.first?.display, !d.isEmpty { return d }
        if let d = task.description, !d.isEmpty { return d }
        if let ft = task.focus?.type, !ft.isEmpty {
            let shortRef = task.focus?.reference?.components(separatedBy: "/").last ?? ""
            return !shortRef.isEmpty ? "\(ft): \(shortRef)" : ft
        }
        if let id = task.id { return "Task \(String(id.prefix(8)))..." }
        return "Task"
    }

    private func homeTaskStatusColor(_ status: String?) -> Color {
        switch status?.lowercased() {
        case "completed": return .green
        case "in-progress": return .blue
        case "ready": return .orange
        case "requested": return .purple
        case "cancelled", "failed": return .red
        default: return .gray
        }
    }

    private func priorityColor(_ priority: String?) -> Color {
        switch priority?.lowercased() {
        case "urgent", "asap", "stat": return .red
        case "routine": return .blue
        default: return .gray
        }
    }
}

// MARK: - Quick Action Button
private struct QuickActionButton: View {
    let icon: String
    let title: String
    let color: Color
    let destination: AppView

    var body: some View {
        NavigationLink(value: destination) {
            VStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundStyle(color)
                    .frame(width: 44, height: 44)
                    .background(color.opacity(0.1))
                    .clipShape(Circle())
                Text(title)
                    .font(.caption)
                    .foregroundStyle(.primary)
                    .lineLimit(1)
            }
            .frame(width: 80)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Health Highlight Card
struct HealthHighlightCard: View {
    let icon: String
    let title: String
    let value: String
    let subtitle: String
    let color: Color

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundStyle(color)
                .frame(width: 36)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                Text(value)
                    .font(.title3)
                    .fontWeight(.semibold)
            }

            Spacer()

            Text(subtitle)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding()
        .background(color.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

// MARK: - Sparkline Card
struct SparklineCard: View {
    let title: String
    let value: String
    let unit: String
    let dataPoints: [Double]
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.caption)
                .foregroundStyle(.secondary)
                .lineLimit(1)

            Text("\(value) \(unit)")
                .font(.headline)
                .fontWeight(.semibold)

            SparklineView(dataPoints: dataPoints, color: color)
                .frame(height: 32)
        }
        .padding(12)
        .frame(width: 140)
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// MARK: - Sparkline Mini Chart
struct SparklineView: View {
    let dataPoints: [Double]
    let color: Color

    var body: some View {
        GeometryReader { geometry in
            if dataPoints.count >= 2 {
                let minVal = dataPoints.min() ?? 0
                let maxVal = dataPoints.max() ?? 1
                let range = max(maxVal - minVal, 0.001)
                let width = geometry.size.width
                let height = geometry.size.height
                let stepX = width / CGFloat(dataPoints.count - 1)

                Path { path in
                    for (index, value) in dataPoints.enumerated() {
                        let x = stepX * CGFloat(index)
                        let y = height - ((CGFloat(value) - CGFloat(minVal)) / CGFloat(range)) * height
                        if index == 0 {
                            path.move(to: CGPoint(x: x, y: y))
                        } else {
                            path.addLine(to: CGPoint(x: x, y: y))
                        }
                    }
                }
                .stroke(color, style: StrokeStyle(lineWidth: 2, lineCap: .round, lineJoin: .round))
            } else if dataPoints.count == 1 {
                // Single point — show a dot in the center
                Circle()
                    .fill(color)
                    .frame(width: 6, height: 6)
                    .position(x: geometry.size.width / 2, y: geometry.size.height / 2)
            }
        }
    }
}

// MARK: - Health Glance Card
private struct HealthGlanceCard: View {
    let count: Int
    let label: String
    let icon: String
    let color: Color

    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .font(.caption)
                .foregroundStyle(color)
            Text("\(count)")
                .font(.title3)
                .fontWeight(.bold)
            Text(label)
                .font(.caption2)
                .foregroundStyle(.secondary)
                .lineLimit(1)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// MARK: - Action Banner (with NavigationLink or action)
private struct ActionBanner: View {
    let title: String
    let message: String
    let buttonTitle: String
    let gradient: [Color]
    var destination: AppView? = nil
    var action: (() -> Void)? = nil

    @State private var dismissed = false

    var body: some View {
        if !dismissed {
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text(title)
                        .font(.headline)
                        .foregroundStyle(.white)
                    Spacer()
                    Button { dismissed = true } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(.white.opacity(0.7))
                    }
                }

                Text(message)
                    .font(.subheadline)
                    .foregroundStyle(.white.opacity(0.9))

                if let destination {
                    NavigationLink(value: destination) {
                        bannerButton
                    }
                } else if let action {
                    Button(action: action) {
                        bannerButton
                    }
                }
            }
            .padding()
            .background(LinearGradient(colors: gradient, startPoint: .topLeading, endPoint: .bottomTrailing))
            .clipShape(RoundedRectangle(cornerRadius: 16))
        }
    }

    private var bannerButton: some View {
        Text(buttonTitle)
            .font(.subheadline)
            .fontWeight(.semibold)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(.white.opacity(0.2))
            .clipShape(Capsule())
            .foregroundStyle(.white)
    }
}

#Preview {
    NavigationStack {
        HomeView()
    }
    .environmentObject(SDKManager())
    .environmentObject(NavigationRouter())
    .environmentObject(HealthSummaryViewModel())
}
