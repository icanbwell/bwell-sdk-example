import Foundation
import BWellSDK

@MainActor
final class ProfileHomeViewModel: ObservableObject {
    @Published var username: String = ""
    @Published var isLoading: Bool = true

    func fetchUsername(sdk: BWellSDK) {
        // Prevent re-fetching if a fetch is already in progress.
        guard username.isEmpty else {
            isLoading = false
            return
        }

        isLoading = true

        Task {
            do {
                let user = try await sdk.user.getProfile()
                self.username = user?.name?.first?.given?.first ?? "BWell User"
            } catch {
                print("Error fetching user profile: \(error)")
                // Assign a default name on error so the UI doesn't look broken.
                self.username = "User"
            }
            self.isLoading = false
        }
    }
}
