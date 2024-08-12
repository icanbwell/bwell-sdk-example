//
//  Router.swift
//  bwell-swift-iOS
//
//  Created by Kyle Wade on 8/12/24.
//

import Foundation
import SwiftUI

enum Route {
    case clientKeyEntry
    case oauthLogin
    case usernamePasswordLogin
    case helloWorld
}


class Router: ObservableObject {
    @Published var currentRoute: Route = .clientKeyEntry

    func navigate(to route: Route) {
        currentRoute = route
    }
}

struct RouterView: View {
    @EnvironmentObject var router: Router

    var body: some View {
        switch router.currentRoute {
        case .clientKeyEntry:
            ClientKeyEntryView()
        case .helloWorld:
            HelloWorldView()
        case .oauthLogin:
            OauthLoginView()
        case .usernamePasswordLogin:
            UsernamePasswordLoginView()
        }
    }
}
