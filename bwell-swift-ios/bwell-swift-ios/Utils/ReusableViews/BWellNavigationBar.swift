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
    var trailingItem: (() -> TrailingItem)?

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
                                                trailingItem: @escaping () -> TrailingItem) -> some View {
        self.modifier(BWellNavigationBar(showMenu: showMenu, trailingItem: trailingItem))
    }

    func bwellNavigationBar(showMenu: Binding<Bool>) -> some View {
        self.modifier(BWellNavigationBar(showMenu: showMenu) { EmptyView() })
    }
}
