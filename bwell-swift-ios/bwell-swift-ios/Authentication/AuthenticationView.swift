//
//  ClientKeyAuthView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import SwiftUI

struct AuthenticationView: View {
    @StateObject private var viewModel = AuthenticationViewModel()
    @StateObject private var connectionsViewModel = ManageConnectionsViewModel()
    @EnvironmentObject var router: NavigationRouter
    @State private var authWithToken: Bool = false

    var body: some View {
        ZStack {
            ZStack(alignment: .top) {
                Color.bwellPurple
                    .ignoresSafeArea()

                Image("bwell-logo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 150)
                    .padding(.bottom, 40)
            }

            VStack(alignment: .center, spacing: 15) {
                Spacer()
                if !viewModel.apiKeyValidated {
                    apiKeyView
                } else {
                    credentialsView
                }
                Spacer()
            }
            .padding(.horizontal)
            .frame(height: 350)
            .padding(.horizontal)
        }
    }

    // MARK: - API Key authentication view
    @ViewBuilder
    var apiKeyView: some View {
        VStack(alignment: .leading) {
            Text("Client Key")
                .font(.headline)
                .fontWeight(.bold)
                .foregroundStyle(.white)
                .padding(.bottom, 7)

            AuthenticationTextField(placeholder: "Client key",
                                    text: $viewModel.clientKey,
                                    iconName: "key.fill",
                                    isSecure: true,
                                    errorMessage: viewModel.errorMessage)
            .padding(.bottom)

            BWellButton(title: "Submit") {
                viewModel.initializeSDK()
            }.disabled(viewModel.isLoading)
        }
    }

    // MARK: - Email/Password authentication view
    @ViewBuilder
    var credentialsView: some View {
        VStack(alignment: .leading) {
            if authWithToken {
                Text("JWT Token")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
                    .padding(.bottom, 7)

                AuthenticationTextField(placeholder: "JWT Token",
                                        text: $viewModel.oauthToken,
                                        iconName: "key.card.fill",
                                        isSecure: true,
                                        errorMessage: viewModel.errorMessage)
                .padding(.bottom)
            } else {
                Text("Email")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
                    .padding(.bottom, 7)

                AuthenticationTextField(placeholder: "Email",
                                        text: $viewModel.email,
                                        iconName: "envelope.fill",
                                        errorMessage: viewModel.emailErrorMessage)
                .keyboardType(.emailAddress)
                .padding(.bottom)

                Text("Password")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
                    .padding(.bottom, 7)

                AuthenticationTextField(placeholder: "Password",
                                        text: $viewModel.password,
                                        iconName: "lock.fill",
                                        isSecure: true,
                                        errorMessage: viewModel.passwordErrorMessage)
                .padding(.bottom)
            }


            BWellButton(title: "Login") {
                authWithToken ? viewModel.loginWithOAuthToken() : viewModel.loginWithEmailAndPassword()
            }.disabled(viewModel.isLoading)

            HStack(spacing: 5) {
                Spacer()

                Text("Login with ")
                    .foregroundStyle(.white)

                Button {
                    authWithToken.toggle()
                    viewModel.errorMessage = nil
                    viewModel.emailErrorMessage = nil
                    viewModel.passwordErrorMessage = nil
                } label: {
                    Text(authWithToken ? "email & password": "OAuth token.")
                        .foregroundStyle(.white)
                        .underline(true, color: .white)
                }
                Spacer()
            }.padding(.top)
        }
        .onChange(of: viewModel.email) {
            viewModel.emailErrorMessage = nil
        }
        .onChange(of: viewModel.password) {
            viewModel.passwordErrorMessage = nil
        }
        .onChange(of: viewModel.oauthToken) {
            viewModel.errorMessage = nil
        }
        .onChange(of: viewModel.clientKey) {
            viewModel.errorMessage = nil
        }
        .padding()

    }
}

#Preview {
    AuthenticationView()
        .environmentObject(NavigationRouter())
}
