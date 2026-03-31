//
//  FinancialView.swift
//  bwell-swift-ios
//
//  Demonstrates FinancialManager: coverages and explanation of benefits.
//

import SwiftUI
import BWellSDK

struct FinancialView: View {
    @StateObject private var viewModel = FinancialViewModel()
    @Binding var showMenu: Bool

    var body: some View {
        List {
            // MARK: - Coverages Section
            Section(header: Text("Insurance Coverages")) {
                if viewModel.coverages.isEmpty && !viewModel.isLoading {
                    Text("No coverage records found")
                        .foregroundColor(.secondary)
                        .font(.subheadline)
                } else {
                    ForEach(viewModel.coverages, id: \.id) { coverage in
                        CoverageRowView(coverage: coverage)
                    }
                }
            }

            // MARK: - EOB Section
            Section(header: Text("Explanation of Benefits")) {
                if viewModel.explanationOfBenefits.isEmpty && !viewModel.isLoading {
                    Text("No explanation of benefit records found")
                        .foregroundColor(.secondary)
                        .font(.subheadline)
                } else {
                    ForEach(viewModel.explanationOfBenefits, id: \.id) { eob in
                        EOBRowView(eob: eob)
                    }
                }
            }

            if let error = viewModel.errorMessage {
                Section {
                    Text(error)
                        .foregroundColor(.red)
                        .font(.caption)
                }
            }
        }
        .bwellNavigationBar(showMenu: $showMenu, navigationTitle: "Financial")
        .listStyle(.plain)
        .overlay {
            if viewModel.isLoading {
                ProgressView("Loading financial data...")
            }
        }
        .task {
            if viewModel.coverages.isEmpty {
                await viewModel.getCoverages()
            }
            if viewModel.explanationOfBenefits.isEmpty {
                await viewModel.getExplanationOfBenefits()
            }
        }
    }
}

// MARK: - Coverage Row
private struct CoverageRowView: View {
    let coverage: BWell.Coverage

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(coverage.type?.text ?? "Unknown Coverage")
                .font(.headline)

            if let status = coverage.status {
                Text("Status: \(status)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            if let subscriberId = coverage.subscriberId {
                Text("Subscriber ID: \(subscriberId)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            if let period = coverage.period {
                let start = period.start ?? ""
                let end = period.end ?? ""
                if !start.isEmpty || !end.isEmpty {
                    Text("Period: \(start) - \(end)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding(.vertical, 4)
    }
}

// MARK: - EOB Row
private struct EOBRowView: View {
    let eob: BWell.ExplanationOfBenefit

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(eob.type?.text ?? "Explanation of Benefit")
                .font(.headline)

            if let status = eob.status {
                Text("Status: \(status)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            if let use = eob.use {
                Text("Use: \(use)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            if let period = eob.billablePeriod {
                let start = period.start ?? ""
                let end = period.end ?? ""
                if !start.isEmpty || !end.isEmpty {
                    Text("Billable Period: \(start) - \(end)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }

            if let created = eob.created {
                Text("Created: \(created)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
}
