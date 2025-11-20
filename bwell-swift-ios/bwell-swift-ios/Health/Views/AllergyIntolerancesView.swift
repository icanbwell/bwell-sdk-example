//
//  AllergyIntolerancesView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 30/10/25.
//

import SwiftUI

struct AllergyIntolerancesView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var router: NavigationRouter

    var body: some View {
        HealthDataGroupListView(
            groups: viewModel.allergyIntoleranceGroups,
            fetch: {
                await viewModel.getAllergyIntoleranceGroups()
            }, rowContent: { group in
                return .init(title: group.name, date: group.recordedDate)
            }, onSelect: { group in
                if let id = group.id, let coding = group.coding {
                    let groupCode = BWellHealthDataWrapper(id, coding)

                    router.navigate(to: .healthGroupItems(category: .allergyIntolerance, groupCode: groupCode))
                }
            }
        ).navigationTitle("Allergy Intolerances")
    }
}

// MARK: - Allergy Intolerance Sheet View
struct AllergySheetView: View {
    var allergy: BWellWrapper.allergyIntolerances

    var body: some View {
        VStack(alignment: .leading) {
            Text(allergy.code?.coding?.first?.display ?? "Title unavailable")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

            VStack(alignment: .leading, spacing: 10) {
                DetailedItemView(display: .vertically, title: "Category: ", content: allergy.code?.text?.capitalizingFirstLetter())
                DetailedItemView(title: "Date of last occurence: ", content: allergy.onsetDateTime?.dateFormatter())
                DetailedItemView(display: .vertically, title: "Summary: ", content: allergy.text?.div?.stripHTML())
            }

            Spacer()
        }
        .padding(10)
        .presentationDragIndicator(.visible)
        .presentationDetents([.medium])
    }
}

extension AllergySheetView {
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
