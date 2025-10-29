//
//  HealthSummaryViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//
import Foundation
import BWellSDK

@MainActor
final class HealthSummaryViewModel: ObservableObject {
    private var sdkManager: BWellSDKManager?
    private var allergyIntoleranceResponse: BWell.GetAllergyIntolerancesQueryResult?
    @Published var errorMessage: String?

    // Queries entries properties
    @Published var allergyIntolerances: [AllergyIntoleranceEntry] = []
    @Published var carePlans: [CarePlanEntry] = []

    init() { }

    func setup(sdkManager: BWellSDKManager) {
        self.sdkManager = sdkManager
    }

    // MARK: - Allergy Intolerance 
    func getAllergyIntolerances() async {
        do {
            guard let sdkManager = sdkManager else { return }
            // Create the health data request
            let request = BWell.HealthDataRequest(page: 1)
            allergyIntoleranceResponse = try await sdkManager.health().getAllergyIntolerances(request)

            guard let entries = allergyIntoleranceResponse?.entry else { return }

            for entry in entries {
                let allergyEntry: AllergyIntoleranceEntry = .init(
                    id: entry.resource?.id,
                    allergy: entry.resource?.code?.coding?.first?.display,
                    criticality: entry.resource?.criticality
                )
                allergyIntolerances.append(allergyEntry)
            }
        } catch {
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            allergyIntoleranceResponse = nil
        }
    }
}
