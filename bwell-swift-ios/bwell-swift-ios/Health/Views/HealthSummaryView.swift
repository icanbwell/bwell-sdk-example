//
//  HealthSummaryView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//
import Foundation
import SwiftUI
import BWellSDK

struct HealthSummaryView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @EnvironmentObject private var viewModel: HealthSummaryViewModel

    var body: some View {
        List {
            ForEach(HealthDataSummaryModel.allCases) { item in
                NavigationLink(value: AppView.healthCategory(category: item)) {
                    HStack(spacing: 12) {
                        Image(systemName: item.icon)
                            .font(.title3)
                            .foregroundStyle(item.category.color)
                            .frame(width: 32, alignment: .center)

                        Text(item.title)
                            .font(.body)

                        Spacer()

                        if viewModel.isLoading {
                            ProgressView()
                        } else {
                            if let total = viewModel.healthSummary.first(where: {
                                $0.category == item.category
                            })?.total, total > 0 {
                                Text("\(total)")
                                    .font(.subheadline)
                                    .fontWeight(.medium)
                                    .foregroundStyle(.white)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 4)
                                    .background(item.category.color.opacity(0.8))
                                    .clipShape(Capsule())
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
            .listRowSeparator(.hidden)

            Section {
                NavigationLink(value: AppView.careTeams) {
                    HealthSummaryRow(icon: "person.3", title: "Care Teams", color: .teal)
                }
                NavigationLink(value: AppView.documentReferences) {
                    HealthSummaryRow(icon: "doc.text", title: "Documents", color: .indigo)
                }
                NavigationLink(value: AppView.goals) {
                    HealthSummaryRow(icon: "target", title: "Goals", color: .mint)
                }
                NavigationLink(value: AppView.diagnosticReports) {
                    HealthSummaryRow(icon: "doc.text.magnifyingglass", title: "Diagnostic Reports", color: .purple)
                }
                NavigationLink(value: AppView.devices) {
                    HealthSummaryRow(icon: "sensor", title: "Devices", color: .gray)
                }
            }
            .listRowSeparator(.hidden)
        }
        .padding(.top, 10)
        .navigationTitle("Health Summary")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarColorScheme(.dark, for: .navigationBar)

        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .listStyle(.plain)
        .listRowSeparator(.hidden)
        .refreshable {
            guard let sdk = sdkManager.sdk else { return }
            viewModel.healthSummary = []
            await viewModel.getHealthDataSummary(sdk: sdk)
        }
        .task {
            if viewModel.healthSummary.isEmpty {
                guard let sdk = sdkManager.sdk else { return }
                await viewModel.getHealthDataSummary(sdk: sdk)
            }
        }
    }
}

private struct HealthSummaryRow: View {
    let icon: String
    let title: String
    let color: Color

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundStyle(color)
                .frame(width: 32, alignment: .center)
            Text(title)
        }
        .padding(.vertical, 4)
    }
}

struct HealthSummaryDetailView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    var category: HealthDataSummaryModel

    var body: some View {
        category.view
    }
}

#Preview {
    NavigationStack {
        HealthSummaryView()
    }
    .environmentObject(NavigationRouter())
    .environmentObject(HealthSummaryViewModel())
    .environmentObject(SDKManager())
}
