//
//  BWellWrapper.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//
import Foundation
import BWellSDK

struct BWellWrapper {
    typealias carePlan = BWell.CarePlan
    typealias condition = BWell.Condition
    typealias immunization = BWell.Immunization
    typealias labs = BWell.Observation
    typealias medications = BWell.MedicationStatement
    typealias procedures = BWell.Procedure
    typealias vitalSigns = BWell.Observation
    typealias encounter = BWell.Encounter
    typealias connectionStatus = BWell.DataConnectionStatus
    typealias healthResource = BWell.SearchHealthResourcesResults
    typealias allergyIntolerances = BWell.AllergyIntolerance

    // Health Data Groups
    typealias allergyIntoleranceGroup = BWell.AllergyIntoleranceGroup
    typealias coding = BWell.Coding
}

struct BWellHealthDataWrapper<T>: Hashable, Identifiable {
    let id: String
    let entry: T

    init(_ id: String, _ entry: T) {
        self.id = id
        self.entry = entry
    }
    
    static func == (lhs: BWellHealthDataWrapper<T>, rhs: BWellHealthDataWrapper<T>) -> Bool {
        lhs.id == rhs.id
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}

extension BWell.DataConnectionStatus {
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
