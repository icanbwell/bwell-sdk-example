//
//  EncountersView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct EncountersView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var router: NavigationRouter

    var body: some View {
        HealthDataGroupListView(
            groups: viewModel.encounterGroups,
            fetch: {
                await viewModel.getEncounterGroups()
            }, rowContent: { group in
                return .init(title: group.name, date: group.date?.dateFormatter())
            }, onSelect: { group in
                if let id = group.id, let coding = group.coding {
                    let groupCode = BWellHealthDataWrapper(id, coding)

                    router.navigate(to: .healthGroupItems(category: .encounter, groupCode: groupCode))
                }
            }
        ).navigationTitle("Encounters")
    }
}

struct EncountersSheetView: View {
    var encounter: BWellWrapper.encounter

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(encounter.text?.div?.stripHTML() ?? "Title unavailable")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

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
        .presentationDragIndicator(.visible)
        .presentationDetents([.medium])
    }
}

