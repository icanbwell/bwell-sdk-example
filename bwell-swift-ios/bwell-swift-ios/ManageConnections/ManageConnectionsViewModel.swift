//
//  ManageConnectionsViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//
import Foundation
import BWell

@MainActor
final class ManageConnectionsViewModel: ObservableObject {
    @Published var errorMessage: String?
    @Published var isLoading: Bool = false

    @Published var memberConnections: [BWell.MemberConnectionResult] = []

    @Published var url: URL?

    func getConnections(sdk: BWellSDK) async {
        isLoading = true
        errorMessage = nil  // Clear previous errors

        do {
            memberConnections = try await sdk.connection.getMemberConnections()

            print("ALL MEMBER CONNECTIONS: \(memberConnections)")

            isLoading = false
        } catch {
            errorMessage = "Failed to retrieve member connections: \(error.localizedDescription)"
            isLoading = false
            return
        }
    }

    /// Deletes a connection and refreshes the connections list
    /// - Parameters:
    ///   - connectionId: The ID of the connection to delete
    ///   - sdk: The BWellSDK instance to use
    func deleteConnection(connectionId: String, sdk: BWellSDK) async {
        isLoading = true
        errorMessage = nil

        do {
            let request = BWell.DeleteConnectionRequest(connectionId: connectionId)
            let result = try await sdk.connection.deleteConnection(request)

            print("Delete connection result - status: \(String(describing: result.status)), updated: \(result.statusUpdated)")

            // Refresh connections list after successful deletion
            await getConnections(sdk: sdk)
        } catch {
            errorMessage = "Failed to delete connection: \(error.localizedDescription)"
            isLoading = false
        }
    }

    /// Disconnects a connection and refreshes the connections list
    /// - Parameters:
    ///   - connectionId: The ID of the connection to disconnect
    ///   - sdk: The BWellSDK instance to use
    func disconnectConnection(connectionId: String, sdk: BWellSDK) async {
        isLoading = true
        errorMessage = nil

        do {
            let request = BWell.DisconnectConnectionRequest(connectionId: connectionId)
            let result = try await sdk.connection.disconnectConnection(request)

            print("Disconnect connection result - status: \(String(describing: result.status)), updated: \(result.statusUpdated)")

            // Refresh connections list after successful disconnection
            await getConnections(sdk: sdk)
        } catch {
            errorMessage = "Failed to disconnect connection: \(error.localizedDescription)"
            isLoading = false
        }
    }

    func createConnection(username: String, password: String, connectionId: String = "proa_demo", sdk: BWellSDK) async {
        do {
            let request = BWell.CreateConnectionRequest(connectionId: connectionId,
                                                        username: username,
                                                        password: password)
            _ = try await sdk.connection.createConnection(request)
        } catch {
            errorMessage = "Failed to established a connection."
            return
        }
    }

    func getDataSourceConnections(sdk: BWellSDK) async {
        do {
            let request = BWell.DataSourceRequest(connectionId: "proa_demo")

            let response = try await sdk.connection.getDataSource(request)
            print("ALL DATA SOURCE CONNECTIONS: \(response)")

            /*for connection in response {
                memberConnections.append(response)
            }*/
        } catch {
            errorMessage = "Failed to established a connection."
            return
        }
    }

    func getOAuthURL(search userRequest: String, sdk: BWellSDK) async {
        do {
            let request = BWell.OAuthURLRequest(connectionId: userRequest)
            let response = try await sdk.connection.getOAuthURL(request)

            url = URL(string: response.redirectURL)
        } catch {
            errorMessage = "Failed to established a connection."
            return
        }
    }
}
