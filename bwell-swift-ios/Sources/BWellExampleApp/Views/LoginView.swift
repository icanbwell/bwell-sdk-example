import SwiftUI

struct LoginView: View {
    @StateObject private var viewModel = SDKViewModel()

    @State private var clientKey = ""
    @State private var email = ""
    @State private var password = ""
    @State private var showHealthData = false

    var body: some View {
        NavigationView {
            Form {
                if !viewModel.isInitialized {
                    Section(header: Text("Initialize SDK")) {
                        TextField("Client Key", text: $clientKey)
                            .textContentType(.password)
                            #if os(iOS)
                            .autocapitalization(.none)
                            #endif

                        Button("Initialize") {
                            Task {
                                await viewModel.initialize(clientKey: clientKey)
                            }
                        }
                        .disabled(clientKey.isEmpty)
                    }

                    Section(header: Text("Example Credentials")) {
                        Text("Use the client key from your bWell dashboard")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                } else if !viewModel.isAuthenticated {
                    Section(header: Text("Authenticate")) {
                        TextField("Email", text: $email)
                            #if os(iOS)
                            .autocapitalization(.none)
                            #endif

                        SecureField("Password", text: $password)
                            .textContentType(.password)

                        Button("Login") {
                            Task {
                                await viewModel.authenticate(email: email, password: password)
                                if viewModel.isAuthenticated {
                                    showHealthData = true
                                }
                            }
                        }
                        .disabled(email.isEmpty || password.isEmpty)
                    }
                } else {
                    Section {
                        Text("✓ Authenticated")
                            .foregroundColor(.green)

                        NavigationLink("View Health Data", isActive: $showHealthData) {
                            HealthDataView(viewModel: viewModel)
                        }
                    }
                }

                if let error = viewModel.errorMessage {
                    Section {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                }
            }
            .navigationTitle("bWell SDK Example")
        }
        .onAppear {
            viewModel.loadPersistedCredentials()
        }
    }
}
