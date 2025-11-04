//
//  ProceduresView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct ProceduresView: View {
    @ObservedObject var viewModel: HealthSummaryViewModel

    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView("Loading procedures data...")
            } else {
                List {
                    Section("Procedures") {
                        ForEach(viewModel.procedures, id: \.id) { entry in
                            NavigationLink {
                                ProceduresDetailView(entry)
                            } label: {
                                Text(entry.code?.text ?? "Title not available")
                            }
                        }
                    }

                    Section("Procedure Groups") {

                    }
                }.listStyle(.plain)
            }
        }.task {
            if viewModel.procedures.isEmpty {
                await viewModel.getProcedures()
            }
        }
    }
}

private struct ProceduresDetailView: View {
    var procedures: BWellWrapper.procedures

    init(_ procedures: BWellWrapper.procedures) {
        self.procedures = procedures
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Performed date: ", content: procedures.performedDateTime?.dateFormatter())
                DetailedItemView(title: "Performed period: ", content: procedures.performedPeriod?.start)
            }.padding(.bottom, 10)

            DetailedItemView(title: "Summary: ", content: procedures.text?.div?.stripHTML())

            if let notes = procedures.note {
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
        .navigationBarTitle(procedures.code?.text ?? "Title not available")
        .navigationBarTitleDisplayMode(.inline)
    }
}

