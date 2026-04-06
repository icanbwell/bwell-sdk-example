//
//  FinancialView.swift
//  bwell-swift-ios
//
//  Created on 3/31/26.
//

import SwiftUI
import BWellSDK

struct FinancialView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @StateObject private var viewModel = FinancialViewModel()

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                if viewModel.isLoading {
                    ProgressView("Loading insurance information...")
                        .padding()
                } else if let errorMessage = viewModel.errorMessage {
                    ErrorMessageView(message: errorMessage)
                        .padding()
                } else {
                    // Coverage Section
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Coverage")
                            .font(.title2)
                            .fontWeight(.bold)
                            .padding(.horizontal)

                        if viewModel.coverages.isEmpty {
                            EmptyStateView(
                                message: "No coverage information available",
                                systemImage: "shield.lefthalf.filled"
                            )
                        } else {
                            ForEach(viewModel.coverages, id: \.id) { coverage in
                                CoverageCardView(coverage: coverage)
                                    .padding(.horizontal)
                            }
                        }
                    }

                    Divider()
                        .padding(.vertical, 8)

                    // Explanation of Benefits Section
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Explanation of Benefits")
                            .font(.title2)
                            .fontWeight(.bold)
                            .padding(.horizontal)

                        if viewModel.explanationOfBenefits.isEmpty {
                            EmptyStateView(
                                message: "No explanation of benefits available",
                                systemImage: "doc.text"
                            )
                        } else {
                            ForEach(viewModel.explanationOfBenefits, id: \.id) { eob in
                                EOBCardView(eob: eob)
                                    .padding(.horizontal)
                            }
                        }
                    }
                }
            }
            .padding(.vertical)
        }
        .navigationTitle("Insurance")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .task {
            if let sdk = sdkManager.sdk {
                await viewModel.getCoverages(sdk: sdk)
                await viewModel.getExplanationOfBenefits(sdk: sdk)
            }
        }
    }
}

// MARK: - Coverage Card View
struct CoverageCardView: View {
    let coverage: BWell.Coverage

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header: Status and Type
            HStack {
                if let status = coverage.status {
                    StatusBadge(status: status, color: statusColor(for: status))
                }
                Spacer()
                if let type = coverage.type?.coding?.first?.display {
                    Text(type)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.bwellPurple)
                }
            }

            // Subscriber ID
            if let subscriberId = coverage.subscriberId {
                InfoRow(label: "Subscriber ID", value: subscriberId)
            }

            // Beneficiary
            if let beneficiary = coverage.beneficiary?.display {
                InfoRow(label: "Beneficiary", value: beneficiary)
            }

            // Payor
            if let payor = coverage.payor?.first?.display {
                InfoRow(label: "Payor", value: payor)
            }

            // Coverage Period
            if let period = coverage.period {
                HStack {
                    Text("Period:")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    Spacer()
                    VStack(alignment: .trailing, spacing: 4) {
                        if let start = period.start {
                            Text("Start: \(formatDate(start))")
                                .font(.subheadline)
                        }
                        if let end = period.end {
                            Text("End: \(formatDate(end))")
                                .font(.subheadline)
                        }
                    }
                }
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }

    private func statusColor(for status: String) -> Color {
        switch status.lowercased() {
        case "active":
            return .bwellGreen
        case "cancelled":
            return .bwellRed
        default:
            return .gray
        }
    }

    private func formatDate(_ dateString: String) -> String {
        let formatter = ISO8601DateFormatter()
        if let date = formatter.date(from: dateString) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateStyle = .medium
            return displayFormatter.string(from: date)
        }
        return dateString
    }
}

// MARK: - EOB Card View
struct EOBCardView: View {
    let eob: BWell.ExplanationOfBenefit

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header: Type and Status
            HStack {
                if let type = eob.type?.coding?.first?.display {
                    Text(type)
                        .font(.headline)
                        .foregroundColor(.bwellPurple)
                }
                Spacer()
                if let status = eob.status {
                    StatusBadge(status: status, color: statusColor(for: status))
                }
            }

            // Created Date
            if let created = eob.created {
                InfoRow(label: "Date", value: formatDate(created))
            }

            // Provider
            if let provider = eob.provider?.display {
                InfoRow(label: "Provider", value: provider)
            }

            // Insurer
            if let insurer = eob.insurer?.display {
                InfoRow(label: "Insurer", value: insurer)
            }

            // Totals
            if let totals = eob.total, !totals.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Totals:")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.secondary)

                    ForEach(totals.indices, id: \.self) { index in
                        let total = totals[index]
                        HStack {
                            if let category = total.category?.coding?.first?.display {
                                Text(category)
                                    .font(.subheadline)
                            }
                            Spacer()
                            if let amount = total.amount {
                                Text(formatAmount(amount))
                                    .font(.subheadline)
                                    .fontWeight(.semibold)
                                    .foregroundColor(.bwellBlue)
                            }
                        }
                    }
                }
                .padding(.top, 4)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
    }

    private func statusColor(for status: String) -> Color {
        switch status.lowercased() {
        case "active":
            return .bwellGreen
        case "cancelled":
            return .bwellRed
        default:
            return .gray
        }
    }

    private func formatDate(_ dateString: String) -> String {
        let formatter = ISO8601DateFormatter()
        if let date = formatter.date(from: dateString) {
            let displayFormatter = DateFormatter()
            displayFormatter.dateStyle = .medium
            return displayFormatter.string(from: date)
        }
        return dateString
    }

    private func formatAmount(_ amount: BWell.Money) -> String {
        if let value = amount.value, let currency = amount.currency {
            return "\(currency) \(String(format: "%.2f", value))"
        } else if let value = amount.value {
            return String(format: "%.2f", value)
        }
        return "N/A"
    }
}

// MARK: - Supporting Views
struct StatusBadge: View {
    let status: String
    let color: Color

    var body: some View {
        Text(status.capitalized)
            .font(.caption)
            .fontWeight(.semibold)
            .foregroundColor(.white)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(color)
            .cornerRadius(8)
    }
}

struct InfoRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text("\(label):")
                .font(.subheadline)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
                .font(.subheadline)
                .multilineTextAlignment(.trailing)
        }
    }
}

struct EmptyStateView: View {
    let message: String
    let systemImage: String

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: systemImage)
                .font(.system(size: 48))
                .foregroundColor(.gray)
            Text(message)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding()
        .frame(maxWidth: .infinity)
    }
}

struct ErrorMessageView: View {
    let message: String

    var body: some View {
        HStack {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(.bwellRed)
            Text(message)
                .font(.subheadline)
                .foregroundColor(.bwellRed)
        }
        .padding()
        .background(Color.bwellRed.opacity(0.1))
        .cornerRadius(8)
    }
}
