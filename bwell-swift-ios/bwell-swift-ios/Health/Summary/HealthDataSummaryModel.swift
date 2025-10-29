//
//  HealthDataSummaryModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 29/10/25.
//


enum HealthDataSummaryModel: Int, CaseIterable {
    case allergyIntolerances
    case carePlans
    case conditions
    case encounters
    case immunizations
    case labs
    case medications
    case procedures
    case vitalSigns

    var title: String {
        switch self {
            case .allergyIntolerances: "Allergy Intolerances"
            case .carePlans: "Care Plans"
            case .conditions: "Conditions"
            case .encounters: "Encounters"
            case .immunizations: "Immunizations"
            case .labs: "Labs"
            case .medications: "Medications"
            case .procedures: "Procedures"
            case .vitalSigns: "Vital Signs"
        }
    }

    var icon: String {
        switch self {
            case .allergyIntolerances: "facemask"
            case .carePlans: "list.clipboard"
            case .conditions: "stethoscope"
            case .encounters: "person.2.wave.2"
            case .immunizations: "syringe"
            case .labs: "testtube.2"
            case .medications: "pills"
            case .procedures: "ivfluid.bag"
            case .vitalSigns: "waveform.path.ecg.rectangle"
        }
    }
}

extension HealthDataSummaryModel: Identifiable {
    var id: Int { return self.rawValue }
}