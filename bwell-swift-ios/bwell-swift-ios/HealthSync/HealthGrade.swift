//
//  HealthGrade.swift
//  bwell-swift-ios
//
//  Ported from ui-platform's mfe-devices body-systems/constants.ts
//  (GRADE_THRESHOLDS/GRADE_COLOR_MAP/scoreToGrade) - same A-F thresholds and
//  color intent, simplified to SwiftUI system colors. Shared by the Body
//  Score tab and its detail page so both grade a score identically.
//

import SwiftUI

enum HealthGrade: Equatable {
    case a, b, c, d, f

    /// Matches ui-platform's GRADE_THRESHOLDS constant object verbatim.
    private enum Threshold {
        static let a: Double = 90
        static let b: Double = 75
        static let c: Double = 60
        static let d: Double = 45
    }

    static func from(score: Double) -> HealthGrade {
        switch score {
        case Threshold.a...: return .a
        case Threshold.b..<Threshold.a: return .b
        case Threshold.c..<Threshold.b: return .c
        case Threshold.d..<Threshold.c: return .d
        default: return .f
        }
    }

    var label: String {
        switch self {
        case .a: return "Excellent"
        case .b: return "Good"
        case .c: return "Fair"
        case .d: return "Low"
        case .f: return "Poor"
        }
    }

    var color: Color {
        switch self {
        case .a, .b: return .green
        case .c: return .yellow
        case .d: return .orange
        case .f: return .red
        }
    }
}

/// "Excellent Performance" pill, colored by grade.
struct HealthGradeBadge: View {
    let grade: HealthGrade

    var body: some View {
        Text("\(grade.label) Performance")
            .font(.caption).fontWeight(.semibold)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(grade.color.opacity(0.15))
            .foregroundStyle(grade.color)
            .clipShape(Capsule())
    }
}

/// "Poor — Fair — Excellent" gauge with a dot at the score's position -
/// simplified from ui-platform's HealthBar (drops the trend segment,
/// baseline comparison, and hover tooltips; keeps the core "where does this
/// score fall" visual).
struct HealthBarView: View {
    let score: Double

    private var position: CGFloat { CGFloat(max(0, min(100, score))) / 100 }
    private var grade: HealthGrade { .from(score: score) }
    private static let dotSize: CGFloat = 14

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text("Poor").font(.caption2).foregroundStyle(.secondary)
                Spacer()
                Text("Fair").font(.caption2).foregroundStyle(.secondary)
                Spacer()
                Text("Excellent").font(.caption2).foregroundStyle(.secondary)
            }
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    Capsule()
                        .fill(Color(.systemGray5))
                        .frame(height: 4)
                    Circle()
                        .fill(grade.color)
                        .frame(width: Self.dotSize, height: Self.dotSize)
                        .offset(x: max(0, min(geometry.size.width - Self.dotSize, geometry.size.width * position - Self.dotSize / 2)))
                }
            }
            .frame(height: Self.dotSize)
        }
    }
}

/// Simplified from ui-platform's TrendAlertCard - drops the minimize/dismiss
/// state and the "Ask Bailey" AI chat link (out of scope, no chat infra
/// here); keeps the trend direction + rate + comparison-date callout.
struct HealthTrendAlertCard: View {
    let label: String
    let trendDirection: String?
    let trendRate: Double?
    let periodStart: String?

    private var isDeclining: Bool {
        guard let trendDirection else { return false }
        let lowered = trendDirection.lowercased()
        return lowered.contains("declin") || lowered.contains("down")
    }

    private var isStable: Bool {
        guard let trendDirection else { return true }
        let lowered = trendDirection.lowercased()
        return lowered.contains("stable") || lowered.contains("flat")
    }

    private var titleText: String {
        if isStable { return "Your \(label) trend is stable" }
        return isDeclining ? "Your \(label) trend is declining" : "Your \(label) trend is improving"
    }

    /// Keyed to trend direction (declining=red, improving=green,
    /// stable=neutral) - the same "light tint background + saturated
    /// foreground" treatment HealthGradeBadge uses for grade, applied here
    /// to trend instead of leaving the card transparent.
    private var tintColor: Color {
        if isStable { return .secondary }
        return isDeclining ? .red : .green
    }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: "exclamationmark.circle.fill")
                .foregroundStyle(tintColor)
            VStack(alignment: .leading, spacing: 6) {
                Text(titleText)
                    .font(.subheadline).fontWeight(.semibold)
                    .foregroundStyle(.primary)
                if !isStable, let trendRate, trendRate != 0 {
                    HStack(spacing: 4) {
                        Image(systemName: isDeclining ? "arrow.down.right" : "arrow.up.right")
                        Text(String(format: "%.0f", abs(trendRate)))
                        if let periodStart {
                            Text("vs \(periodStart.dateFormatter())")
                        }
                    }
                    .font(.caption).fontWeight(.medium)
                    .foregroundStyle(tintColor)
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(tintColor.opacity(0.1))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .strokeBorder(tintColor.opacity(0.3), lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

/// "Score calculated on <date>" / "Based on data from <start> to <end>" -
/// shared by the Body Score tab and the body-system detail page, which both
/// format the exact same three fields off two different SDK types
/// (HealthScore/BodySystemScore) that don't otherwise share a protocol.
struct PeriodMetadataView: View {
    let calculationDate: String?
    let periodStart: String?
    let periodEnd: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            if let calculationDate {
                Text("Score calculated on \(calculationDate.dateFormatter())")
            }
            if let periodStart, let periodEnd {
                Text("Based on data from \(periodStart.dateFormatter()) to \(periodEnd.dateFormatter())")
            }
        }
        .font(.caption2)
        .foregroundStyle(.tertiary)
    }
}
