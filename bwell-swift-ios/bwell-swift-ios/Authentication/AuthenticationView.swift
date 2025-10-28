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
            VStack(alignment: .center, spacing: 0) {
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
                Spacer()
            }
        }.onAppear {
            clientKeyViewModel.setup(router: router)
        }
    }
}

struct ClientKeyView: View {
    @StateObject var viewModel: ClientKeyViewModel

    var body: some View {
        VStack {
            CustomTextField(placeholder: "Client key",
                            text: $viewModel.clientKey,
                            iconName: "key.fill",
                            isSecure: true,
                            errorMessage: viewModel.errorMessage)
            .padding(.bottom)

            CustomButton(title: "Submit") {
                viewModel.initializeSDK()
            }
        }
    }
}

struct LoginView: View {
    @StateObject var viewModel = LoginViewModel()

    var body: some View {
        VStack {
            CustomTextField(placeholder: "Email",
                            text: $viewModel.email,
                            iconName: "envelope.fill")
            .keyboardType(.emailAddress)

            CustomTextField(placeholder: "Password",
                            text: $viewModel.password,
                            iconName: "key.fill",
                            isSecure: true)
            .padding(.bottom)

            CustomButton(title: "Login") {
                viewModel.login()
            }
        }.padding()
    }
}

#Preview {
    AuthenticationView()
        .environmentObject(NavigationRouter())
}
