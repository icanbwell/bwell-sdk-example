//
//  ConnectionsModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 06/11/25.
//
import Foundation
import SwiftUI

enum ConnectionsModel: Hashable {
    case insurance
    case providers
    case clinics
    case labs

    var icon: String {
        switch self {
            case .insurance: "heart.text.square"
            case .providers: "cross.fill"
            case .clinics: "ivfluid.bag"
            case .labs: "heart.text.clipboard"
        }
    }

    var title: String {
        switch self {
            case .insurance: "Insurance"
            case .providers: "Providers"
            case .clinics: "Clinics, Hospitals, and Health Systems."
            case .labs: "Labs"
        }
    }

    var description: LocalizedStringKey {
        switch self {
            case .insurance: "Insurance Description"
            case .providers: "Providers Description"
            case .clinics: "Clinics Description"
            case .labs: "Labs Description"
        }
    }

    var iconColor: Color {
        switch self {
            case .insurance, .clinics: .bwellBlue
            case .providers, .labs: .bwellRed
        }
    }
}
