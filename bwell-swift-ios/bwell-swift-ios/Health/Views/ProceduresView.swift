//
//  ProceduresView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct ProceduresView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var router: NavigationRouter

    var body: some View {
        HealthDataGroupListView(
            groups: viewModel.procedureGroups,
            id: \.id,
            fetch: {
                await viewModel.getProcedureGroups()
            }, rowContent: { group in
                return .init(title: group.name, date: group.performedDate?.dateFormatter())
            }, onSelect: { group in
                if let id = group.id, let coding = group.coding {
                    let groupCode = BWellHealthDataWrapper(id, coding)

                    router.navigate(to: .healthGroupItems(category: .procedure, groupCode: groupCode))
                }
            }
        ).navigationTitle("Procedures")
    }
}

struct ProceduresSheetView: View {
    var procedures: BWellWrapper.procedures

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(procedures.code?.text ?? "Title unavailable")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)
            
            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Performed date: ", content: procedures.performedDateTime?.dateFormatter())
                DetailedItemView(title: "Performed period: ", content: procedures.performedPeriod?.start)
            }.padding(.bottom, 10)

            DetailedItemView(title: "Summary: ", content: procedures.text?.div?.stripHTML())

            if let notes = procedures.note {
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

