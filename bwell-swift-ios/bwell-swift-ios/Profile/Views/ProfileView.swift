//
//  ProfileView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//

import Foundation
import SwiftUI
import BWell

struct ProfileView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @StateObject private var viewModel = ProfileViewModel()
    @State private var text: String = ""
    @State private var isEditing: Bool = false
    @State private var showVerificationInstructions = false

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
                            if let sdk = sdkManager.sdk {
                                await viewModel.updateUserProfile(sdk: sdk)
                            }
                            isEditing = false
                        }
                    }).padding(.top, 5)
                } else {
                    readOnlyProfileView
                }
            }
        }
        .ignoresSafeArea(edges: .bottom)
        .navigationTitle("Profile")
        .navigationBarTitleDisplayMode(.large)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    isEditing.toggle()
                } label: {
                    if isEditing {
                        Text("Cancel")
                            .foregroundStyle(.white)
                    } else {
                        Image(systemName: "square.and.pencil")
                    }
                }
            }
            ToolbarItem(placement: .topBarTrailing) {
                NavigationLink(value: AppView.accountSettings) {
                    Image(systemName: "gearshape")
                }
            }
        }
        .task {
            if let sdk = sdkManager.sdk {
                await viewModel.getUserProfile(sdk: sdk)
                await viewModel.getVerificationStatus(sdk: sdk)
            }
        }
        .sheet(isPresented: $showVerificationInstructions) {
            verificationInstructionsSheet
        }
        .onReceive(NotificationCenter.default.publisher(for: .verificationCallback)) { _ in
            Task {
                if let sdk = sdkManager.sdk {
                    await viewModel.getVerificationStatus(sdk: sdk)
                }
            }
        }
    }

    @ViewBuilder
    var verificationStatusCard: some View {
        if viewModel.isLoadingVerification {
            HStack(spacing: 8) {
                ProgressView()
                Text("Checking verification status...")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
            .padding()
        } else if viewModel.isVerified {
            HStack(spacing: 8) {
                Image(systemName: "checkmark.seal.fill")
                    .font(.title3)
                Text("Identity Verified")
                    .font(.subheadline)
                    .fontWeight(.semibold)
            }
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.green.opacity(0.85))
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .padding(.horizontal)
        } else if viewModel.isVerificationFailed {
            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.title3)
                    Text("Verification Failed")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                }
                .foregroundStyle(.white)

                Button {
                    showVerificationInstructions = true
                } label: {
                    Text("Try Again")
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 6)
                        .background(.white.opacity(0.25))
                        .clipShape(Capsule())
                        .foregroundStyle(.white)
                }
            }
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.red.opacity(0.8))
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .padding(.horizontal)
        } else {
            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    Image(systemName: "person.badge.shield.checkmark")
                        .font(.title3)
                    Text("Verify Your Identity")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                }

                Text("Complete identity verification to unlock full access to your health data.")
                    .font(.caption)
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)

                Button {
                    showVerificationInstructions = true
                } label: {
                    Text("Get Verified")
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 6)
                        .background(.bwellPurple)
                        .foregroundStyle(.white)
                        .clipShape(Capsule())
                }
            }
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color(.systemGray6))
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .padding(.horizontal)
        }
    }

    @ViewBuilder
    var verificationInstructionsSheet: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Image(systemName: "person.badge.shield.checkmark")
                    .font(.system(size: 48))
                    .foregroundStyle(.bwellPurple)
                    .padding(.top, 24)

                Text("Verify your identity with CLEAR")
                    .font(.title3)
                    .fontWeight(.bold)
                    .multilineTextAlignment(.center)

                VStack(alignment: .leading, spacing: 16) {
                    instructionRow(step: "1", text: "You'll be redirected to CLEAR")
                    instructionRow(step: "2", text: "Complete identity verification")
                    instructionRow(step: "3", text: "Return to the app")
                }
                .padding(.horizontal)

                Spacer()

                Button {
                    Task {
                        if let sdk = sdkManager.sdk {
                            await viewModel.createVerificationURL(sdk: sdk)
                            if let urlString = viewModel.verificationURL,
                               let url = URL(string: urlString) {
                                showVerificationInstructions = false
                                await UIApplication.shared.open(url)
                            }
                        }
                    }
                } label: {
                    Text("Continue")
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(.bwellPurple)
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .padding(.horizontal)

                Button {
                    showVerificationInstructions = false
                } label: {
                    Text("Cancel")
                        .foregroundStyle(.secondary)
                }
                .padding(.bottom, 24)
            }
        }
        .presentationDetents([.medium])
    }

    private func instructionRow(step: String, text: String) -> some View {
        HStack(alignment: .top, spacing: 12) {
            Text(step)
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(.white)
                .frame(width: 28, height: 28)
                .background(.bwellPurple)
                .clipShape(Circle())

            Text(text)
                .font(.body)
                .padding(.top, 4)
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
                .padding(.top, 20)

            // Verification status card
            verificationStatusCard
                .padding(.vertical, 4)

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

                // Device Registration
                NavigationLink(value: AppView.deviceRegistration) {
                    HStack(spacing: 12) {
                        Image(systemName: "bell.badge")
                            .font(.title2)
                            .frame(width: 24, alignment: .center)
                        Text("Push Notifications")
                            .font(.headline)
                            .fontWeight(.medium)
                        Spacer()
                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .foregroundStyle(.tertiary)
                    }
                    .padding(.top, 10)
                }

                // Logout
                Button(role: .destructive) {
                    sdkManager.logout()
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: "arrow.right.square")
                            .font(.title2)
                            .frame(width: 24, alignment: .center)
                        Text("Log Out")
                            .font(.headline)
                            .fontWeight(.medium)
                    }
                    .foregroundStyle(.red)
                    .padding(.top, 20)
                }
            }
            .padding()
            .frame(height: .infinity)
            .clipShape(UnevenRoundedRectangle(topLeadingRadius: 25,
                                              topTrailingRadius: 25))

        }
        .ignoresSafeArea(edges: .bottom)
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
    NavigationStack {
        ProfileView()
    }
    .environmentObject(SDKManager())
    .environmentObject(NavigationRouter())
    .environmentObject(HealthSummaryViewModel())
}
