//
//  LabsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct LabsView: View {
    @ObservedObject var viewModel: HealthSummaryViewModel

    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView("Loading labs data...")
            } else {
                List {
                    Section("Labs") {
                        ForEach(viewModel.labs, id: \.id) { entry in
                            NavigationLink {
                                LabsDetailView(entry)
                            } label: {
                                Text(entry.code?.text ?? "Title not available")
                            }
                        }
                    }

                    Section("Lab Groups") {

                    }

                }.listStyle(.plain)
            }
        }.task {
            if viewModel.labs.isEmpty {
                await viewModel.getLabs()
            }
        }
    }
}

private struct LabsDetailView: View {
    var labs: BWellWrapper.labs

    init(_ labs: BWellWrapper.labs) {
        self.labs = labs
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Effective date: ", content: labs.effectiveDateTime?.dateFormatter())
                DetailedItemView(title: "Effective period: ", content: labs.effectivePeriod?.start)
            }.padding(.bottom, 10)

            DetailedItemView(title: "Summary: ", content: labs.text?.div?.stripHTML())

            if let notes = labs.note {
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
        .navigationBarTitle(labs.code?.text ?? "Title not available")
        .navigationBarTitleDisplayMode(.inline)
    }
}
