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
import UIKit

struct PlaygroundCard<Endpoint: PlaygroundEndpoint, Input: View>: View {
    let endpoint: Endpoint
    let state: PlaygroundCardState
    /// True when this card's Run button has nothing usable to call with —
    /// e.g. a dropdown sourced live from another endpoint's response came
    /// back empty. The "why" belongs in the endpoint's parameterInfo (info
    /// sheet), not inline text on the card itself.
    var runDisabled: Bool = false
    let onRun: () -> Void
    @ViewBuilder var input: () -> Input

    @State private var showingInfo = false
    @State private var didCopyOutput = false

    private var hasInfo: Bool {
        endpoint.documentationURL != nil || endpoint.parameterInfo != nil
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(endpoint.title).font(.headline).foregroundStyle(Color.playgroundHeading)
                if hasInfo {
                    Button {
                        showingInfo = true
                    } label: {
                        Image(systemName: "info.circle")
                            .foregroundStyle(.secondary)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel("More info about \(endpoint.title)")
                }
                if endpoint.isBlocked {
                    Text("blocked").font(.caption2).padding(4)
                        .background(.orange.opacity(0.2)).clipShape(Capsule())
                }
                Spacer()
                if case .success(let text) = state {
                    Button {
                        UIPasteboard.general.string = text
                        didCopyOutput = true
                        Task {
                            try? await Task.sleep(for: .seconds(1.2))
                            didCopyOutput = false
                        }
                    } label: {
                        Image(systemName: didCopyOutput ? "checkmark" : "doc.on.doc")
                            .foregroundStyle(.secondary)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel("Copy output")
                }
                if case .loading = state {
                    ProgressView()
                } else {
                    Button(endpoint.isBlocked ? "Blocked" : "Run", action: onRun)
                        .buttonStyle(.playgroundCompact)
                        .disabled(endpoint.isBlocked || runDisabled)
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
        .sheet(isPresented: $showingInfo) {
            PlaygroundEndpointInfoSheet(endpoint: endpoint)
        }
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
