//
//  ImmunizationsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct ImmunizationsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var router: NavigationRouter

    var body: some View {
        HealthDataGroupListView(
            groups: viewModel.immunizationGroups,
            fetch: {
                await viewModel.getImmunizationGroups()
            }, rowContent: { group in
                return .init(title: group.name, date: group.occurrenceDateTime?.dateFormatter())
            }, onSelect: { group in
                if let id = group.id, let coding = group.coding {
                    let groupCode = BWellHealthDataWrapper(id, coding)

                    router.navigate(to: .healthGroupItems(category: .immunization, groupCode: groupCode))
                }
            }
        ).navigationTitle("Immunizations")
    }
}

struct ImmunizationSheetView: View {
    var immunization: BWellWrapper.immunization

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(immunization.vaccineCode?.text ?? "Title unavailable")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Occurance date: ", content: immunization.occurrenceDateTime?.dateFormatter())
                DetailedItemView(title: "Expiration date: ", content: immunization.expirationDate?.dateFormatter())
            }.padding(.bottom, 10)

            DetailedItemView(title: "Administration route: ", content: immunization.route?.text)
            DetailedItemView(title: "Summary: ", content: immunization.text?.div?.stripHTML())
            Spacer()
        }
        .padding()
        .presentationDragIndicator(.visible)
        .presentationDetents([.medium])
    }
}
