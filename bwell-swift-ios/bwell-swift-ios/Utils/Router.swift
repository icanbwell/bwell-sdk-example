//
//  Router.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import Foundation
import SwiftUI

enum AppView {
    case home
    case profile
    case search
    case manageConnections
}

@MainActor
final class NavigationRouter: ObservableObject {
    // The path that controls the navigation stack
    @Published var path = NavigationPath()

    // Push the new screen into the navigation path
    func navigate(to destination: AppView) {
        path.append(destination)
    }

    // Navigate to a specific destination, clearing the current path first
    func navigateAndReplace(to destination: AppView) {
        path = NavigationPath()
        path.append(destination)
    }

    // Navigate to the previous screen
    func pop() {
        path.removeLast()
    }

    // Navigates back to the root of the navigation stack.
    func navigateToRoot() {
        path.removeLast(path.count)
    }
}
