//
//  VitalSignsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct VitalSignsView: View {
    @ObservedObject var viewModel: HealthSummaryViewModel

    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView("Loading vital signs data...")
            } else {
                List {
                    Section("Vital Signs") {
                        ForEach(viewModel.vitalSigns, id: \.id) { entry in
                            NavigationLink {
                                VitalSignsDetailView(entry)
                            } label: {
                                Text(entry.code?.text ?? "Title not available")
                            }
                        }
                    }

                    Section("Vital Signs Groups") {

                    }

                }.listStyle(.plain)
            }
        }.task {
            if viewModel.vitalSigns.isEmpty {
                await viewModel.getVialSigns()
            }
        }
    }
}

private struct VitalSignsDetailView: View {
    var vitalSigns: BWellWrapper.vitalSigns

    init(_ vitalSigns: BWellWrapper.vitalSigns) {
        self.vitalSigns = vitalSigns
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Effective date: ", content: vitalSigns.effectiveDateTime?.dateFormatter())
                DetailedItemView(title: "Effective period: ", content: vitalSigns.effectivePeriod?.start)
            }.padding(.bottom, 10)

            DetailedItemView(title: "Summary: ", content: vitalSigns.text?.div?.stripHTML())

            if let notes = vitalSigns.note {
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
        .navigationBarTitle(vitalSigns.code?.text ?? "Title not available")
        .navigationBarTitleDisplayMode(.inline)
    }
}

