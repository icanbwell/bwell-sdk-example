//
//  CarePlansView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 30/10/25.
//

import Foundation
import SwiftUI

struct CarePlansView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var router: NavigationRouter

    var body: some View {
        HealthDataGroupListView(
            groups: viewModel.carePlanGroups,
            id: \.id,
            fetch: {
                await viewModel.getCarePlanGroups()
            }, rowContent: { group in
                return .init(title: group.name, date: group.period?.start?.dateFormatter())
            }, onSelect: { group in
                if let id = group.id, let coding = group.coding {
                    let groupCode = BWellHealthDataWrapper(id, coding)

                    router.navigate(to: .healthGroupItems(category: .carePlan, groupCode: groupCode))
                }
            }
        ).navigationTitle("Care Plans")
    }
}

// MARK: - Care Plan Sheet View
struct CarePlanSheetView: View {
    var carePlan: BWellWrapper.carePlan

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(carePlan.title ?? "Title unavailable")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

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
        .presentationDragIndicator(.visible)
        .presentationDetents([.medium])
    }
}
