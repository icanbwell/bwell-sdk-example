//
//  HealthMetricIcon.swift
//  bwell-swift-ios
//
//  A small, hand-picked SF Symbol lookup for metric/body-system card icons -
//  ported in spirit (not literally) from ui-platform's mfe-devices, which
//  picks an icon per LOINC code from a remote-config JSON with a generic
//  fallback (see lib/metrics/adapters.ts's matchIconToVital). This demo has
//  no such remote config, so this matches on keywords in the metric's
//  display name instead - approximate, not a LOINC registry port. Anything
//  unmatched falls back to a generic trending-chart icon, same shape as the
//  real app's own fallback behavior.
//

import Foundation

enum HealthMetricIcon {
    private static let genericFallback = "chart.line.uptrend.xyaxis"

    /// (keyword, SF Symbol) pairs, checked in order against the lowercased
    /// title - first match wins. Order matters: more specific keywords
    /// (e.g. "blood pressure") are listed before generic ones they'd
    /// otherwise also match (e.g. "blood").
    private static let keywordIcons: [(keyword: String, symbol: String)] = [
        ("blood pressure", "heart.text.square.fill"),
        ("heart rate", "heart.fill"),
        ("heart", "heart.fill"),
        ("step", "figure.walk"),
        ("distance", "figure.walk.motion"),
        ("exercise", "figure.run"),
        ("activity", "flame.fill"),
        ("calorie", "flame.fill"),
        ("energy", "flame.fill"),
        ("sleep", "moon.zzz.fill"),
        ("weight", "scalemass.fill"),
        ("body mass", "scalemass.fill"),
        ("oxygen", "lungs.fill"),
        ("respiratory", "lungs.fill"),
        ("temperature", "thermometer"),
        ("glucose", "drop.fill"),
        ("blood", "drop.fill"),
    ]

    static func symbolName(for title: String?) -> String {
        guard let title, !title.isEmpty else { return genericFallback }
        let lowercased = title.lowercased()
        return keywordIcons.first { lowercased.contains($0.keyword) }?.symbol ?? genericFallback
    }
}
