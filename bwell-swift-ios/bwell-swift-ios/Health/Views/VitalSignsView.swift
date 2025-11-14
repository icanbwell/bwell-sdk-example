//
//  VitalSignsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct VitalSignsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var router: NavigationRouter

    var body: some View {
        HealthDataGroupListView(
            groups: viewModel.vitalSignGroups,
            fetch: {
                await viewModel.getVitalSignGroups()
            }, rowContent: { group in
                return .init(title: group.name, date: group.effectiveDateTime?.dateFormatter())
            }, onSelect: { group in
                if let id = group.id, let coding = group.coding {
                    let groupCode = BWellHealthDataWrapper(id, coding)

                    router.navigate(to: .healthGroupItems(category: .vitalSigns, groupCode: groupCode))
                }
            }
        ).navigationTitle("Vital Signs")
    }
}

struct VitalSignsSheetView: View {
    var vitalSigns: BWellWrapper.vitalSigns

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(vitalSigns.code?.text ?? "Title unavailable")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Effective date: ", content: vitalSigns.effectiveDateTime?.dateFormatter())
                DetailedItemView(title: "Effective period: ", content: vitalSigns.effectivePeriod?.start)
            }.padding(.bottom, 10)

            DetailedItemView(title: "Summary: ", content: vitalSigns.text?.div?.stripHTML())

            if let notes = vitalSigns.note {
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

