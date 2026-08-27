//
//  Extensions.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 30/10/25.
//

import Foundation
import SwiftUI
import BWellSDK

// BWellClient is not declared Sendable in the SDK. This conformance is safe
// for the sample app because BWellClient is only created once on the main actor
// and its internal state is not mutated concurrently. Remove when the SDK
// adds its own Sendable conformance.
extension BWellClient: @unchecked Sendable {}

extension Date {
    func toString() -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateStyle = .long
        dateFormatter.timeStyle = .none
        dateFormatter.locale = Locale(identifier: "en_US")

        return dateFormatter.string(from: self)
    }
}

extension String {
    /// Parse FHIR date strings to Date. Handles all common FHIR datetime formats.
    func fhirDate() -> Date? {
        // Try ISO8601 first (handles most FHIR formats including timezone offsets)
        let iso = ISO8601DateFormatter()
        iso.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        if let date = iso.date(from: self) { return date }

        iso.formatOptions = [.withInternetDateTime]
        if let date = iso.date(from: self) { return date }

        // Fall back to manual formats
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        let formats = [
            "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssZZZZZ",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        ]
        for format in formats {
            formatter.dateFormat = format
            if let date = formatter.date(from: self) { return date }
        }
        return nil
    }

    func dateFormatter() -> String {
        guard let date = fhirDate() else { return self }

        let outputFormatter = DateFormatter()
        outputFormatter.dateFormat = "MMMM dd, yyyy"
        outputFormatter.locale = Locale(identifier: "en_US_POSIX")

        return outputFormatter.string(from: date)
    }

    func relativeDate() -> String {
        guard let validDate = fhirDate() else { return self }

        let calendar = Calendar.current
        let now = Date.now

        if calendar.isDateInToday(validDate) {
            return "Today"
        } else if calendar.isDateInYesterday(validDate) {
            return "Yesterday"
        } else {
            let days = calendar.dateComponents([.day], from: validDate, to: now).day ?? 0
            if days > 0 && days <= 7 {
                return "\(days) day\(days == 1 ? "" : "s") ago"
            } else if calendar.component(.year, from: validDate) == calendar.component(.year, from: now) {
                let outputFormatter = DateFormatter()
                outputFormatter.dateFormat = "MMMM d"
                outputFormatter.locale = Locale(identifier: "en_US")
                return outputFormatter.string(from: validDate)
            } else {
                let outputFormatter = DateFormatter()
                outputFormatter.dateFormat = "MMMM d, yyyy"
                outputFormatter.locale = Locale(identifier: "en_US")
                return outputFormatter.string(from: validDate)
            }
        }
    }

    func toDate() -> Date {
        fhirDate() ?? .now
    }

    func stripHTML() -> String {
        guard let data = self.data(using: .utf8) else { return self }

        let options: [NSAttributedString.DocumentReadingOptionKey: Any] = [
            .documentType: NSAttributedString.DocumentType.html,
            .characterEncoding: String.Encoding.utf8.rawValue
        ]

        guard let attributedString = try? NSAttributedString(data: data,
                                                             options: options,
                                                             documentAttributes: nil) else {
            return self
        }

        return attributedString.string
    }

    func capitalizingFirstLetter() -> String {
        return prefix(1).capitalized + dropFirst().lowercased()
    }
}

extension BWell.HealthSummary.Resource.Category {
    var displayName: String {
        switch self {
        case .allergyIntolerance: return "Allergies"
        case .carePlan: return "Care Plans"
        case .condition: return "Conditions"
        case .encounter: return "Encounters"
        case .immunization: return "Immunizations"
        case .labs: return "Labs"
        case .medications: return "Medications"
        case .procedure: return "Procedures"
        case .vitalSigns: return "Vitals"
        }
    }

    var icon: String {
        switch self {
        case .allergyIntolerance: return "facemask"
        case .carePlan: return "list.clipboard"
        case .condition: return "stethoscope"
        case .encounter: return "person.2.wave.2"
        case .immunization: return "syringe"
        case .labs: return "testtube.2"
        case .medications: return "pills"
        case .procedure: return "ivfluid.bag"
        case .vitalSigns: return "waveform.path.ecg.rectangle"
        }
    }

    var color: Color {
        switch self {
        case .allergyIntolerance: return .orange
        case .carePlan: return .bwellGreen
        case .condition: return .bwellRed
        case .encounter: return .bwellBlue
        case .immunization: return .teal
        case .labs: return .purple
        case .medications: return .bwellPurple
        case .procedure: return .cyan
        case .vitalSigns: return .pink
        }
    }
}

// BWell.SearchHealthResourcesResults.Result already conforms to Identifiable in the SDK

extension BWell.Gender {
    func description() -> String {
        switch self {
            case .female:
                return "Female"
            case .male:
                return "Male"
            case .other:
                return "Other"
            default:
                return "Unknown"
        }
    }
}
