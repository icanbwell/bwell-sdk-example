//
//  QuestionnaireResponsesView.swift
//  bwell-swift-ios
//
//  Displays questionnaire responses from sdk.questionnaire.getQuestionnaireResponses().
//

import SwiftUI
import BWellSDK

struct QuestionnaireResponsesView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var responses: [BWell.QuestionnaireResponse] = []
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var selectedResponse: BWell.QuestionnaireResponse?

    var body: some View {
        ZStack {
            if isLoading && responses.isEmpty {
                ProgressView("Loading questionnaires...")
            } else if let error = errorMessage {
                ContentUnavailableView("Error", systemImage: "exclamationmark.triangle", description: Text(error))
            } else if responses.isEmpty {
                ContentUnavailableView("No Questionnaires", systemImage: "doc.questionmark", description: Text("No questionnaire responses found."))
            } else {
                List(responses) { response in
                    Button {
                        selectedResponse = response
                    } label: {
                        QuestionnaireResponseRow(response: response)
                    }
                    .buttonStyle(.plain)
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Questionnaires")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .refreshable {
            await loadResponses()
        }
        .task {
            guard responses.isEmpty else { return }
            await loadResponses()
        }
        .sheet(item: $selectedResponse) { response in
            NavigationStack {
                QuestionnaireResponseDetailView(response: response)
            }
            .presentationDragIndicator(.visible)
        }
    }

    private func loadResponses() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoading = true
        errorMessage = nil
        do {
            let request = BWell.QuestionnaireResponsesRequest(page: 0)
            let response = try await sdk.questionnaire.getQuestionnaireResponses(request)
            responses = response?.entry?.compactMap { $0.resource } ?? []
        } catch {
            errorMessage = "Failed to load questionnaire responses."
        }
        isLoading = false
    }
}

// MARK: - Row

private struct QuestionnaireResponseRow: View {
    let response: BWell.QuestionnaireResponse

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(response.questionnaire ?? "Questionnaire")
                .font(.body)
                .fontWeight(.medium)
                .lineLimit(2)

            if let status = response.status {
                Text(status)
                    .font(.caption)
                    .fontWeight(.medium)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(statusColor(status).opacity(0.15))
                    .foregroundStyle(statusColor(status))
                    .clipShape(Capsule())
            }

            if let meta = response.meta, let lastUpdated = meta.lastUpdated {
                Text(lastUpdated.dateFormatter())
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 4)
    }

    private func statusColor(_ status: String) -> Color {
        switch status.lowercased() {
        case "completed": return .green
        case "in-progress": return .blue
        case "amended": return .orange
        case "stopped": return .red
        default: return .gray
        }
    }
}

// MARK: - Detail View

struct QuestionnaireResponseDetailView: View {
    let response: BWell.QuestionnaireResponse

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // Header
                VStack(alignment: .leading, spacing: 8) {
                    Text(response.questionnaire ?? "Questionnaire Response")
                        .font(.title3)
                        .fontWeight(.bold)

                    if let status = response.status {
                        Text(status.capitalized)
                            .font(.caption)
                            .fontWeight(.semibold)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(Color.bwellPurple.opacity(0.15))
                            .foregroundStyle(.bwellPurple)
                            .clipShape(Capsule())
                    }
                }
                .padding(.horizontal)
                .padding(.top)

                Divider()

                // Items
                if let items = response.item, !items.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        ForEach(items.indices, id: \.self) { index in
                            QuestionnaireItemView(item: items[index], depth: 0)
                        }
                    }
                    .padding(.horizontal)
                } else {
                    Text("No response items.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .padding(.horizontal)
                }
            }
            .padding(.bottom)
        }
        .navigationTitle("Response Detail")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - Recursive Item View

private struct QuestionnaireItemView: View {
    let item: BWell.QuestionnaireResponse.QuestionnaireResponseItem
    let depth: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            if let text = item.text, !text.isEmpty {
                Text(text)
                    .font(depth == 0 ? .subheadline : .caption)
                    .fontWeight(depth == 0 ? .semibold : .regular)
                    .foregroundStyle(depth == 0 ? .primary : .secondary)
            }

            // Answers
            if let answers = item.answer, !answers.isEmpty {
                ForEach(answers.indices, id: \.self) { index in
                    AnswerValueView(answer: answers[index])
                }
            }

            // Nested items
            if let nestedItems = item.item, !nestedItems.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    ForEach(nestedItems.indices, id: \.self) { index in
                        QuestionnaireItemView(item: nestedItems[index], depth: depth + 1)
                    }
                }
                .padding(.leading, 12)
            }
        }
        .padding(.vertical, 2)
    }
}

private struct AnswerValueView: View {
    let answer: BWell.QuestionnaireResponse.QuestionnaireResponseAnswer

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: "arrow.turn.down.right")
                .font(.caption2)
                .foregroundStyle(.tertiary)
            Text(displayValue)
                .font(.caption)
                .foregroundStyle(.primary)
        }
    }

    private var displayValue: String {
        if let str = answer.valueString { return str }
        if let bool = answer.valueBoolean { return bool ? "Yes" : "No" }
        if let int = answer.valueInteger { return "\(int)" }
        if let decimal = answer.valueDecimal { return "\(decimal)" }
        if let date = answer.valueDate { return date.dateFormatter() }
        if let dateTime = answer.valueDateTime { return dateTime.dateFormatter() }
        if let uri = answer.valueUri { return uri }
        if let coding = answer.valueCoding {
            return coding.display ?? coding.code ?? "Coded value"
        }
        if let qty = answer.valueQuantity {
            let val = qty.value.map { "\($0)" } ?? ""
            let unit = qty.unit ?? ""
            return "\(val) \(unit)".trimmingCharacters(in: .whitespaces)
        }
        return "—"
    }
}
