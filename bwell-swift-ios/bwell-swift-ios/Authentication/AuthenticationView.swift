//
//  ClientKeyAuthView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 27/10/25.
//

import SwiftUI

struct AuthenticationView: View {
    @StateObject var clientKeyViewModel = ClientKeyViewModel()
    @StateObject var loginViewModel = LoginViewModel()
    @EnvironmentObject var router: NavigationRouter

    var body: some View {
        ZStack(alignment: .center) {
            Color.bwellPurple
                .ignoresSafeArea()
            VStack(alignment: .center, spacing: 20) {
                Image("bwell-logo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 150)
                    .padding(.bottom, 40)

                Spacer()

                VStack(spacing: 20) {
                    Text("Authentication")
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(.white)

                    if clientKeyViewModel.authenticated {
                        LoginView()
                    } else {
                        ClientKeyView(viewModel: clientKeyViewModel)
                    }
                }
                .padding()
                .background(.white.opacity(0.25))
                .clipShape(RoundedRectangle(cornerRadius: 10.0))
                .padding(.horizontal)

                if loginViewModel.isLoading {
                    ProgressView("Authenticating...")
                        .padding(.top, 20)
                        .foregroundStyle(.white)
                }
                Spacer()
            }
        }
    }
}

struct ClientKeyView: View {
    @StateObject var viewModel: ClientKeyViewModel

    var body: some View {
        VStack {
            BWellTextField(placeholder: "Client key",
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
}

struct LoginView: View {
    @StateObject var viewModel = LoginViewModel()

    var body: some View {
        VStack {
            BWellTextField(placeholder: "Email",
                           text: $viewModel.email,
                           iconName: "envelope.fill",
                           errorMessage: viewModel.errorMessage)
            .keyboardType(.emailAddress)

            BWellTextField(placeholder: "Password",
                           text: $viewModel.password,
                           iconName: "lock.fill",
                           isSecure: true,
                           errorMessage: viewModel.errorMessage)
            .padding(.bottom)

            BWellButton(title: "Login") {
                viewModel.login()
            }.disabled(viewModel.isLoading)
        }.padding()
    }
}

#Preview {
    AuthenticationView()
        .environmentObject(NavigationRouter())
}
