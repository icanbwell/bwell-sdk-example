//
//  MainTabView.swift
//  bwell-swift-ios
//
//  Bottom TabView with 4 tabs per UX spec.
//

import SwiftUI
import BWellSDK

enum Tab: Int, Hashable {
    case home
    case healthRecords
    case findCare
    case profile
}

struct MainTabView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @EnvironmentObject private var healthSummaryViewModel: HealthSummaryViewModel

    @State private var selectedTab: Tab = .home

    // Per-tab navigation paths
    @State private var homePath = NavigationPath()
    @State private var healthRecordsPath = NavigationPath()
    @State private var findCarePath = NavigationPath()
    @State private var profilePath = NavigationPath()

    var body: some View {
        TabView(selection: $selectedTab) {
            // MARK: - Home Tab
            NavigationStack(path: $homePath) {
                HomeView()
                    .navigationDestination(for: AppView.self) { destination in
                        destinationView(destination)
                    }
            }
            .tabItem {
                Label("Home", systemImage: "house.fill")
            }
            .tag(Tab.home)

            // MARK: - Health Records Tab
            NavigationStack(path: $healthRecordsPath) {
                HealthRecordsBrowseView()
                    .navigationDestination(for: AppView.self) { destination in
                        destinationView(destination)
                    }
            }
            .tabItem {
                Label("Health Records", systemImage: "heart.text.square.fill")
            }
            .tag(Tab.healthRecords)

            // MARK: - Find Care Tab
            NavigationStack(path: $findCarePath) {
                ProviderSearchView()
                    .navigationDestination(for: AppView.self) { destination in
                        destinationView(destination)
                    }
            }
            .tabItem {
                Label("Find Care", systemImage: "magnifyingglass")
            }
            .tag(Tab.findCare)

            // MARK: - Profile Tab
            NavigationStack(path: $profilePath) {
                ProfileView()
                    .navigationDestination(for: AppView.self) { destination in
                        destinationView(destination)
                    }
            }
            .tabItem {
                Label("Profile", systemImage: "person.crop.circle")
            }
            .tag(Tab.profile)
        }
        .tint(.bwellPurple)
    }

    @ViewBuilder
    private func destinationView(_ destination: AppView) -> some View {
        switch destination {
        case .healthSummary:
            HealthSummaryView()
        case .healthCategory(let category):
            HealthSummaryDetailView(category: category)
        case .healthGroupItems(let category, let groupCode):
            HealthDataFactoryView(category, groupCode)
        case .careTeams:
            CareTeamsView()
        case .careTeamMembers:
            CareTeamMembersView()
        case .documentReferences:
            DocumentReferencesView()
        case .goals:
            GoalsView()
        case .diagnosticReports:
            DiagnosticReportsView()
        case .devices:
            DevicesView()
        case .manageConnections:
            ManageConnectionsView()
        case .connections:
            ConnectionsView()
        case .searchConnections(let connection):
            SearchConnectionsView(connection: connection)
        case .financial:
            FinancialView()
        case .accountSettings:
            AccountSettingsView()
        case .deviceRegistration:
            DeviceRegistrationView()
        case .profile:
            ProfileView()
        case .providerSearch:
            ProviderSearchView()
        case .home:
            HomeView()
        case .healthJourney:
            HealthJourneyView()
        case .taskDetail:
            HealthJourneyView()
        case .questionnaireResponses:
            QuestionnaireResponsesView()
        case .providerResources:
            ProviderResourcesView()
        }
    }
}
