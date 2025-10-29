//
//  BWellNavigationBar.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//
import Foundation
import SwiftUI

struct BWellNavigationBar: ViewModifier {
    @Binding var showMenu: Bool
    @EnvironmentObject var viewModel: SideMenuOptionViewModel

    func body(content: Content) -> some View {
        NavigationView {
            ZStack {
                content
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

                        ToolbarItem(placement: .principal) {
                            Image("bwell-logo-header")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 60)
                        }
                    }

                SideMenuView(isShowing: $showMenu, viewModel: viewModel)
            }
        }
    }
}

extension View {
    func bwellNavigationBar(showMenu: Binding<Bool>) -> some View {
        self.modifier(BWellNavigationBar(showMenu: showMenu))
    }
}
