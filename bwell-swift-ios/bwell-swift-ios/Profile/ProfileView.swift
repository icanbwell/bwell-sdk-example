//
//  ProfileView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//

import Foundation
import SwiftUI

struct ProfileView: View {
    @EnvironmentObject private var sdkManager: BWellSDKManager
    @ObservedObject private var viewModel = ProfileViewModel()
    @State private var showMenu: Bool = false

    var fullName: String {
        return "\(viewModel.givenName) \(viewModel.familyName)"
    }

    var address: String {
        return "\(viewModel.addressLine), \(viewModel.city), \(viewModel.state) \(viewModel.postalCode)"
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 30) {
            if viewModel.isLoading {
                ProgressView("Loading profile data...")
            } else {
                HStack(alignment: .top ,spacing: 20) {
                    Image("user-profile-picture")
                        .resizable()
                        .scaledToFill()
                        .clipShape(Circle())
                        .frame(width: 90, height: 90, alignment: .leading)

                    VStack(alignment: .leading, spacing: 5) {
                        Text(fullName)
                            .font(.system(.title3, design: .rounded, weight: .semibold))

                        if let contactPoint = viewModel.contactPoint["value"] {
                            Text(contactPoint)
                                .font(.system(.headline, design: .rounded, weight: .regular))
                        }

                    }.padding(.leading, 15)
                    Spacer()
                }
                .padding(.leading, 10)

                List {
                    ProfileListItem(icon: "person.fill", text: viewModel.gender)
                    ProfileListItem(icon: "calendar", text: viewModel.birthdate.dateFormatter())
                    ProfileListItem(icon: "house", text: address)
                    ProfileListItem(icon: "character.book.closed", text: viewModel.language)
                }.listStyle(.plain)

                Spacer()
            }
        }
        .padding(.top, 10)
        .bwellNavigationBar(showMenu: $showMenu)
        .onAppear {
            viewModel.setup(sdkManager: sdkManager)
            Task {
                await viewModel.getUserProfile()
            }
        }
        .refreshable {
            await viewModel.getUserProfile()
        }
    }
}

private struct ProfileListItem: View {
    var icon: String
    var text: String

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .frame(width: 24, alignment: .center)
            Text(text)
        }
    }
}

#Preview {
    ProfileView()
        .environmentObject(BWellSDKManager.shared)
        .environmentObject(NavigationRouter())
        .environmentObject(SideMenuOptionViewModel())
}

struct SearchView: View {
    @State private var showMenu: Bool = false

    var body: some View {
        VStack {
            Text("Search view")
        }.bwellNavigationBar(showMenu: $showMenu)
    }
}

struct ManageConnectionsView: View {
    @State private var showMenu: Bool = false

    var body: some View {
        VStack {
            Text("Manage connection view")
        }.bwellNavigationBar(showMenu: $showMenu)
    }
}
