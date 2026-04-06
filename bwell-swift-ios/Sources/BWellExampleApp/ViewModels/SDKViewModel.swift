import Foundation
import BWellSDK

@MainActor
class SDKViewModel: ObservableObject {
    @Published var isInitialized = false
    @Published var isAuthenticated = false
    @Published var errorMessage: String?
    @Published var allergies: [BWell.AllergyIntolerance] = []

    private var sdk: BWellSDK?
    private let keychain = KeychainService()

    private let clientKeyKey = "bwell_client_key"
    private let tokenKey = "bwell_auth_token"

    func initialize(clientKey: String) async {
        do {
            let config = BWell.SDKConfig(
                clientKey: clientKey
            )

            let sdk = try BWellClient(config: config)
            try await sdk.initialize()

            self.sdk = sdk
            self.isInitialized = true

            // Persist client key
            try keychain.save(clientKey, for: clientKeyKey)

            errorMessage = nil
        } catch {
            errorMessage = "Initialization failed: \(error.localizedDescription)"
        }
    }

    func authenticate(email: String, password: String) async {
        guard let sdk = sdk else {
            errorMessage = "SDK not initialized"
            return
        }

        do {
            let credentials = BWell.Credentials.usernamePassword(
                username: email,
                password: password
            )

            try await sdk.authenticate(credentials: credentials)
            self.isAuthenticated = true
            errorMessage = nil
        } catch {
            errorMessage = "Authentication failed: \(error.localizedDescription)"
        }
    }

    func fetchAllergies() async {
        guard let sdk = sdk else {
            errorMessage = "SDK not initialized"
            return
        }

        do {
            let request = BWell.HealthDataRequest(page: 0)
            let result = try await sdk.health.getAllergyIntolerances(request)
            self.allergies = result.entry?.compactMap { $0.resource } ?? []
            errorMessage = nil
        } catch {
            errorMessage = "Fetch allergies failed: \(error.localizedDescription)"
        }
    }

    func loadPersistedCredentials() {
        if let clientKey = try? keychain.load(for: clientKeyKey) {
            Task {
                await initialize(clientKey: clientKey)
            }
        }
    }
}
