//
//  DiagnosticReportsView.swift
//  bwell-swift-ios
//
//  Displays diagnostic reports using sdk.health.getDiagnosticReports().
//

import SwiftUI
import BWellSDK

struct DiagnosticReportsView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var reports: [BWell.DiagnosticReport] = []
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var expandedIds: Set<String> = []

    var body: some View {
        ZStack {
            if isLoading && reports.isEmpty {
                ProgressView("Loading diagnostic reports...")
            } else if let error = errorMessage {
                ContentUnavailableView {
                    Label("Error", systemImage: "exclamationmark.triangle")
                } description: {
                    Text(error)
                } actions: {
                    Button("Retry") { Task { await loadReports() } }
                }
            } else if reports.isEmpty {
                ContentUnavailableView("No Reports", systemImage: "doc.text.magnifyingglass", description: Text("No diagnostic reports found."))
            } else {
                List(reports, id: \.id) { report in
                    reportRow(report)
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Diagnostic Reports")
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable {
            await loadReports()
        }
        .task {
            guard reports.isEmpty else { return }
            await loadReports()
        }
    }

    @ViewBuilder
    private func reportRow(_ report: BWell.DiagnosticReport) -> some View {
        let id = report.id ?? UUID().uuidString
        let isExpanded = expandedIds.contains(id)
        let resultCount = report.result?.count ?? 0

        VStack(alignment: .leading, spacing: 0) {
            Button(action: {
                withAnimation(.easeInOut(duration: 0.25)) {
                    if expandedIds.contains(id) { expandedIds.remove(id) }
                    else { expandedIds.insert(id) }
                }
            }) {
                HStack(alignment: .center, spacing: 10) {
                    Image(systemName: "doc.text")
                        .foregroundStyle(FHIRDiagnosticReportStatus(rawStatus: report.status)?.color ?? .gray)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 3) {
                        Text(report.code?.text ?? report.code?.coding?.first?.display ?? "Diagnostic Report")
                            .font(.subheadline)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .fixedSize(horizontal: false, vertical: true)

                        HStack(spacing: 8) {
                            if let date = report.effectiveDateTime {
                                Text(date.dateFormatter())
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                            if resultCount > 0 {
                                Text("\(resultCount) result\(resultCount == 1 ? "" : "s")")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }

                    Spacer()

                    if let status = report.status {
                        let color = FHIRDiagnosticReportStatus(rawStatus: status)?.color ?? .gray
                        Text(status.capitalized)
                            .font(.caption2)
                            .fontWeight(.medium)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(color.opacity(0.15))
                            .foregroundStyle(color)
                            .clipShape(Capsule())
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
                reportDetail(report)
                    .padding(.top, 6)
                    .padding(.leading, 38)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 6)
    }

    @ViewBuilder
    private func reportDetail(_ report: BWell.DiagnosticReport) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            if let category = report.category?.first?.text ?? report.category?.first?.coding?.first?.display {
                detailRow("Category", category)
            }
            if let issued = report.issued {
                detailRow("Issued", issued.dateFormatter())
            }
            if let performers = report.performer, !performers.isEmpty {
                let names = performers.compactMap { $0.display }
                if !names.isEmpty {
                    detailRow("Performer", names.joined(separator: ", "))
                }
            }
            if let results = report.result, !results.isEmpty {
                let resultNames = results.compactMap { $0.display }
                if !resultNames.isEmpty {
                    Divider().padding(.vertical, 4)
                    Text("Results")
                        .font(.caption)
                        .fontWeight(.semibold)
                        .foregroundStyle(.secondary)
                    ForEach(resultNames, id: \.self) { name in
                        HStack(alignment: .top, spacing: 6) {
                            Text("•")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            Text(name)
                                .font(.caption)
                                .foregroundStyle(.primary)
                                .fixedSize(horizontal: false, vertical: true)
                        }
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func detailRow(_ label: String, _ value: String) -> some View {
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

    private func loadReports() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        errorMessage = nil
        do {
            let request = BWell.DiagnosticReportRequest(page: 0)
            let response = try await sdk.health.getDiagnosticReports(request)
            reports = response.entry?.compactMap { $0.resource } ?? []
        } catch {
            errorMessage = "Failed to load diagnostic reports."
        }
        isLoading = false
    }

}
