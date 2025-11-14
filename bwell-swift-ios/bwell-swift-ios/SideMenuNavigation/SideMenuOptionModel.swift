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
    case manageConnections
    case healthSummary
    case logout


    var title: String {
        switch self {
            case .home: "Home"
            case .profile: "Profile"
            case .manageConnections: "Manage Connections"
            case .healthSummary: "Health Summary"
            case .logout: "Logout"
        }
    }

    var iconName: String {
        switch self {
            case .home: "house"
            case .profile: "person"
            case .manageConnections: "rectangle.connected.to.line.below"
            case .healthSummary: "heart.text.clipboard"
            case .logout: "arrow.right.square"
        }
    }

    var destination: AppView {
        switch self {
            case .home: .home
            case .profile: .profile
            case .manageConnections: .manageConnections
            case .healthSummary: .healthSummary
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
    private var sdkManager: BWellSDKManager?
    @Published var optionSelected: SideMenuOptionModel?

    init() {
        self.sdkManager = .shared
    }

    func setup(router: NavigationRouter) {
        self.router = router
    }

    func navigate(to optionSelected: SideMenuOptionModel) {
        guard let router = router else { return }
        
        // Handle logout separately
        if optionSelected == .logout {
            sdkManager?.logout()
            return
        }
        
        // Update the selected option
        self.optionSelected = optionSelected
        
        // For other navigation, navigate and replace the current stack
        router.navigateAndReplace(to: optionSelected.destination)
    }
}
