//
//  SearchConnectionsViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 07/11/25.
//
import Foundation
import BWellSDK

@MainActor
final class SearchConnectionsViewModel: ObservableObject {
    private var sdkManager: BWellSDKManager?

    @Published var errorMessage: String?
    @Published var isLoading: Bool = false
    @Published var healthResources: [BWell.SearchHealthResourcesResults.Result] = []

    init() {
        self.sdkManager = .shared
    }

    func searchHealthResources(searchedText: String?) async {
        isLoading = true

        do {
            guard let sdkManager = sdkManager else {
                isLoading = false
                errorMessage = "SDK Manager not available."
                return
            }

            let filters: BWell.SearchHealthResourcesRequest.Filter = .init(includePopulatedProaOnly: true)
            let orderBy: BWell.SearchHealthResourcesRequest.OrderBy = .init(field: .distance, order: .asc)
            let location: BWell.SearchHealthResourcesRequest.Location = .init(latitude: 39.2848102, longitude: -76.702898)

            let request = BWell.SearchHealthResourcesRequest(page: 0,
                                                             pageSize: 100,
                                                             search: searchedText,
                                                             filters: filters,
                                                             orderBy: [orderBy],
                                                             location: location)


            let response = try await sdkManager.search().searchHealthResources(request)

            if let healthResourcesResults = response.results, !healthResourcesResults.isEmpty {
                healthResources = healthResourcesResults
            }
        } catch {
            errorMessage = "Failed to load connections."
            isLoading = false
            return
        }
    }
}
