//
//  MedicationsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 03/11/25.
//
import SwiftUI

struct MedicationsView: View {
    @ObservedObject var viewModel: HealthSummaryViewModel

    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView("Loading medication data...")
            } else {
                List {
                    Section("Medications") {
                        ForEach(viewModel.medications, id: \.id) { entry in
                            NavigationLink {
                                MedicationsDetailView(entry)
                            } label: {
                                Text(entry.medicationCodeableConcept?.text ?? "Title not available")
                            }
                        }
                    }

                    Section("Medication Groups") {

                    }

                }.listStyle(.plain)
            }
        }.task {
            if viewModel.medications.isEmpty {
                await viewModel.getMedicationStatements()
            }
        }
    }
}

private struct MedicationsDetailView: View {
    var medications: BWellWrapper.medications

    init(_ medications: BWellWrapper.medications) {
        self.medications = medications
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
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
        .navigationBarTitle(medications.medicationCodeableConcept?.text ?? "Title not available")
        .navigationBarTitleDisplayMode(.inline)
    }
}

