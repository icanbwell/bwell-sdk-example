//
//  Router.swift
//  bwell-swift-ios
//
//  Navigation coordinator using NavigationPath.
//  All navigation goes through AppView — no mixed types in the path.
//

import Foundation
import SwiftUI
import BWell

enum AppView: Hashable {
    case home
    case profile
    case healthSummary
    case manageConnections
    case providerSearch
    case financial

    // Subviews of manageConnections
    case connections
    case searchConnections(connection: ConnectionsModel)

    // Health Summary -> category group list
    case healthCategory(category: HealthDataSummaryModel)

    // Health category -> group items list
    case healthGroupItems(category: HealthDataSummaryModel,
                          groupCode: BWellHealthDataWrapper<BWell.Coding>)

    // Additional health data views
    case careTeams
    case documentReferences
    case goals
    case diagnosticReports
    case devices
    case accountSettings
    case deviceRegistration

    // Health Journey (Tasks)
    case healthJourney
    case taskDetail(taskId: String)

    // Questionnaire Responses
    case questionnaireResponses

    // Provider Resources
    case providerResources
}

@MainActor
final class NavigationRouter: ObservableObject {
    @Published var path = NavigationPath()

    func navigate(to destination: AppView) {
        path.append(destination)
    }

    func navigateAndReplace(to destination: AppView) {
        path = NavigationPath()
        path.append(destination)
    }

    func pop() {
        guard !path.isEmpty else { return }
        path.removeLast()
    }

    func popToRoot() {
        path.removeLast(path.count)
    }
}
