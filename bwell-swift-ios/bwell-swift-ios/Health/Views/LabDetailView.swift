//
//  LabDetailView.swift
//  bwell-swift-ios
//
//  Two-tab detail view: Overview + "What is it?" (lab knowledge).
//

import SwiftUI
import BWellSDK

struct LabDetailView: View {
    let lab: BWell.Observation
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var selectedTab = 0
    @State private var knowledgeHTML: String?
    @State private var isLoadingKnowledge = false
    @State private var knowledgeError: String?

    var body: some View {
        VStack(spacing: 0) {
            TabSelectorView(tabs: ["Overview", "What is it?"], selectedIndex: $selectedTab)
                .padding(.top, 8)

            TabView(selection: $selectedTab) {
                overviewTab.tag(0)
                knowledgeTab.tag(1)
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
        }
        .navigationTitle(lab.code?.coding?.first?.display ?? lab.code?.text ?? "Lab Detail")
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
    }

    @ViewBuilder
    private var overviewTab: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text(lab.code?.coding?.first?.display ?? lab.code?.text ?? "Title unavailable")
                    .font(.headline)
                    .fontWeight(.medium)
                    .padding(.top, 16)

                // Value
                if let valueQuantity = lab.valueQuantity,
                   let value = valueQuantity.value {
                    let unit = valueQuantity.unit ?? ""
                    DetailedItemView(title: "Value: ",
                                     content: "\(value) \(unit)".trimmingCharacters(in: .whitespaces))
                } else if let valueString = lab.valueString {
                    DetailedItemView(title: "Value: ",
                                     content: valueString)
                }

                // Reference Range
                if let refRange = lab.referenceRange?.first {
                    if let text = refRange.text {
                        DetailedItemView(title: "Reference Range: ",
                                         content: text)
                    } else if let low = refRange.low, let high = refRange.high,
                              let lowValue = low.value, let highValue = high.value {
                        let unit = low.unit ?? high.unit ?? ""
                        DetailedItemView(title: "Reference Range: ",
                                         content: "\(lowValue) - \(highValue) \(unit)".trimmingCharacters(in: .whitespaces))
                    }
                }

                // Interpretation
                if let interpretation = lab.interpretation?.first {
                    let interpretationText = interpretation.text ?? interpretation.coding?.first?.display
                    DetailedItemView(title: "Interpretation: ",
                                     content: interpretationText)
                }

                // Status
                DetailedItemView(title: "Status: ",
                                 content: lab.status)

                // Effective date
                DetailedItemView(title: "Effective date: ",
                                 content: lab.effectiveDateTime?.dateFormatter())

                if let notes = lab.note, !notes.isEmpty {
                    Text("Notes:")
                        .font(.title3)
                        .fontWeight(.semibold)
                        .padding(.top, 8)

                    ForEach(notes, id: \.id) { note in
                        HStack {
                            Text(note.text ?? "Note not available")
                            Spacer()
                            Text(note.time?.dateFormatter() ?? "")
                                .foregroundStyle(.secondary)
                        }
                    }
                }
            }
            .padding(.horizontal)
        }
    }

    @ViewBuilder
    private var knowledgeTab: some View {
        Group {
            if isLoadingKnowledge {
                ProgressView("Loading knowledge...")
            } else if let html = knowledgeHTML {
                HTMLContentView(htmlContent: html)
            } else if let error = knowledgeError {
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)
                    Text(error)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                }
                .padding()
            } else {
                VStack(spacing: 12) {
                    Image(systemName: "doc.text.magnifyingglass")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)
                    Text("No knowledge content available for this lab.")
                        .foregroundStyle(.secondary)
                }
                .padding()
            }
        }
        .task {
            guard knowledgeHTML == nil, !isLoadingKnowledge else { return }
            await loadKnowledge()
        }
    }

    private func loadKnowledge() async {
        guard let sdk = sdkManager.sdk else { return }
        isLoadingKnowledge = true
        do {
            let request = BWell.LabKnowledgeRequest(labId: lab.id)
            let response = try await sdk.health.getLabKnowledge(request)
            knowledgeHTML = response.content
            isLoadingKnowledge = false
        } catch {
            knowledgeError = "Failed to load lab knowledge."
            isLoadingKnowledge = false
        }
    }
}
