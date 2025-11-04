//
//  CarePlansView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 30/10/25.
//

import Foundation
import SwiftUI

struct CarePlansView: View {
    @ObservedObject var viewModel: HealthSummaryViewModel

    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView("Loading care plan data...")
            } else {
                List {
                    Section("Care Plans") {
                        ForEach(viewModel.carePlans, id: \.id)  { plan in
                            NavigationLink {
                                CarePlanDetailView(plan)
                            } label: {
                                Text(plan.title ?? "Title not available")
                            }
                        }
                    }

                    Section("Care Plan Groups") {

                    }
                }.listStyle(.plain)
            }
        }.task {
            if viewModel.carePlans.isEmpty {
                await viewModel.getCarePlans()
            }
        }
    }
}

private struct CarePlanDetailView: View {
    var carePlan: BWellWrapper.carePlan

    init(_ carePlan: BWellWrapper.carePlan) {
        self.carePlan = carePlan
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Plan created in: ", content: carePlan.created?.dateFormatter())
                DetailedItemView(title: "Plan start date: ", content: carePlan.period?.start?.dateFormatter())
                DetailedItemView(title: "Plan end date: ", content: carePlan.period?.end?.dateFormatter())
            }.padding(.bottom, 10)

            DetailedItemView(title: "Description: ", content: carePlan.description ?? "")
            DetailedItemView(display: .vertically, title: "Summary: ", content: carePlan.text?.div?.stripHTML())

            Spacer()
        }
        .padding()
        .navigationBarTitle(carePlan.title ?? "Title not available")
        .navigationBarTitleDisplayMode(.inline)
    }
}
