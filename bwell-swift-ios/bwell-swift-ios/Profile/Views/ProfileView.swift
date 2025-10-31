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
    @State private var text: String = ""
    @State private var isEditing: Bool = false

    var fullName: String {
        return "\(viewModel.givenName) \(viewModel.familyName)"
    }

    var address: String {
        let addressLine = "\(viewModel.addressLineOne), \(viewModel.addressLineTwo)"

        return "\(addressLine), \(viewModel.city), \(viewModel.state) \(viewModel.postalCode)"
    }

    var body: some View {
        VStack(alignment: .leading) {
            if viewModel.isLoading {
                ProgressView("Loading profile data...")
            } else {
                if !isEditing {
                    readOnlyProfileView
                        .padding(.top, 20)
                } else {
                    EditableProfileView(
                        givenName: $viewModel.givenName,
                        familyName: $viewModel.familyName,
                        gender: $viewModel.gender,
                        addressLineOne: $viewModel.addressLineOne,
                        addressLineTwo: $viewModel.addressLineTwo,
                        city: $viewModel.city,
                        state: $viewModel.state,
                        postalCode: $viewModel.postalCode,
                        language: $viewModel.language,
                        selectedBirthdate: Binding<Date>(
                            get: {
                                let formatted = viewModel.birthdate.dateFormatter()
                                let date = formatted.toDate()
                                return date
                                },
                            set: { newDate in
                                viewModel.birthdate = newDate.toString()
                            }
                        ), action: {
                            Task {
                                await viewModel.updateUserProfile()
                            }
                            isEditing = false
                        }
                    ).padding(.top, 5)
                }

                Spacer()
            }
        }
        .ignoresSafeArea(edges: .bottom)
        .bwellNavigationBar(showMenu: $showMenu, trailingItem: {
            Button {
                isEditing.toggle()
            } label: {
                Text(isEditing ? "Cancel" : "Edit")
                    .foregroundStyle(.bwellBlue)
            }
        })
        .onAppear {
            viewModel.setup(sdkManager: sdkManager)
            Task {
                await viewModel.getUserProfile()
            }
        }
    } // end VStack

    var readOnlyProfileView: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top, spacing: 20) {
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

            List {
                ProfileListItem(icon: "person.fill", text: viewModel.gender.description())
                ProfileListItem(icon: "calendar", text: viewModel.birthdate.dateFormatter())
                ProfileListItem(icon: "house", text: address)
                ProfileListItem(icon: "character.book.closed", text: viewModel.language)
            }.listStyle(.plain)
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
