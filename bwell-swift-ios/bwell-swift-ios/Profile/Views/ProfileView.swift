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
        ZStack(alignment: .center) {
            if viewModel.isLoading {
                ProgressView("Loading profile data...")
            } else {
                if isEditing {
                   EditableProfileView(
                    givenName: $viewModel.givenName,
                    familyName: $viewModel.familyName,
                    gender: $viewModel.gender,
                    email: $viewModel.email,
                    workPhone: $viewModel.workPhone,
                    homePhone: $viewModel.homePhone,
                    mobilePhone: $viewModel.mobilePhone,
                    addressLineOne: $viewModel.addressLineOne,
                    addressLineTwo: $viewModel.addressLineTwo,
                    city: $viewModel.city,
                    state: $viewModel.state,
                    postalCode: $viewModel.postalCode,
                    language: $viewModel.language,
                    selectedBirthdate: Binding<Date>(
                        get: {
                            return viewModel.birthdate.dateFormatter().toDate()
                        }, set: { newDate in
                            viewModel.birthdate = newDate.toString()
                        }
                    ),
                    action: {
                        Task {
                            await viewModel.updateUserProfile()
                        }
                    }).padding(.top, 5)
                } else {
                    readOnlyProfileView
                }
            }
        }
        .ignoresSafeArea(edges: .bottom)
        .bwellNavigationBar(showMenu: $showMenu,
                            navigationTitle: "Profile", trailingItem: {
            Button {
                isEditing.toggle()
            } label: {
                if isEditing {
                    Text("Cancel")
                        .foregroundStyle(.bwellBlue)
                } else {
                    Image(systemName: "square.and.pencil")
                }
            }
        })
        .onAppear {
            viewModel.setup(sdkManager: sdkManager)
            Task {
                await viewModel.getUserProfile()
            }
        }
    }

    @ViewBuilder
    var readOnlyProfileView: some View {
        ScrollView {
            Image("user-profile-picture")
                .resizable()
                .scaledToFill()
                .clipShape(Circle())
                .frame(width: 120, height: 120, alignment: .center)
                .padding(.bottom, 20)

            VStack {
                Text("Personal Information")
                    .font(.title3)
                    .fontWeight(.medium)
                    .padding(.bottom, 10)

                ListItem(icon: "person.text.rectangle", title: "Full name", text: fullName)
                ListItem(icon: "calendar", title: "Birthdate", text: viewModel.birthdate)
                ListItem(icon: "figure.stand.dress.line.vertical.figure", title: "Gender", text: viewModel.gender.description())
                ListItem(icon: "house", title: "Address", text: address)

                Text("Contact Information")
                    .font(.title3)
                    .fontWeight(.medium)
                    .padding(.vertical, 10)

                if !viewModel.email.isEmpty {
                    ListItem(icon: "envelope", title: "Email address", text: viewModel.email)
                }

                if !viewModel.workPhone.isEmpty {
                    ListItem(icon: "suitcase", title: "Work phone number", text: viewModel.workPhone)
                }

                if !viewModel.homePhone.isEmpty {
                    ListItem(icon: "house", title: "Home phone number", text: viewModel.homePhone)
                }

                if !viewModel.mobilePhone.isEmpty {
                    ListItem(icon: "iphone", title: "Mobile phone number", text: viewModel.mobilePhone)
                }

                Text("Language")
                    .font(.title3)
                    .fontWeight(.medium)
                    .padding(.vertical, 10)

                ListItem(icon: "translate", title: "Language", text: viewModel.language)
            }
            .padding()
            .frame(height: .infinity)
            .clipShape(UnevenRoundedRectangle(topLeadingRadius: 20, topTrailingRadius: 20))
        }.ignoresSafeArea(edges: .bottom)
    }
}

private struct ListItem: View {
    var icon: String
    var title: String
    var text: String

    var body: some View {
        HStack(alignment: .center, spacing: 17) {
            Image(systemName: icon)
                .font(.title2)
                .frame(width: 24, alignment: .center)

            VStack(alignment: .leading) {
                Text(title)
                    .font(.headline)
                    .fontWeight(.medium)

                Text(text)
            }
            Spacer()
        }.padding(.bottom, 5)
    }
}

#Preview {
    ProfileView()
        .environmentObject(BWellSDKManager.shared)
        .environmentObject(NavigationRouter())
        .environmentObject(SideMenuOptionViewModel())
}
