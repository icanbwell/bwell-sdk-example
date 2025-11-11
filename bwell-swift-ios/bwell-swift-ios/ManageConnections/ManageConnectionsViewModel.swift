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

    func setup(router: NavigationRouter, sdkManager: BWellSDKManager) {
        self.sdkManager = sdkManager
    }

    func getConnections() async {
        isLoading = true

        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available."
                isLoading = false
                return
            }

            memberConnections = try await sdkManager.connection().getMemberConnections()

            print("ALL MEMBER CONNECTIONS: \(memberConnections)")

            for connection in memberConnections {
                memberConnections.append(connection)
            }

            isLoading = false
        } catch {
            errorMessage = "Failed to retrieve member connections."
            isLoading = false
            return
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
