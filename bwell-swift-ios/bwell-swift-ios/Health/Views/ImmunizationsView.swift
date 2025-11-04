//
//  ImmunizationsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct ImmunizationsView: View {
    @ObservedObject var viewModel: HealthSummaryViewModel

    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView("Loading immnuzations data...")
            } else {
                List {
                    Section("Immunizations") {
                        ForEach(viewModel.immunizations, id: \.id) { entry in
                            NavigationLink {
                                ImmunizationDetailView(entry)
                            } label: {
                                Text(entry.vaccineCode?.text ?? "Title not available")
                            }
                        }
                    }

                    Section("Immunization Groups") {

                    }

                }.listStyle(.plain)
            }
        }.task {
            if viewModel.immunizations.isEmpty {
                await viewModel.getImmunizations()
            }
        }
    }
}

private struct ImmunizationDetailView: View {
    var immunization: BWellWrapper.immunization

    init(_ immunization: BWellWrapper.immunization) {
        self.immunization = immunization
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Occurance date: ", content: immunization.occurrenceDateTime?.dateFormatter())
                DetailedItemView(title: "Expiration date: ", content: immunization.expirationDate?.dateFormatter())
            }.padding(.bottom, 10)

            DetailedItemView(title: "Administration route: ", content: immunization.route?.text)
            DetailedItemView(title: "Summary: ", content: immunization.text?.div?.stripHTML())
            Spacer()
        }
        .padding()
        .navigationBarTitle(immunization.vaccineCode?.text ?? "Title not available")
        .navigationBarTitleDisplayMode(.inline)
    }
}
