//
//  FinancialViewModel.swift
//  bwell-swift-ios
//
//  Demonstrates the FinancialManager for insurance coverage and EOB retrieval.
//

import Foundation
import BWellSDK

@MainActor
final class FinancialViewModel: ObservableObject {
    private let sdkManager: BWellSDKManager

    @Published var coverages: [BWell.Coverage] = []
    @Published var explanationOfBenefits: [BWell.ExplanationOfBenefit] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    init() {
        self.sdkManager = .shared
    }

    // MARK: - Coverages

    func getCoverages() async {
        isLoading = true
        defer { isLoading = false }

        do {
            let request = BWell.CoverageRequest(page: 0, pageSize: 20)
            let response = try await sdkManager.financial().getCoverages(request)
            self.coverages = response?.entry?.compactMap { $0.resource } ?? []
            self.errorMessage = nil
        } catch {
            errorMessage = "Failed to fetch coverages: \(error.localizedDescription)"
        }
    }

    // MARK: - Explanation of Benefits

    func getExplanationOfBenefits() async {
        isLoading = true
        defer { isLoading = false }

        do {
            let request = BWell.ExplanationOfBenefitRequest(page: 0, pageSize: 20)
            let response = try await sdkManager.financial().getExplanationOfBenefits(request)
            self.explanationOfBenefits = response?.entry?.compactMap { $0.resource } ?? []
            self.errorMessage = nil
        } catch {
            errorMessage = "Failed to fetch explanation of benefits: \(error.localizedDescription)"
        }
    }
}
