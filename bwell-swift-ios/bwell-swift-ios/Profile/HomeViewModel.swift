import Foundation
import BWellSDK
import Combine

@MainActor
final class HomeViewModel: ObservableObject {
    private let sdkManager: BWellSDKManager
    private var cancellables = Set<AnyCancellable>()

    @Published var username: String = ""
    @Published var isLoading: Bool = true

    init(sdkManager: BWellSDKManager = .shared) {
        self.sdkManager = sdkManager
        
        // Subscribe to state changes to know when it's safe to fetch data.
        sdkManager.$state
            .receive(on: DispatchQueue.main)
            .sink { [weak self] state in
                if case .authenticated = state {
                    self?.fetchUsername()
                }
            }
            .store(in: &cancellables)

        // Also check the state immediately on init. This handles the case where
        // the view is created *after* the app is already authenticated.
        if case .authenticated = sdkManager.state {
            fetchUsername()
        }
    }

    private func fetchUsername() {
        // Prevent re-fetching if a fetch is already in progress.
        guard username.isEmpty else {
            isLoading = false
            return
        }
        
        isLoading = true
        
        Task {
            do {
                let user = try await sdkManager.user()?.getProfile()
                self.username = user?.firstName ?? "BWell User"
            } catch {
                print("Error fetching user profile: \(error)")
                // Assign a default name on error so the UI doesn't look broken.
                self.username = "User"
            }
            self.isLoading = false
        }
    }
}
