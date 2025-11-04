//
//  BWellNavigationBar.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//
import Foundation
import SwiftUI

struct BWellNavigationBar<TrailingItem: View>: ViewModifier {
    @Binding var showMenu: Bool
    @EnvironmentObject var viewModel: SideMenuOptionViewModel
    var navigationTitle: String = ""
    var trailingItem: (() -> TrailingItem)?

    func body(content: Content) -> some View {
        NavigationView {
            ZStack {
                content
                    .navigationTitle(navigationTitle)
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

                        if let item = trailingItem?() {
                            ToolbarItem(placement: .topBarTrailing) {
                                item
                            }
                        }
                    }

                SideMenuView(isShowing: $showMenu, viewModel: viewModel)
            }
        }
    }
}

extension View {
    func bwellNavigationBar<TrailingItem: View>(showMenu: Binding<Bool>,
                                                navigationTitle: String = "",
                                                trailingItem: @escaping () -> TrailingItem) -> some View {
        self.modifier(BWellNavigationBar(showMenu: showMenu, navigationTitle: navigationTitle, trailingItem: trailingItem))
    }

    func bwellNavigationBar(showMenu: Binding<Bool>, navigationTitle: String = "") -> some View {
        self.modifier(BWellNavigationBar(showMenu: showMenu, navigationTitle: navigationTitle) { EmptyView() })
    }
}
