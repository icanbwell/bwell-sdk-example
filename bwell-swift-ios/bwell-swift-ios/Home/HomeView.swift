//
//  HomeView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import SwiftUI

struct HomeView: View {
    @State private var showMenu: Bool = false
    var body: some View {
        VStack {
            Text("Authenticated")
        }.bwellNavigationBar(showMenu: $showMenu)
    }
}

#Preview {
    HomeView()
        .environmentObject(NavigationRouter())
        .environmentObject(SideMenuOptionViewModel())
}
