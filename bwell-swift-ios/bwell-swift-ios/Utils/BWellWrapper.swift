//
//  BWellWrapper.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//
import Foundation
import BWell

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
    let codingKey: String
    let entry: T

    init(_ id: String, _ entry: T) {
        self.id = id
        self.entry = entry
        // Build a unique key from coding properties if available
        if let coding = entry as? BWell.Coding {
            self.codingKey = "\(coding.system ?? "")||\(coding.code ?? "")"
        } else {
            self.codingKey = id
        }
    }

    static func == (lhs: BWellHealthDataWrapper<T>, rhs: BWellHealthDataWrapper<T>) -> Bool {
        lhs.id == rhs.id && lhs.codingKey == rhs.codingKey
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
        hasher.combine(codingKey)
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
