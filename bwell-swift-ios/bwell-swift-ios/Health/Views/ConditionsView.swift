//
//  ConditionsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 30/10/25.
//

import SwiftUI

struct ConditionsView: View {
    @ObservedObject var viewModel: HealthSummaryViewModel
    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView("Loading conditions data...")
            } else {
                List {
                    Section("Conditions") {
                        ForEach(viewModel.conditions, id: \.id) { condition in
                            NavigationLink {
                                ConditionsDetailView(condition)
                            } label: {
                                Text("\(condition.code?.text ?? "Title not available")")
                            }
                        }
                    }

                    Section("Condition Groups") {

                    }
                }.listStyle(.plain)
            }
        }.task {
            if viewModel.conditions.isEmpty {
                await viewModel.getConditions()
            }
        }
    }
}

private struct ConditionsDetailView: View {
    var condition: BWellWrapper.condition

    init(_ condition: BWellWrapper.condition) {
        self.condition = condition
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            DetailedItemView(title: "Onset date: ", content: condition.onsetDateTime?.dateFormatter())
            DetailedItemView(title: "Recorded date: ", content: condition.recordedDate?.dateFormatter())
                .padding(.bottom, 10)

            DetailedItemView(title: "Clinical status: ", content: condition.clinicalStatus?.text)
            DetailedItemView(title: "Summary: ", content: condition.code?.text)
                .padding(.bottom, 10)


            if let bodySite = condition.bodySite {
                Text("Body site:")
                    .fontWeight(.semibold)

                ForEach(bodySite, id: \.id) { site in
                    Text(site.coding?.first?.display ?? "")
                }.padding(.bottom, 10)
            }

            if let notes = condition.note {
                Text("Notes:")
                    .font(.title2)
                    .fontWeight(.semibold)
                ForEach(notes, id: \.id) { note in
                    HStack {
                        Text(note.text ?? "Note not available")
                        Spacer()
                        Text(note.time?.dateFormatter() ?? "")
                    }
                }
            }
            Spacer()
        }
        .padding()
        .navigationBarTitle("Condition Information")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    ConditionsView(viewModel: HealthSummaryViewModel())
}

