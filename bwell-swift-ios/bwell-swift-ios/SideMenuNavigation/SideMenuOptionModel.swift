//
//  SideMenuOptionModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//

import Foundation

enum SideMenuOptionModel: Int, CaseIterable {
    case home
    case profile
    case healthSummary
    case providerSearch
    case manageConnections
    case financial
    case logout

    var title: String {
        switch self {
            case .home: "Home"
            case .profile: "Profile"
            case .healthSummary: "Health Summary"
            case .providerSearch: "Find Providers"
            case .manageConnections: "Manage Connections"
            case .financial: "Insurance"
            case .logout: "Logout"
        }
    }

    var iconName: String {
        switch self {
            case .home: "house"
            case .profile: "person"
            case .healthSummary: "heart.text.clipboard"
            case .providerSearch: "magnifyingglass"
            case .manageConnections: "rectangle.connected.to.line.below"
            case .financial: "creditcard"
            case .logout: "arrow.right.square"
        }
    }

    var destination: AppView {
        switch self {
            case .home: .home
            case .profile: .profile
            case .healthSummary: .healthSummary
            case .providerSearch: .providerSearch
            case .manageConnections: .manageConnections
            case .financial: .financial
            case .logout: .home
        }
    }
}

extension SideMenuOptionModel: Identifiable {
    var id: Int {
        return self.rawValue
    }
}

@MainActor
final class SideMenuOptionViewModel: ObservableObject {
    private var router: NavigationRouter?
    @Published var optionSelected: SideMenuOptionModel?

    func setup(router: NavigationRouter) {
        self.router = router
    }

    func navigate(to optionSelected: SideMenuOptionModel, sdkManager: SDKManager) {
        guard let router = router else { return }

        // Handle logout separately
        if optionSelected == .logout {
            sdkManager.logout()
            return
        }

        // Update the selected option
        self.optionSelected = optionSelected

        // For other navigation, navigate and replace the current stack
        router.navigateAndReplace(to: optionSelected.destination)
    }
}
