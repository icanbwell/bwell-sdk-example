//
//  LabsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct LabsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var router: NavigationRouter

    var body: some View {
        HealthDataGroupListView(
            groups: viewModel.labGroups,
            fetch: {
                await viewModel.getLabGroups()
            }, rowContent: { group in
                return .init(title: group.name, date: group.effectiveDateTime?.dateFormatter())
            }, onSelect: { group in
                if let id = group.id, let coding = group.coding {
                    let groupCode = BWellHealthDataWrapper(id, coding)

                    router.navigate(to: .healthGroupItems(category: .labs, groupCode: groupCode))
                }
            }
        ).navigationTitle("Labs")
    }
}

struct LabsSheetView: View {
    var labs: BWellWrapper.labs

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(labs.code?.text ?? "Title unavailable")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Effective date: ", content: labs.effectiveDateTime?.dateFormatter())
                DetailedItemView(title: "Effective period: ", content: labs.effectivePeriod?.start)
            }.padding(.bottom, 10)

            DetailedItemView(title: "Summary: ", content: labs.text?.div?.stripHTML())

            if let notes = labs.note {
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
