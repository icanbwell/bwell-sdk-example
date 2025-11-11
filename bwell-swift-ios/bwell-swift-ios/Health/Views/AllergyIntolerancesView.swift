//
//  AllergyIntolerancesView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 30/10/25.
//

import SwiftUI

struct AllergyIntolerancesView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel

    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView("Loading allergy intolerance data...")
            } else {
                List {
                    Section("Allergy Intolerances") {
                        AllergiesView(allergies: viewModel.allergyIntolerances)
                    }

                    Section("Allergy Intolerance Groups") {
                        AllergyIntoleranceGroupsView()
                    }
                }.listStyle(.plain)
            }
        }.task {
            if viewModel.allergyIntolerances.isEmpty {
                await viewModel.getAllergyIntolerances()
            }

            if let _ = viewModel.allergyIntoleranceGroups {
                await viewModel.getAllergyIntoleranceGroups()
            }
        }
    }
}

// MARK: - Allergy Intolerances
private struct AllergiesView: View {
    var allergies: [BWellWrapper.allergyIntolerances]

    var body: some View {
        Group {
            HStack {
                Text("Allergy")
                    .font(.system(size: 18, weight: .semibold))
                Spacer()
                Text("Criticality")
                    .font(.system(size: 18, weight: .semibold))
            }.listRowSeparator(.hidden, edges: .top)

            ForEach(allergies, id: \.id) { entry in
                HStack {
                    if let allergy = entry.code?.coding?.first?.display,
                       let criticality = entry.criticality {
                        Text(allergy)

                        Spacer()

                        Text(criticality)
                            .padding(5)
                            .background(getStatusColor(of: criticality))
                            .foregroundStyle(.white)
                            .fontWeight(.semibold)
                            .clipShape(RoundedRectangle(cornerRadius: 5))
                    }
                }
            }

            /*
             Text("Total: \(viewModel.allergyIntolerances.count)")
                 .font(.headline)
                 .fontWeight(.semibold)
                 .frame(maxWidth: .infinity, alignment: .leading)
                 .listRowSeparator(.hidden, edges: .bottom)
             */
        }
    }
}

extension AllergiesView {
    func getStatusColor(of criticality: String) -> Color {
        if criticality == "high" {
            return .bwellRed
        } else if criticality == "low" {
            return .bwellGreen
        } else if criticality == "unable-to-assess" || criticality == "unknown" {
            return .gray
        } else {
            return .gray
        }
    }
}

// MARK: - Allergy Intolerance Groups
private struct AllergyIntoleranceGroupsView: View {
    var body: some View {
        
    }
}
