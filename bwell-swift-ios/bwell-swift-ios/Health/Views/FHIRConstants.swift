//
//  FHIRConstants.swift
//  bwell-swift-ios
//
//  FHIR R4 standard codes, value sets, and LOINC identifiers.
//  See https://www.hl7.org/fhir/observation.html and US Core Vital Signs profile.
//

import SwiftUI

// MARK: - LOINC Vital Signs Panel Codes

// Apple Health-style vital sign categories
enum VitalSignCategory: String, CaseIterable {
    case heart = "Heart"
    case respiratory = "Respiratory"
    case bodyMeasurements = "Body Measurements"
    case otherVitals = "Other Vitals"

    var icon: String {
        switch self {
        case .heart: return "heart.fill"
        case .respiratory: return "lungs.fill"
        case .bodyMeasurements: return "figure.stand"
        case .otherVitals: return "waveform.path.ecg.rectangle"
        }
    }

    var color: Color {
        switch self {
        case .heart: return .red
        case .respiratory: return .blue
        case .bodyMeasurements: return .purple
        case .otherVitals: return .orange
        }
    }
}

enum LoincVitalSign: String, CaseIterable {
    case bloodPressurePanel = "85354-9"
    case systolicBP = "8480-6"
    case diastolicBP = "8462-4"
    case heartRate = "8867-4"
    case bodyTemperature = "8310-5"
    case oralTemperature = "8331-1"
    case respiratoryRate = "9279-1"
    case oxygenSaturation = "2708-6"
    case oxygenSaturationPulseOx = "59408-5"
    case bodyWeight = "29463-7"
    case bodyWeightMeasured = "3141-9"
    case bodyHeight = "8302-2"
    case bodyHeightLying = "8306-3"
    case bmi = "39156-5"
    case headCircumference = "9843-4"
    case bodySurfaceArea = "3140-1"

    static let allCodes: Set<String> = Set(allCases.map(\.rawValue))

    var category: VitalSignCategory {
        switch self {
        case .bloodPressurePanel, .systolicBP, .diastolicBP, .heartRate:
            return .heart
        case .respiratoryRate, .oxygenSaturation, .oxygenSaturationPulseOx:
            return .respiratory
        case .bodyWeight, .bodyWeightMeasured, .bodyHeight, .bodyHeightLying,
             .bmi, .headCircumference, .bodySurfaceArea:
            return .bodyMeasurements
        case .bodyTemperature, .oralTemperature:
            return .otherVitals
        }
    }

    var friendlyName: String {
        switch self {
        case .bloodPressurePanel: return "Blood Pressure"
        case .systolicBP: return "Systolic BP"
        case .diastolicBP: return "Diastolic BP"
        case .heartRate: return "Heart Rate"
        case .bodyTemperature, .oralTemperature: return "Body Temperature"
        case .respiratoryRate: return "Respiratory Rate"
        case .oxygenSaturation, .oxygenSaturationPulseOx: return "Blood Oxygen"
        case .bodyWeight, .bodyWeightMeasured: return "Body Weight"
        case .bodyHeight: return "Height"
        case .bodyHeightLying: return "Length (Lying)"
        case .bmi: return "BMI"
        case .headCircumference: return "Head Circumference"
        case .bodySurfaceArea: return "Body Surface Area"
        }
    }

    var icon: String {
        switch self {
        case .bloodPressurePanel, .systolicBP, .diastolicBP: return "heart.fill"
        case .heartRate: return "waveform.path.ecg"
        case .bodyTemperature, .oralTemperature: return "thermometer.medium"
        case .respiratoryRate: return "lungs.fill"
        case .oxygenSaturation, .oxygenSaturationPulseOx: return "o2.circle.fill"
        case .bodyWeight, .bodyWeightMeasured: return "scalemass.fill"
        case .bodyHeight, .bodyHeightLying: return "ruler.fill"
        case .bmi: return "figure.stand"
        case .headCircumference: return "brain.head.profile"
        case .bodySurfaceArea: return "person.fill"
        }
    }

    var color: Color {
        switch self {
        case .bloodPressurePanel, .systolicBP, .diastolicBP, .heartRate: return .red
        case .bodyTemperature, .oralTemperature: return .orange
        case .respiratoryRate, .oxygenSaturation, .oxygenSaturationPulseOx: return .blue
        case .bodyWeight, .bodyWeightMeasured, .bodyHeight, .bodyHeightLying, .bmi, .headCircumference, .bodySurfaceArea: return .purple
        }
    }
}

// MARK: - FHIR Observation Interpretation Codes
// See https://www.hl7.org/fhir/valueset-observation-interpretation.html

enum FHIRInterpretation: String {
    case normal = "N"
    case high = "H"
    case criticallyHigh = "HH"
    case low = "L"
    case criticallyLow = "LL"
    case abnormal = "A"
    case criticallyAbnormal = "AA"

    init?(code: String?) {
        guard let code = code?.uppercased() else { return nil }
        switch code {
        case "N", "NORMAL": self = .normal
        case "H", "HIGH": self = .high
        case "HH": self = .criticallyHigh
        case "L", "LOW": self = .low
        case "LL": self = .criticallyLow
        case "A", "ABNORMAL": self = .abnormal
        case "AA": self = .criticallyAbnormal
        default: return nil
        }
    }

    var isAbnormal: Bool {
        switch self {
        case .normal: return false
        case .high, .criticallyHigh, .low, .criticallyLow, .abnormal, .criticallyAbnormal: return true
        }
    }

    var color: Color {
        switch self {
        case .normal: return .green
        case .high: return .orange
        case .criticallyHigh: return .red
        case .low: return .blue
        case .criticallyLow: return .red
        case .abnormal: return .orange
        case .criticallyAbnormal: return .red
        }
    }
}

// MARK: - FHIR Goal Lifecycle Status
// See https://www.hl7.org/fhir/valueset-goal-status.html

enum FHIRGoalStatus: String {
    case proposed, planned, accepted, active
    case onHold = "on-hold"
    case completed, cancelled
    case enteredInError = "entered-in-error"
    case rejected

    init?(rawStatus: String?) {
        guard let raw = rawStatus?.lowercased() else { return nil }
        self.init(rawValue: raw)
    }

    var color: Color {
        switch self {
        case .active: return .green
        case .completed: return .blue
        case .cancelled, .enteredInError, .rejected: return .red
        case .onHold: return .orange
        case .proposed, .planned, .accepted: return .gray
        }
    }
}

// MARK: - FHIR Diagnostic Report Status
// See https://www.hl7.org/fhir/valueset-diagnostic-report-status.html

enum FHIRDiagnosticReportStatus: String {
    case registered, partial, preliminary
    case final_ = "final"
    case amended, corrected, appended, cancelled
    case enteredInError = "entered-in-error"
    case unknown

    init?(rawStatus: String?) {
        guard let raw = rawStatus?.lowercased() else { return nil }
        if raw == "final" { self = .final_; return }
        self.init(rawValue: raw)
    }

    var color: Color {
        switch self {
        case .final_, .amended, .corrected, .appended: return .green
        case .preliminary: return .orange
        case .registered, .partial: return .blue
        case .cancelled, .enteredInError: return .red
        case .unknown: return .gray
        }
    }
}

// MARK: - FHIR Encounter Class
// See https://www.hl7.org/fhir/v3/ActEncounterCode/vs.html

enum FHIREncounterClass: String {
    case ambulatory = "AMB"
    case emergency = "EMER"
    case inpatient = "IMP"
    case shortStay = "SS"
    case homeHealth = "HH"
    case virtual = "VR"
    case field = "FLD"

    init?(code: String?) {
        guard let code = code?.uppercased() else { return nil }
        self.init(rawValue: code)
    }

    var icon: String {
        switch self {
        case .ambulatory: return "figure.walk"
        case .emergency: return "cross.case.fill"
        case .inpatient, .shortStay: return "building.2.fill"
        case .homeHealth: return "house.fill"
        case .virtual: return "video.fill"
        case .field: return "map.fill"
        }
    }

    var displayName: String {
        switch self {
        case .ambulatory: return "Ambulatory"
        case .emergency: return "Emergency"
        case .inpatient: return "Inpatient"
        case .shortStay: return "Short Stay"
        case .homeHealth: return "Home Health"
        case .virtual: return "Virtual"
        case .field: return "Field"
        }
    }

    var color: Color {
        switch self {
        case .emergency: return .red
        case .inpatient, .shortStay: return .blue
        case .ambulatory: return .green
        case .virtual: return .purple
        case .homeHealth: return .teal
        case .field: return .orange
        }
    }
}

// MARK: - FHIR Allergy Criticality
// See https://www.hl7.org/fhir/valueset-allergy-intolerance-criticality.html

enum FHIRAllergyCriticality: String {
    case low, high
    case unableToAssess = "unable-to-assess"

    init?(rawValue: String?) {
        guard let raw = rawValue?.lowercased() else { return nil }
        self.init(rawValue: raw)
    }

    var color: Color {
        switch self {
        case .high: return .red
        case .low: return .green
        case .unableToAssess: return .orange
        }
    }
}

// MARK: - FHIR Device Status
// See https://www.hl7.org/fhir/valueset-device-status.html

enum FHIRDeviceStatus: String {
    case active, inactive
    case enteredInError = "entered-in-error"

    init?(rawStatus: String?) {
        guard let raw = rawStatus?.lowercased() else { return nil }
        self.init(rawValue: raw)
    }

    var color: Color {
        switch self {
        case .active: return .green
        case .inactive: return .orange
        case .enteredInError: return .red
        }
    }
}

// MARK: - Metadata Observation Patterns (not clinically meaningful)

enum FHIRObservationMetadata {
    static let metadataPatterns = [
        "measurement time", "measurement device", "measurement location",
        "measurement method", "measurement setting", "measurement comment",
    ]

    static func isMetadata(name: String?) -> Bool {
        guard let name = name?.lowercased() else { return false }
        return metadataPatterns.contains { name.contains($0) }
    }
}
