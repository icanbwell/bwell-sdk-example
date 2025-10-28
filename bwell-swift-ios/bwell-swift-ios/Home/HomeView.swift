//
//  HomeView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import SwiftUI

struct HomeView: View {
    @EnvironmentObject var sdkManager: BWellSDKManager
    @State private var showMenu: Bool = false

    var body: some View {
        NavigationView {
            ZStack {
                VStack {
                    
                }
                SideMenuView(isShowing: $showMenu)
            }
            .navigationTitle(Text("Welcome"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar(showMenu ? .hidden: .visible, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        showMenu.toggle()
                    } label: {
                        Image(systemName: "line.3.horizontal")
                            .foregroundStyle(.black)
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        sdkManager.logout()
                    } label: {
                        Text("Log out")
                    }
                }
            }
        }
    }
}

#Preview {
    HomeView()
}
