//
//  PlaygroundNoteBanner.swift
//  bwell-swift-ios
//
//  Small info callout for a Playground group - e.g. a prerequisite note.
//  Feature-agnostic, lives in the shared Playground infra.
//

import SwiftUI

struct PlaygroundNoteBanner: View {
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            Image(systemName: "info.circle.fill")
                .foregroundStyle(Color.accentColor)
            Text(text)
                .font(.footnote)
                .foregroundStyle(.secondary)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 10, style: .continuous)
                .fill(Color.accentColor.opacity(0.08))
        )
    }
}
