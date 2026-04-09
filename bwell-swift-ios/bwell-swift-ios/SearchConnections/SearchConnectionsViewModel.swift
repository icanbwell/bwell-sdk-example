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
    @Published var errorMessage: String?
    @Published var isLoading: Bool = false
    @Published var healthResources: [BWell.SearchHealthResourcesResults.Result] = []

    func searchHealthResources(searchedText: String?, sdk: BWellClient) async {
        isLoading = true

        do {
            let filters: BWell.SearchHealthResourcesRequest.Filter = .init(includePopulatedProaOnly: true)
            let orderBy: BWell.SearchHealthResourcesRequest.OrderBy = .init(field: .relevance, order: .desc)
            
            let location: BWell.SearchHealthResourcesRequest.Location = .init(latitude: 39.2848102, longitude: -76.702898)

            let request = BWell.SearchHealthResourcesRequest(page: 0,
                                                             pageSize: 100,
                                                             search: searchedText,
                                                             filters: filters,
                                                             orderBy: [orderBy],
                                                             location: location)

            let response = try await sdk.search.searchHealthResources(request)

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
