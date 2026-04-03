//
//  ChartModels.swift
//  bwell-swift-ios
//
//  Data models and helpers for SwiftUI Charts integration.
//

import Foundation
import SwiftUI
import BWell

struct ChartDataPoint: Identifiable {
    let id = UUID()
    let date: Date
    let value: Double
    let unit: String
    let inRange: Bool
}

enum ChartTimeRange: String, CaseIterable {
    case thirtyDays = "30D"
    case ninetyDays = "90D"
    case oneYear = "1Y"
    case all = "All"

    var startDate: Date? {
        let calendar = Calendar.current
        switch self {
        case .thirtyDays: return calendar.date(byAdding: .day, value: -30, to: .now)
        case .ninetyDays: return calendar.date(byAdding: .day, value: -90, to: .now)
        case .oneYear: return calendar.date(byAdding: .year, value: -1, to: .now)
        case .all: return nil
        }
    }
}

extension BWell.Observation {
    func toChartDataPoint() -> ChartDataPoint? {
        // Try valueQuantity first, then fall back to first component's valueQuantity
        let qty = valueQuantity ?? component?.first?.valueQuantity
        guard let qty, let value = qty.value else { return nil }
        let unit = qty.unit ?? ""
        let date = effectiveDateTime?.fhirDate() ?? Date()

        // Check if in reference range
        var inRange = true
        if let refRange = referenceRange?.first {
            if let low = refRange.low?.value, value < low { inRange = false }
            if let high = refRange.high?.value, value > high { inRange = false }
        }

        return ChartDataPoint(date: date, value: value, unit: unit, inRange: inRange)
    }
}

/// Find the first non-nil reference range across a list of observations
func findReferenceRange(in observations: [BWell.Observation]) -> (low: Double?, high: Double?)? {
    for obs in observations {
        if let refRange = obs.referenceRange?.first,
           refRange.low?.value != nil || refRange.high?.value != nil {
            return (low: refRange.low?.value, high: refRange.high?.value)
        }
    }
    return nil
}


