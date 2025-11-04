//
//  ManageConnectionsViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//
import Foundation
import BWellSDK

final class ManageConnectionsViewModel: ObservableObject {
    private var sdkManager: BWellSDKManager?
    @Published var errorMessage: String?
    @Published var isLoading: Bool = false

    func setup(sdkManager: BWellSDKManager) {
        self.sdkManager = sdkManager
    }

    func createConnection() async {
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }
            let request = BWell.CreateConnectionRequest(connectionId: "",
                                                        username: "",
                                                        password: "")
            _ = try await sdkManager.connection().createConnection(request)
        } catch {
            errorMessage = "Failed to established a connection."
        }
    }

    func getDataSourceConnections() async {
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let request = BWell.DataSourceRequest(connectionId: "")

            _ = try await sdkManager.connection().getDataSource(request)
        } catch {
            errorMessage = "Failed to established a connection."
        }
    }
}
