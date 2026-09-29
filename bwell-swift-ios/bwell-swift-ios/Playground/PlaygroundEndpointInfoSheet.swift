//
//  PlaygroundEndpointInfoSheet.swift
//  bwell-swift-ios
//
//  Generic "more info" sheet for any PlaygroundCard's (i) button - title,
//  full description, parameter requirements (if any), and a documentation
//  link (if any) opened in-app. Feature-agnostic, works over any
//  PlaygroundEndpoint.
//

import SwiftUI

struct PlaygroundEndpointInfoSheet<Endpoint: PlaygroundEndpoint>: View {
    let endpoint: Endpoint
    @Environment(\.dismiss) private var dismiss
    @State private var showingSafari = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text(endpoint.documentationSummary ?? endpoint.explanation)
                        .font(.body)

                    if let parameterInfo = endpoint.parameterInfo {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Parameters").font(.subheadline.weight(.semibold))
                            Text(parameterInfo).font(.body).foregroundStyle(.secondary)
                        }
                    }

                    if endpoint.documentationURL != nil {
                        Button {
                            showingSafari = true
                        } label: {
                            Text("More info on the doc page")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.playgroundSecondary)
                    }
                }
                .padding()
            }
            .navigationTitle(endpoint.title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
        .sheet(isPresented: $showingSafari) {
            if let url = endpoint.documentationURL {
                SafariView(url: url)
            }
        }
    }
}
