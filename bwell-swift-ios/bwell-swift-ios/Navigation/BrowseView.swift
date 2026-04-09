//
//  BrowseView.swift
//  bwell-swift-ios
//
//  Health Records browse with 4 sections: Clinical, Care Management,
//  Providers & Devices, Financial.
//

import SwiftUI
import BWellSDK

struct HealthRecordsBrowseView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @EnvironmentObject private var healthSummaryViewModel: HealthSummaryViewModel

    var body: some View {
        List {
            // MARK: - Clinical
            Section("Clinical") {
                ForEach(HealthDataSummaryModel.allCases) { item in
                    NavigationLink(value: AppView.healthCategory(category: item)) {
                        BrowseRow(icon: item.icon, title: item.title, color: item.category.color)
                    }
                }
            }

            // MARK: - Care Management
            Section("Care Management") {
                NavigationLink(value: AppView.careTeams) {
                    BrowseRow(icon: "person.3", title: "Care Teams", color: .teal)
                }
                NavigationLink(value: AppView.goals) {
                    BrowseRow(icon: "target", title: "Goals", color: .mint)
                }
                NavigationLink(value: AppView.healthJourney) {
                    BrowseRow(icon: "checklist", title: "Health Journey", color: .bwellPurple)
                }
                NavigationLink(value: AppView.questionnaireResponses) {
                    BrowseRow(icon: "doc.questionmark", title: "Questionnaires", color: .indigo)
                }
            }

            // MARK: - Providers & Devices
            Section("Providers & Devices") {
                NavigationLink(value: AppView.diagnosticReports) {
                    BrowseRow(icon: "doc.text.magnifyingglass", title: "Diagnostic Reports", color: .purple)
                }
                NavigationLink(value: AppView.documentReferences) {
                    BrowseRow(icon: "doc.text", title: "Documents", color: .indigo)
                }
                NavigationLink(value: AppView.devices) {
                    BrowseRow(icon: "sensor", title: "Devices", color: .gray)
                }
                NavigationLink(value: AppView.providerResources) {
                    BrowseRow(icon: "person.text.rectangle", title: "Provider Resources", color: .bwellBlue)
                }
                NavigationLink(value: AppView.manageConnections) {
                    BrowseRow(icon: "rectangle.connected.to.line.below", title: "Connections", color: .bwellBlue)
                }
            }

            // MARK: - Financial
            Section("Financial") {
                NavigationLink(value: AppView.financial) {
                    BrowseRow(icon: "creditcard", title: "Coverage & Benefits", color: .bwellPurple)
                }
            }

            #if DEBUG
            // MARK: - Developer
            Section("Developer") {
                DeveloperStubRow(icon: "doc.zipper", title: "Get Binary", description: "getBinary — raw binary FHIR resource access")
                DeveloperStubRow(icon: "chevron.left.forwardslash.chevron.right", title: "Get FHIR", description: "getFhir — raw FHIR resource access")
            }
            #endif
        }
        .listStyle(.insetGrouped)
        .navigationTitle("Health Records")
        .navigationBarTitleDisplayMode(.large)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
    }
}

private struct DeveloperStubRow: View {
    let icon: String
    let title: String
    let description: String

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundStyle(.secondary)
                .frame(width: 32, alignment: .center)
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.body)
                Text(description)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
}

private struct BrowseRow: View {
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
                .font(.body)
        }
        .padding(.vertical, 4)
    }
}
