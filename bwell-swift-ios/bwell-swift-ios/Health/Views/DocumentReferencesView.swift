//
//  DocumentReferencesView.swift
//  bwell-swift-ios
//
//  Displays clinical document references.
//

import SwiftUI
import BWell

struct DocumentReferencesView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var documents: [BWell.DocumentReference] = []
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        ZStack {
            if isLoading && documents.isEmpty {
                ProgressView("Loading documents...")
            } else if let error = errorMessage {
                VStack(spacing: 8) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)
                    Text(error)
                        .foregroundStyle(.red)
                }
            } else if documents.isEmpty {
                VStack(spacing: 8) {
                    Image(systemName: "doc.text")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)
                    Text("No documents found.")
                        .foregroundStyle(.secondary)
                }
            } else {
                List(documents, id: \.id) { document in
                    VStack(alignment: .leading, spacing: 6) {
                        Text(document.type?.text ?? document.description ?? "Document")
                            .font(.headline)

                        if let status = document.status {
                            Text(status)
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(.bwellBlue.opacity(0.2))
                                .clipShape(Capsule())
                        }

                        if let date = document.date {
                            Text(date.dateFormatter())
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }

                        if let categories = document.category, !categories.isEmpty {
                            HStack {
                                ForEach(categories, id: \.id) { category in
                                    Text(category.text ?? "")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Documents")
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .task {
            guard documents.isEmpty else { return }
            await loadDocuments()
        }
    }

    private func loadDocuments() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        do {
            let request = BWell.DocumentReferencesRequest(page: 0)
            let response = try await sdk.health.getDocumentReferences(request)
            documents = response.entry?.compactMap { $0.resource } ?? []
        } catch {
            errorMessage = "Failed to load documents."
        }
        isLoading = false
    }
}
