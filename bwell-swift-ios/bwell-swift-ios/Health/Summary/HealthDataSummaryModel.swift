//
//  HealthDataSummaryModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 29/10/25.
//
import BWellSDK

enum HealthDataSummaryModel: Int, CaseIterable {
    case allergyIntolerance
    case carePlan
    case condition
    case encounter
    case immunization
    case labs
    case medications
    case procedure
    case vitalSigns

    var title: String {
        switch self {
            case .allergyIntolerance: "Allergy Intolerances"
            case .carePlan: "Care Plans"
            case .condition: "Conditions"
            case .encounter: "Encounters"
            case .immunization: "Immunizations"
            case .labs: "Labs"
            case .medications: "Medications"
            case .procedure: "Procedures"
            case .vitalSigns: "Vital Signs"
        }
    }

    var icon: String {
        switch self {
            case .allergyIntolerance: "facemask"
            case .carePlan: "list.clipboard"
            case .condition: "stethoscope"
            case .encounter: "person.2.wave.2"
            case .immunization: "syringe"
            case .labs: "testtube.2"
            case .medications: "pills"
            case .procedure: "ivfluid.bag"
            case .vitalSigns: "waveform.path.ecg.rectangle"
        }
    }

    var category: BWell.HealthSummary.Resource.Category {
        switch self {
            case .allergyIntolerance: .allergyIntolerance
            case .carePlan: .carePlan
            case .condition: .condition
            case .encounter: .encounter
            case .immunization: .immunization
            case .labs: .labs
            case .medications: .medications
            case .procedure: .procedure
            case .vitalSigns: .vitalSigns
        }
    }
}

extension HealthDataSummaryModel: Identifiable {
    var id: Int { return self.rawValue }
}
