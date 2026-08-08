//
//  PlaygroundCard.swift
//  bwell-swift-ios
//
//  Generic per-endpoint card for any feature's Playground screen: title,
//  "blocked" badge, Run button/spinner, explanation, optional feature-specific
//  input, and inline result. Layout ported from the internal bwell-sdk-swift
//  Health Sync sample's `PlaygroundView.card(for:)`, generalized over any
//  `PlaygroundEndpoint` and an injected input view so feature-specific inputs
//  (a picker, a text field) don't require forking this layout.
//

import SwiftUI

struct PlaygroundCard<Endpoint: PlaygroundEndpoint, Input: View>: View {
    let endpoint: Endpoint
    let state: PlaygroundCardState
    let onRun: () -> Void
    @ViewBuilder var input: () -> Input

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(endpoint.title).font(.headline).foregroundStyle(Color.playgroundHeading)
                if endpoint.isBlocked {
                    Text("blocked").font(.caption2).padding(4)
                        .background(.orange.opacity(0.2)).clipShape(Capsule())
                }
                Spacer()
                if case .loading = state {
                    ProgressView()
                } else {
                    Button(endpoint.isBlocked ? "Blocked" : "Run", action: onRun)
                        .buttonStyle(.playgroundCompact)
                        .disabled(endpoint.isBlocked)
                }
            }

            Text(endpoint.explanation).font(.footnote).foregroundStyle(.secondary)

            if let blockedReason = endpoint.blockedReason {
                Text(blockedReason).font(.footnote).foregroundStyle(.orange)
            }

            input()

            resultView(for: state)
        }
        .playgroundCardStyle()
    }

    @ViewBuilder
    private func resultView(for state: PlaygroundCardState) -> some View {
        switch state {
        case .idle, .loading:
            EmptyView() // loading is shown in the title row instead
        case .success(let text):
            ScrollView {
                Text(text).font(.system(.footnote, design: .monospaced)).textSelection(.enabled)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .frame(maxHeight: 220)
        case .error(let message):
            Text(message).font(.footnote).foregroundStyle(.red).textSelection(.enabled)
        }
    }
}

extension PlaygroundCard where Input == EmptyView {
    init(endpoint: Endpoint, state: PlaygroundCardState, onRun: @escaping () -> Void) {
        self.init(endpoint: endpoint, state: state, onRun: onRun, input: { EmptyView() })
    }
}
