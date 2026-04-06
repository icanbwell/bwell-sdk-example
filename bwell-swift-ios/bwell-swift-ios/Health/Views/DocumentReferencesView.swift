//
//  DocumentReferencesView.swift
//  bwell-swift-ios
//
//  Displays clinical document references with expandable detail rows.
//

import SwiftUI
import BWellSDK

struct DocumentReferencesView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var documents: [BWell.DocumentReference] = []
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var expandedIds: Set<String> = []

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
                    DocumentReferenceRow(
                        document: document,
                        isExpanded: expandedIds.contains(document.id ?? ""),
                        onToggle: {
                            withAnimation(.easeInOut(duration: 0.25)) {
                                let id = document.id ?? ""
                                if expandedIds.contains(id) {
                                    expandedIds.remove(id)
                                } else {
                                    expandedIds.insert(id)
                                }
                            }
                        }
                    )
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

// MARK: - Document Reference Row

private struct DocumentReferenceRow: View {
    let document: BWell.DocumentReference
    let isExpanded: Bool
    let onToggle: () -> Void

    private var displayTitle: String {
        document.type?.text ?? document.description ?? "Document"
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Button(action: onToggle) {
                HStack(alignment: .center, spacing: 10) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text(displayTitle)
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundStyle(.primary)
                            .lineLimit(2)
                            .multilineTextAlignment(.leading)

                        if let date = document.date {
                            Text(date.dateFormatter())
                                .font(.caption2)
                                .foregroundStyle(.tertiary)
                        }
                    }

                    Spacer()

                    if let status = document.status {
                        Text(status)
                            .font(.caption2)
                            .fontWeight(.medium)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(.bwellBlue.opacity(0.15))
                            .foregroundStyle(.bwellBlue)
                            .clipShape(Capsule())
                    }

                    Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                        .font(.caption2)
                        .fontWeight(.semibold)
                        .foregroundStyle(.tertiary)
                }
            }
            .buttonStyle(.plain)

            if isExpanded {
                Divider().padding(.top, 8)
                DocumentReferenceDetail(document: document)
                    .padding(.top, 6)
                    .padding(.leading, 20)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
        .padding(.vertical, 6)
    }
}

// MARK: - Document Reference Detail (Expanded)

private struct DocumentReferenceDetail: View {
    let document: BWell.DocumentReference

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            // Description (if different from title used in summary)
            if let description = document.description,
               let typeText = document.type?.text,
               description != typeText {
                detailRow("Description", description)
            }

            // Categories
            if let categories = document.category, !categories.isEmpty {
                let categoryText = categories.compactMap { $0.text ?? $0.coding?.first?.display }.joined(separator: ", ")
                if !categoryText.isEmpty {
                    detailRow("Category", categoryText)
                }
            }

            // Author
            if let authors = document.author, !authors.isEmpty {
                let authorNames = authors.compactMap { $0.display }.joined(separator: ", ")
                if !authorNames.isEmpty {
                    detailRow("Author", authorNames)
                }
            }

            // Date
            if let date = document.date {
                detailRow("Date", date.dateFormatter())
            }

            // Content details
            if let contents = document.content, !contents.isEmpty {
                Divider().padding(.vertical, 4)
                Text("Content")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundStyle(.secondary)

                ForEach(Array(contents.enumerated()), id: \.offset) { index, content in
                    if let attachment = content.attachment {
                        if let contentType = attachment.contentType {
                            detailRow("Format", contentType)
                        }
                        if let size = attachment.size {
                            detailRow("Size", formatFileSize(size))
                        }
                        if let title = attachment.title {
                            detailRow("Title", title)
                        }
                    }
                    if let format = content.format {
                        detailRow("Encoding", format.display ?? format.code)
                    }
                    if index < contents.count - 1 {
                        Divider().padding(.vertical, 2)
                    }
                }
            }

        }
    }

    @ViewBuilder
    private func detailRow(_ label: String, _ value: String?) -> some View {
        if let value, !value.isEmpty {
            HStack(alignment: .top, spacing: 6) {
                Text(label)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .frame(width: 80, alignment: .trailing)
                Text(value)
                    .font(.caption)
                    .foregroundStyle(.primary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
    }

    private func formatFileSize(_ bytes: Int) -> String {
        if bytes < 1024 {
            return "\(bytes) B"
        } else if bytes < 1024 * 1024 {
            return String(format: "%.1f KB", Double(bytes) / 1024.0)
        } else {
            return String(format: "%.1f MB", Double(bytes) / (1024.0 * 1024.0))
        }
    }
}
