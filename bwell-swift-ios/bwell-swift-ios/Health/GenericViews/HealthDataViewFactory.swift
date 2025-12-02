//
//  HealthDataViewFactory.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 14/11/25.
//
import Foundation
import SwiftUI
import BWellSDK

@MainActor
struct HealthDataFactoryView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel

    let category: HealthDataSummaryModel
    let groupCode: BWellHealthDataWrapper<BWell.Coding>

    init(_ category: HealthDataSummaryModel,
         _ groupCode: BWellHealthDataWrapper<BWell.Coding>) {
        self.category = category
        self.groupCode = groupCode
    }

    @ViewBuilder
    var body: some View {
        ZStack {
            switch category {
            case .allergyIntolerance:
                HealthDataGroupItemsView(
                    entry: viewModel.allergyIntolerances,
                    id: \.id,
                    rowContent: { allergy in
                        return .init(title: allergy.code?.coding?.first?.display,
                                     value: allergy.criticality)
                    }, fetch: {
                        await  viewModel.getAllergyIntolerances(groupCode.entry)
                    }, detailView: { entry in
                        AllergySheetView(allergy: entry)
                    }
                )
                .navigationTitle(category.title)
            case .carePlan:
                HealthDataGroupItemsView(
                    entry: viewModel.carePlans,
                    id: \.id,
                    rowContent: { carePlan in
                        return .init(title: carePlan.title,
                                     date: carePlan.period?.start?.dateFormatter())
                    }, fetch: {
                        await  viewModel.getCarePlans(groupCode.entry)
                    }, detailView: { entry in
                        CarePlanSheetView(carePlan: entry)
                    }
                )
                .navigationTitle(category.title)
            case .condition:
                HealthDataGroupItemsView(
                    entry: viewModel.conditions,
                    id: \.id,
                    rowContent: { condition in
                        return .init(title: condition.code?.text,
                                     date: condition.recordedDate?.dateFormatter())
                    }, fetch: {
                        await  viewModel.getConditions(groupCode.entry)
                    }, detailView: { entry in
                        ConditionSheetView(condition: entry)
                    }
                )
                .navigationTitle(category.title)
            case .encounter:
                HealthDataGroupItemsView(
                    entry: viewModel.encounters,
                    id: \.id,
                    rowContent: { encounter in
                        return .init(title: encounter.text?.status,
                                     date: encounter.period?.start?.dateFormatter())
                    }, fetch: {
                        await  viewModel.getEncounters(groupCode.entry)
                    }, detailView: { entry in
                        EncountersSheetView(encounter: entry)
                    }
                )
                .navigationTitle(category.title)
            case .immunization:
                HealthDataGroupItemsView(
                    entry: viewModel.immunizations,
                    id: \.id,
                    rowContent: { immunization in
                        return .init(title: immunization.vaccineCode?.text,
                                     date: immunization.occurrenceDateTime?.dateFormatter())
                    }, fetch: {
                        await  viewModel.getImmunizations(groupCode.entry)
                    }, detailView: { entry in
                        ImmunizationSheetView(immunization: entry)
                    }
                )
                .navigationTitle(category.title)
            case .labs:
                HealthDataGroupItemsView(
                    entry: viewModel.labs,
                    id: \.id,
                    rowContent: { labs in
                        return .init(title: labs.code?.text,
                                     date: labs.effectiveDateTime?.dateFormatter())
                    }, fetch: {
                        await  viewModel.getLabs(groupCode.entry)
                    }, detailView: { entry in
                        LabsSheetView(labs: entry)
                    }
                )
                .navigationTitle(category.title)
            case .medications:
                HealthDataGroupItemsView(
                    entry: viewModel.medications,
                    id: \.id,
                    rowContent: { medication in
                        return .init(title: medication.medicationCodeableConcept?.text,
                                     date: medication.effectivePeriod?.start?.dateFormatter())
                    }, fetch: {
                        await  viewModel.getMedicationStatements(groupCode.entry)
                    }, detailView: { entry in
                        MedicationsSheetView(medications: entry)
                    }
                )
                .navigationTitle(category.title)
            case .procedure:
                HealthDataGroupItemsView(
                    entry: viewModel.procedures,
                    id: \.id,
                    rowContent: { procedure in
                        return .init(title: procedure.code?.text,
                                     date: procedure.performedDateTime?.dateFormatter())
                    }, fetch: {
                        await  viewModel.getProcedures(groupCode.entry)
                    }, detailView: { entry in
                        ProceduresSheetView(procedures: entry)
                    }
                )
                .navigationTitle(category.title)
            case .vitalSigns:
                HealthDataGroupItemsView(
                    entry: viewModel.vitalSigns,
                    id: \.id,
                    rowContent: { vitalSign in
                        return .init(title: vitalSign.code?.text,
                                     date: vitalSign.effectiveDateTime?.dateFormatter())
                    }, fetch: {
                        await  viewModel.getVialSigns(groupCode.entry)
                    }, detailView: { entry in
                        VitalSignsSheetView(vitalSigns: entry)
                    }
                )
                .navigationTitle(category.title)
            }
        }
    }
}
