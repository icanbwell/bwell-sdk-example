//
//  EncountersView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct EncountersView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel

    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView("Loading encounters data...")
            } else {
                List {
                    Section("Encounters") {
                        ForEach(viewModel.encounters, id: \.id) { entry in
                            NavigationLink {
                                EncountersDetailView(entry)
                            } label: {
                                Text("Title not available")
                            }
                        }
                    }

                    Section("Encounter Groups") {

                    }

                }.listStyle(.plain)
            }
        }.task {
            if viewModel.encounters.isEmpty {
                await viewModel.getEncounters()
            }
        }
    }
}

private struct EncountersDetailView: View {
    var encounter: BWellWrapper.encounter

    init(_ encounter: BWellWrapper.encounter) {
        self.encounter = encounter
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Encounter start: ", content: encounter.period?.start?.dateFormatter())
                DetailedItemView(title: "Encounter end: ", content: encounter.period?.end?.dateFormatter())
            }.padding(.bottom, 10)

            DetailedItemView(title: "Status: ", content: encounter.status)
            DetailedItemView(title: "Class: ", content: encounter.class?.display)
            DetailedItemView(title: "Hospitalization: ", content: encounter.hospitalization?.dischargeDisposition?.text)

            DetailedItemView(title: "Summary: ", content: encounter.text?.div?.stripHTML())
            Spacer()
        }
        .padding()
        .navigationBarTitle("Title not available")
        .navigationBarTitleDisplayMode(.inline)
    }
}

