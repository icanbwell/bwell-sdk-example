//
//  ManageConnectionsViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//
import Foundation
import BWellSDK

@MainActor
final class ManageConnectionsViewModel: ObservableObject {
    private var sdkManager: BWellSDKManager?
    @Published var errorMessage: String?
    @Published var isLoading: Bool = false

    @Published var memberConnections: [BWell.MemberConnectionResult] = []

    @Published var url: URL?

    init() {
        self.sdkManager = .shared
    }

    func getConnections() async {
        isLoading = true
        errorMessage = nil  // Clear previous errors

        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available."
                isLoading = false
                return
            }

            memberConnections = try await sdkManager.connection().getMemberConnections()

            print("ALL MEMBER CONNECTIONS: \(memberConnections)")

            isLoading = false
        } catch {
            errorMessage = "Failed to retrieve member connections: \(error.localizedDescription)"
            isLoading = false
            return
        }
    }

    /// Deletes a connection and refreshes the connections list
    /// - Parameter connectionId: The ID of the connection to delete
    func deleteConnection(connectionId: String) async {
        isLoading = true
        errorMessage = nil

        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available."
                isLoading = false
                return
            }

            let request = BWell.DeleteConnectionRequest(connectionId: connectionId)
            let result = try await sdkManager.connection().deleteConnection(request)

            print("Delete connection result - status: \(String(describing: result.status)), updated: \(result.statusUpdated)")

            // Refresh connections list after successful deletion
            await getConnections()
        } catch {
            errorMessage = "Failed to delete connection: \(error.localizedDescription)"
            isLoading = false
        }
    }

    /// Disconnects a connection and refreshes the connections list
    /// - Parameter connectionId: The ID of the connection to disconnect
    func disconnectConnection(connectionId: String) async {
        isLoading = true
        errorMessage = nil

        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available."
                isLoading = false
                return
            }

            let request = BWell.DisconnectConnectionRequest(connectionId: connectionId)
            let result = try await sdkManager.connection().disconnectConnection(request)

            print("Disconnect connection result - status: \(String(describing: result.status)), updated: \(result.statusUpdated)")

            // Refresh connections list after successful disconnection
            await getConnections()
        } catch {
            errorMessage = "Failed to disconnect connection: \(error.localizedDescription)"
            isLoading = false
        }
    }

    func createConnection(username: String, password: String, connectionId: String = "proa_demo") async {
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }
            let request = BWell.CreateConnectionRequest(connectionId: connectionId,
                                                        username: username,
                                                        password: password)
            _ = try await sdkManager.connection().createConnection(request)
        } catch {
            errorMessage = "Failed to established a connection."
            return
        }
    }

    func getDataSourceConnections() async {
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let request = BWell.DataSourceRequest(connectionId: "proa_demo")

            let response = try await sdkManager.connection().getDataSource(request)
            print("ALL DATA SOURCE CONNECTIONS: \(response)")

            /*for connection in response {
                memberConnections.append(response)
            }*/
        } catch {
            errorMessage = "Failed to established a connection."
            return
        }
    }

    func getOAuthURL(search userRequest: String) async {
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let request = BWell.OAuthURLRequest(connectionId: userRequest)
            let response = try await sdkManager.connection().getOAuthURL(request)

            url = URL(string: response.redirectURL)
        } catch {
            errorMessage = "Failed to established a connection."
            return
        }
    }
}
