//
//  HealthSummaryModels.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//

import Foundation
import BWellSDK

// MARK: - Allergy Intolerance
struct AllergyIntoleranceEntry: Identifiable {
    let id: String?
    let allergy: String?
    let criticality: String?
}

// MARK: - Care Plan
// TODO: Adapt correctly the care plan model
struct CarePlanEntry: Identifiable {
    let id: String?
    let description: String?
}
