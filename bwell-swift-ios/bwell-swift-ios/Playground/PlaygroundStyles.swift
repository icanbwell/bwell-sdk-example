//
//  PlaygroundStyles.swift
//  bwell-swift-ios
//
//  Shared look for any feature's Playground screen — ported from the
//  internal bwell-sdk-swift Health Sync sample's CardStyle.swift, unchanged.
//

import SwiftUI

/// Shared card look for Playground endpoint cards.
struct PlaygroundCardStyle: ViewModifier {
    func body(content: Content) -> some View {
        content
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(.secondarySystemGroupedBackground))
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            .shadow(color: .black.opacity(0.06), radius: 4, y: 2)
    }
}

/// Shared input look (text fields + pickers) — taller and more padded than
/// the default `.roundedBorder`, with a soft fill instead of a thin outline.
struct PlaygroundFieldStyle: ViewModifier {
    func body(content: Content) -> some View {
        content
            .padding(.vertical, 9)
            .padding(.horizontal, 10)
            .background(Color(.tertiarySystemGroupedBackground))
            .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 10, style: .continuous)
                    .stroke(Color(.separator), lineWidth: 1)
            )
    }
}

extension View {
    func playgroundCardStyle() -> some View { modifier(PlaygroundCardStyle()) }
    func playgroundFieldStyle() -> some View { modifier(PlaygroundFieldStyle()) }
}

/// Full-width filled button — a Playground screen's one primary action.
struct PlaygroundPrimaryButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.body.weight(.semibold))
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(Color.accentColor.opacity(!isEnabled ? 0.4 : configuration.isPressed ? 0.75 : 1))
            .foregroundStyle(.white)
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

/// Full-width outlined button — secondary actions alongside a primary one.
struct PlaygroundSecondaryButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.body.weight(.medium))
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(Color(.tertiarySystemGroupedBackground))
            .foregroundStyle(Color.accentColor.opacity(!isEnabled ? 0.4 : configuration.isPressed ? 0.6 : 1))
            .overlay(
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .stroke(Color(.separator), lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

/// Compact filled button for inline actions (e.g. a Playground card's Run button).
struct PlaygroundCompactButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.subheadline.weight(.semibold))
            .padding(.vertical, 8)
            .padding(.horizontal, 16)
            .background(Color.accentColor.opacity(!isEnabled ? 0.35 : configuration.isPressed ? 0.75 : 1))
            .foregroundStyle(.white)
            .clipShape(RoundedRectangle(cornerRadius: 9, style: .continuous))
    }
}

extension ButtonStyle where Self == PlaygroundPrimaryButtonStyle {
    static var playgroundPrimary: PlaygroundPrimaryButtonStyle { PlaygroundPrimaryButtonStyle() }
}

extension ButtonStyle where Self == PlaygroundSecondaryButtonStyle {
    static var playgroundSecondary: PlaygroundSecondaryButtonStyle { PlaygroundSecondaryButtonStyle() }
}

extension ButtonStyle where Self == PlaygroundCompactButtonStyle {
    static var playgroundCompact: PlaygroundCompactButtonStyle { PlaygroundCompactButtonStyle() }
}

extension Color {
    /// Softer than pure black for Playground headings - full black reads harshly on iOS.
    static let playgroundHeading = Color(red: 0.12, green: 0.12, blue: 0.16)
}

/// Bordered container for a group of Playground cards - a visible rounded
/// outline + label, distinct from the plain system GroupBox look.
struct PlaygroundGroupBoxStyle: GroupBoxStyle {
    func makeBody(configuration: Configuration) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            configuration.label
            configuration.content
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .fill(Color(.secondarySystemGroupedBackground))
        )
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Color.accentColor.opacity(0.25), lineWidth: 1.5)
        )
    }
}

extension GroupBoxStyle where Self == PlaygroundGroupBoxStyle {
    static var playground: PlaygroundGroupBoxStyle { PlaygroundGroupBoxStyle() }
}

/// Shared vertically-centered, width-capped form shell used by any
/// single-card Playground screen (e.g. a feature's own Setup/Reauthenticate).
struct PlaygroundCenteredForm<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack {
            Spacer()
            VStack(spacing: 16) {
                content
            }
            .frame(maxWidth: 360)
            Spacer()
        }
        .padding(.horizontal, 24)
    }
}
