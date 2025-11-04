//
//  AllergyIntolerancesView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 30/10/25.
//

import SwiftUI

struct AllergyIntolerancesView: View {
    @ObservedObject var viewModel: HealthSummaryViewModel

    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView("Loading allergy intolerance data...")
            } else {
                VStack(alignment: .leading) {
                    HStack {
                        Text("Allergy")
                            .font(.system(size: 18, weight: .semibold))
                        Spacer()
                        Text("Criticality")
                            .font(.system(size: 18, weight: .semibold))
                    }.padding(.bottom, 5)

                    ForEach(viewModel.allergyIntolerances, id: \.id) { entry in
                        HStack {
                            if let allergy = entry.code?.coding?.first?.display,
                               let criticality = entry.criticality {
                                Text(allergy)
                                Spacer()
                                Text(criticality)
                            }
                        }
                    }

                    Rectangle()
                        .frame(height: 1)
                        .foregroundStyle(.gray)

                    Text("Total: \(viewModel.allergyIntolerances.count)")
                        .font(.headline)
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.bottom, 20)
                    Spacer()
                }
                .padding(.horizontal)
                .padding(.top, 20)
            }
        }.task {
            if viewModel.allergyIntolerances.isEmpty {
                await viewModel.getAllergyIntolerances()
                // await viewModel.getAllergyIntoleranceGroups()
            }
        }
    }
}
