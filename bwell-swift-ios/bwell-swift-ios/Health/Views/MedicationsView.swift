//
//  MedicationsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct MedicationsView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var router: NavigationRouter

    var body: some View {
        HealthDataGroupListView(
            groups: viewModel.medicationGroups,
            id: \.id,
            fetch: {
                await viewModel.getMedicationGroups()
            }, rowContent: { group in
                return .init(title: group.name, date: group.authoredOn?.dateFormatter())
            }, onSelect: { group in
                if let id = group.id, let coding = group.coding {
                    let groupCode = BWellHealthDataWrapper(id, coding)

                    router.navigate(to: .healthGroupItems(category: .medications, groupCode: groupCode))
                }
            }
        ).navigationTitle("Medications")
    }
}

struct MedicationsSheetView: View {
    var medications: BWellWrapper.medications

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(medications.medicationCodeableConcept?.text ?? "Title unavailable")
                .font(.headline)
                .fontWeight(.medium)
                .padding(.vertical, 20)

            VStack(alignment: .leading, spacing: 5) {
                DetailedItemView(title: "Period start: ", content: medications.effectivePeriod?.start?.dateFormatter())
                DetailedItemView(title: "Period end: ", content: medications.effectivePeriod?.end?.dateFormatter())
            }.padding(.bottom, 10)

            DetailedItemView(title: "Status: ", content: medications.status)
            if let reasonCode = medications.reasonCode {
                ForEach(reasonCode, id: \.id) { reason in
                    DetailedItemView(title: "Reason for medication: ", content: reason.text)
                }
            }

            if let dosage = medications.dosage {
                ForEach(dosage, id: \.id) { dose in
                    DetailedItemView(title: "Dosage: ", content: dose.text)
                }
            }
            Spacer()
        }
        .padding()
        .presentationDragIndicator(.visible)
        .presentationDetents([.medium])
        .onAppear {
            print("DEBUG: MedicationsSheetView appeared")
            print("DEBUG: Medication title: \(medications.medicationCodeableConcept?.text ?? "nil")")
            print("DEBUG: Medication status: \(medications.status ?? "nil")")
        }
    }
}

