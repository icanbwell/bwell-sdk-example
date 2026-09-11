//
//  MetricCardView.swift
//  bwell-swift-ios
//
//  Shared card shape for the Metrics and Body Score grids - icon, title,
//  big value + unit, and an "Updated on <date>" footer. Ported in spirit
//  from ui-platform's mfe-devices DeviceVitalsCard (2-column grid of
//  rounded cards), simplified to this demo's needs (no reference-range
//  chip, no "just synced" highlight).
//

import SwiftUI

struct MetricCardView: View {
    let title: String
    let value: String?
    let unit: String?
    let footer: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Image(systemName: HealthMetricIcon.symbolName(for: title))
                .font(.title2)
                .foregroundStyle(Color.accentColor)

            Text(title)
                .font(.subheadline).fontWeight(.semibold)
                .foregroundStyle(.primary)
                .lineLimit(2)
                .fixedSize(horizontal: false, vertical: true)

            if let value {
                VStack(alignment: .leading, spacing: 2) {
                    Text(value)
                        .font(.title2).fontWeight(.bold)
                        .foregroundStyle(.primary)
                    if let unit, !unit.isEmpty {
                        Text(unit)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }

            if let footer {
                Divider()
                Text(footer)
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, minHeight: 150, alignment: .topLeading)
        .background(Color(.secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .healthCardBorderStyle()
    }
}

/// Border + shadow + pointer-hover highlight shared by the Metrics grid
/// card and the Body Score row card - extracted since both used the exact
/// same three modifiers verbatim.
private struct HealthCardBorderStyle: ViewModifier {
    func body(content: Content) -> some View {
        content
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .strokeBorder(Color(.separator), lineWidth: 1)
            )
            .shadow(color: .black.opacity(0.06), radius: 10, x: 0, y: 4)
            // Highlights the card under a pointer (trackpad/Magic Keyboard
            // on iPad) - the closest native equivalent to ui-platform's CSS
            // :hover/:focus box-shadow. Has no visible effect on iPhone
            // touch; CardPressButtonStyle below covers touch-driven feedback.
            .hoverEffect(.highlight)
    }
}

extension View {
    func healthCardBorderStyle() -> some View {
        modifier(HealthCardBorderStyle())
    }
}

/// Scales down and dims slightly while pressed - the touch-driven analog of
/// ui-platform's DeviceVitalsCard :hover/:focus state (stronger shadow,
/// darker border, hover background) for a device with no pointer.
struct CardPressButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1)
            .opacity(configuration.isPressed ? 0.85 : 1)
            .animation(.easeOut(duration: 0.15), value: configuration.isPressed)
    }
}

/// Two-column grid, used identically by both the Metrics and Body Score tabs.
struct MetricCardGrid<Item: Identifiable, Content: View>: View {
    let items: [Item]
    @ViewBuilder let content: (Item) -> Content

    private let columns = [GridItem(.flexible(), spacing: 12), GridItem(.flexible(), spacing: 12)]

    var body: some View {
        LazyVGrid(columns: columns, spacing: 12) {
            ForEach(items) { item in
                content(item)
            }
        }
    }
}
