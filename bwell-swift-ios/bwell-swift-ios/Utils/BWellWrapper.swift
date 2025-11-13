//
//  BWellTypeWrapper.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//
import Foundation
import BWellSDK

public struct BWellWrapper {
    typealias carePlan = BWell.CarePlan
    typealias condition = BWell.Condition
    typealias immunization = BWell.Immunization
    typealias labs = BWell.Observation
    typealias medications = BWell.MedicationStatements
    typealias procedures = BWell.Procedure
    typealias vitalSigns = BWell.Observation
    typealias encounter = BWell.Encounter
    typealias connectionStatus = BWell.DataConnectionStatus
    typealias healthResource = BWell.SearchHealthResourcesResults
    typealias allergyIntolerances = BWell.AllergyIntolerance
}

extension BWellWrapper.connectionStatus {
    func description() -> String {
        switch self {
            case .connected: "CONNECTED"
            case .disconnected: "DISCONNECTED"
            case .deleted: "DELETED"
            case .expired: "EXPIRED"
            case .accessEnded: "ACCESS ENDED"
            case .error: "ERROR"
            case .unknown: "UNKNOWN"
        }
    }
}
