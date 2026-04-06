//
//  FinancialViewModel.swift
//  bwell-swift-ios
//
//  Created on 3/31/26.
//

import Foundation
import BWellSDK

@MainActor
final class FinancialViewModel: ObservableObject {
    @Published var coverages: [BWell.Coverage] = []
    @Published var explanationOfBenefits: [BWell.ExplanationOfBenefit] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    func getCoverages(sdk: BWellClient) async {
        isLoading = true
        errorMessage = nil
        do {
            let request = BWell.CoverageRequest(page: 0)
            let response = try await sdk.financial.getCoverages(request)
            coverages = response?.entry?.compactMap { $0.resource } ?? []
            isLoading = false
        } catch {
            errorMessage = "Failed to load coverage data: \(error.localizedDescription)"
            isLoading = false
        }
    }

    func getExplanationOfBenefits(sdk: BWellClient) async {
        do {
            let request = BWell.ExplanationOfBenefitRequest(page: 0)
            let response = try await sdk.financial.getExplanationOfBenefits(request)
            explanationOfBenefits = response?.entry?.compactMap { $0.resource } ?? []
        } catch {
            errorMessage = "Failed to load explanation of benefits: \(error.localizedDescription)"
        }
    }
}
