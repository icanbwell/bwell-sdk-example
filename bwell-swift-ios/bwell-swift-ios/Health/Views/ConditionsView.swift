//
//  ConditionsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 30/10/25.
//

import SwiftUI

struct ConditionsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var router: NavigationRouter

    var body: some View {
        HealthDataGroupListView(
            groups: viewModel.conditionGroups,
            id: \.id,
            fetch: {
                await viewModel.getConditionGroups()
            }, rowContent: { group in
                return .init(title: group.name, date: group.recordedDate?.dateFormatter())
            }, onSelect: { group in
                if let id = group.id, let coding = group.coding {
                    let groupCode = BWellHealthDataWrapper(id, coding)

                    router.navigate(to: .healthGroupItems(category: .condition, groupCode: groupCode))
                }
            }
        ).navigationTitle("Conditions")
    }
}

struct ConditionSheetView: View {
    var condition: BWellWrapper.condition

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(condition.code?.text ?? "Title unavailable")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

            DetailedItemView(title: "Onset date: ", content: condition.onsetDateTime?.dateFormatter())
            DetailedItemView(title: "Recorded date: ", content: condition.recordedDate?.dateFormatter())
                .padding(.bottom, 10)

            DetailedItemView(title: "Clinical status: ", content: condition.clinicalStatus?.text)
            DetailedItemView(title: "Summary: ", content: condition.code?.text)
                .padding(.bottom, 10)


            if let bodySite = condition.bodySite {
                Text("Body site:")
                    .fontWeight(.semibold)

                ForEach(bodySite, id: \.id) { site in
                    Text(site.coding?.first?.display ?? "")
                }.padding(.bottom, 10)
            }

            if let notes = condition.note {
                Text("Notes:")
                    .font(.title2)
                    .fontWeight(.semibold)
                ForEach(notes, id: \.id) { note in
                    HStack {
                        Text(note.text ?? "Note not available")
                        Spacer()
                        Text(note.time?.dateFormatter() ?? "")
                    }
                }
            }
            Spacer()
        }
        .padding()
        .presentationDragIndicator(.visible)
        .presentationDetents([.medium])
    }
}

