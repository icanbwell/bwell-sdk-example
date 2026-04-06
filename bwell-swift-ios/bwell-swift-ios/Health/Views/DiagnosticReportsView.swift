//
//  DiagnosticReportsView.swift
//  bwell-swift-ios
//
//  Displays diagnostic reports using sdk.health.getDiagnosticReportLabGroups()
//  and sdk.health.getDiagnosticReports().
//

import SwiftUI
import BWellSDK

struct DiagnosticReportsView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var reports: [BWell.DiagnosticReport] = []
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        ZStack {
            if isLoading && reports.isEmpty {
                ProgressView("Loading diagnostic reports...")
            } else if let error = errorMessage {
                ContentUnavailableView("Error", systemImage: "exclamationmark.triangle", description: Text(error))
            } else if reports.isEmpty {
                ContentUnavailableView("No Reports", systemImage: "doc.text.magnifyingglass", description: Text("No diagnostic reports found."))
            } else {
                List(reports, id: \.id) { report in
                    VStack(alignment: .leading, spacing: 8) {
                        Text(report.code?.text ?? report.code?.coding?.first?.display ?? "Diagnostic Report")
                            .font(.headline)

                        if let status = report.status {
                            Text(status)
                                .font(.caption)
                                .fontWeight(.medium)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(reportStatusColor(status).opacity(0.15))
                                .foregroundStyle(reportStatusColor(status))
                                .clipShape(Capsule())
                        }

                        if let category = report.category?.first?.text ?? report.category?.first?.coding?.first?.display {
                            HStack(spacing: 4) {
                                Text("Category:")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                Text(category)
                                    .font(.caption)
                            }
                        }

                        if let effectiveDate = report.effectiveDateTime {
                            Text("Date: \(effectiveDate.dateFormatter())")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }

                        if let issued = report.issued {
                            Text("Issued: \(issued.dateFormatter())")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }

                        if let performers = report.performer, !performers.isEmpty {
                            let names = performers.compactMap { $0.display }
                            if !names.isEmpty {
                                HStack(spacing: 4) {
                                    Text("Performer:")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                    Text(names.joined(separator: ", "))
                                        .font(.caption)
                                }
                            }
                        }

                        if let results = report.result, !results.isEmpty {
                            let resultNames = results.compactMap { $0.display }
                            if !resultNames.isEmpty {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("Results:")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                    ForEach(resultNames, id: \.self) { name in
                                        HStack(spacing: 4) {
                                            Text("•")
                                            Text(name)
                                        }
                                        .font(.caption)
                                    }
                                }
                            }
                        }
                    }
                    .padding(.vertical, 4)
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

    private func reportStatusColor(_ status: String) -> Color {
        switch status.lowercased() {
        case "final": return .green
        case "preliminary": return .orange
        case "cancelled": return .red
        case "registered": return .blue
        default: return .gray
        }
    }
}
