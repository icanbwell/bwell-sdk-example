//
//  SideMenuOptionModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//

import Foundation

enum SideMenuOptionModel: Int, CaseIterable {
    case home
    case profile
    case search
    case manageConnections
    case healthSummary
    // case allergyIntolerances
    // case carePlans
    // case conditions
    // case encounters
    // case immunizations
    // case labs
    // case medications
    // case procedures
    // case vitalSigns
    case logout


    var title: String {
        switch self {
            case .home: "Home"
            case .profile: "Profile"
            case .search: "Search"
            case .manageConnections: "Manage Connections"
            case .healthSummary: "Health Summary"
            // case .allergyIntolerances: "Allergy Intolerances"
            // case .carePlans: "Care Plans"
            // case .conditions: "Conditions"
            // case .encounters: "Encounters"
            // case .immunizations: "Immunizations"
            // case .labs: "Labs"
            // case .medications: "Medications"
            // case .procedures: "Procedures"
            // case .vitalSigns: "Vital Signs"
            case .logout: "Logout"
        }
    }

    var iconName: String {
        switch self {
            case .home: "house"
            case .profile: "person"
            case .search: "magnifyingglass"
            case .manageConnections: "rectangle.connected.to.line.below"
            case .healthSummary: "heart.text.clipboard"
            // case .allergyIntolerances: "facemask"
            // case .carePlans: "list.clipboard"
            // case .conditions: "stethoscope"
            // case .encounters: "person.2.wave.2"
            // case .immunizations: "syringe"
            // case .labs: "testtube.2"
            // case .medications: "pills"
            // case .procedures: "ivfluid.bag"
            // case .vitalSigns: "waveform.path.ecg.rectangle"
            case .logout: "arrow.right.square"
        }
    }

    // TODO: Add another switch case with the views names for navigation
}

extension SideMenuOptionModel: Identifiable {
    var id: Int {
        return self.rawValue
    }
}
