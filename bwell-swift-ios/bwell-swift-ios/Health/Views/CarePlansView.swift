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
                        carePlanDetailView
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

    var carePlanDetailView: some View {
        Group {
            ForEach(viewModel.carePlans, id: \.id)  { plan in
                NavigationLink {
                    CarePlanDetailView(title: plan.title,
                                       description: plan.description,
                                       summary: plan.text?.div,
                                       planStartDate: plan.period?.start,
                                       planEndDate: plan.period?.end,
                                       planCreationDate: plan.created
                    )
                } label: {
                    Text(plan.title ?? "Plan with no title")
                }
            }
        }
    }
}

private struct CarePlanDetailView: View {
    var title: String?
    var description: String?
    var summary: String?
    var planStartDate: String?
    var planEndDate: String?
    var planCreationDate: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            VStack(alignment: .leading, spacing: 5) {
                InformationView(title: "Plan created in: ", content: planCreationDate?.dateFormatter())
                InformationView(title: "Plan start date: ", content: planStartDate?.dateFormatter())
                InformationView(title: "Plan end date: ", content: planEndDate?.dateFormatter())

            }

            Text("")
                .frame(height: 20)

            InformationView(display: .vertically, title: "Description: ", content: description ?? "")
            InformationView(display: .vertically, title: "Summary: ", content: summary?.stripHTML())
            Spacer()
        }
        .padding()
        .navigationBarTitle(title ?? "No title available")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct InformationView: View {
    enum Direction {
        case vertically, horizontally
    }
    var display: Direction = .horizontally
    var title: String
    var content: String?

    var body: some View {
        if display == .vertically {
            VStack(alignment: .leading, spacing: 5) {
                Text(title)
                    .frame(alignment: .leading)
                    .fontWeight(.semibold)

                Text(content ?? "")
            }
        } else {
            HStack(spacing: 0) {
                Text(title)
                    .frame(alignment: .leading)
                    .fontWeight(.semibold)

                Text(content ?? "")
                Spacer()
            }
        }
    }
}

#Preview {
    // CarePlansView(viewModel: HealthSummaryViewModel())
    let summary = "<div xmlns=\"http://www.w3.org/1999/xhtml\">Comprehensive care plan for managing diabetes, including diet, exercise, and medication management. The goal is to maintain healthy blood sugar levels. Activities include exercise therapy and dietary modifications. The plan is in progress and the patient is responding well.</div>"
    CarePlanDetailView(description: "Weight management plan",
                       summary: summary,
                       planStartDate: "2018-02-01T10:31:10.000Z",
                       planEndDate: "2019-06-01T10:30:10.000Z")
}
