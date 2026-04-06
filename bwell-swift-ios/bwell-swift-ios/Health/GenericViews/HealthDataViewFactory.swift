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
    @EnvironmentObject private var sdkManager: SDKManager

    let category: HealthDataSummaryModel
    let groupCode: BWellHealthDataWrapper<BWell.Coding>

    init(_ category: HealthDataSummaryModel,
         _ groupCode: BWellHealthDataWrapper<BWell.Coding>) {
        self.category = category
        self.groupCode = groupCode
    }

    @ViewBuilder
    var body: some View {
        switch category {
        case .allergyIntolerance:
            HealthDataGroupItemsView(
                id: \BWell.AllergyIntolerance.id,
                rowContent: { (allergy: BWell.AllergyIntolerance) in
                    .init(title: allergy.code?.coding?.first?.display ?? allergy.code?.text,
                          subtitle: allergy.clinicalStatus?.text ?? allergy.clinicalStatus?.coding?.first?.display,
                          date: allergy.recordedDate,
                          value: allergy.criticality?.capitalizingFirstLetter(),
                          valueColor: allergyCriticalityColor(allergy.criticality))
                }, fetch: {
                    guard let sdk = sdkManager.sdk else { return [] }
                    return await viewModel.fetchAllergyIntolerances(groupCode.entry, sdk: sdk)
                }, detailView: { allergy in
                    AllergyInlineDetail(allergy: allergy)
                }
            )
            .navigationTitle(category.title)
        case .carePlan:
            HealthDataGroupItemsView(
                id: \BWell.CarePlan.id,
                rowContent: { (carePlan: BWell.CarePlan) in
                    .init(title: carePlan.title,
                          subtitle: carePlan.category?.first?.coding?.first?.display,
                          date: carePlan.period?.start,
                          value: carePlan.status?.capitalizingFirstLetter())
                }, fetch: {
                    guard let sdk = sdkManager.sdk else { return [] }
                    return await viewModel.fetchCarePlans(groupCode.entry, sdk: sdk)
                }, detailView: { carePlan in
                    CarePlanInlineDetail(carePlan: carePlan)
                }
            )
            .navigationTitle(category.title)
        case .condition:
            HealthDataGroupItemsView(
                id: \BWell.Condition.id,
                rowContent: { (condition: BWell.Condition) in
                    .init(title: condition.code?.coding?.first?.display ?? condition.code?.text,
                          subtitle: condition.clinicalStatus?.text ?? condition.clinicalStatus?.coding?.first?.display,
                          date: condition.recordedDate)
                }, fetch: {
                    guard let sdk = sdkManager.sdk else { return [] }
                    return await viewModel.fetchConditions(groupCode.entry, sdk: sdk)
                }, detailView: { condition in
                    ConditionInlineDetail(condition: condition)
                }
            )
            .navigationTitle(category.title)
        case .encounter:
            HealthDataGroupItemsView(
                id: \BWell.Encounter.id,
                rowContent: { (encounter: BWell.Encounter) in
                    let providerName = encounterPractitionerName(encounter)
                    let facility = encounter.serviceProvider?.resource?.name
                        ?? encounter.location?.first?.location?.resource?.name
                    let subtitle: String? = {
                        let parts = [providerName, facility].compactMap { $0 }
                        if parts.count == 2 { return "\(parts[0]) at \(parts[1])" }
                        return parts.first
                    }()
                    let reason = encounter.reasonCode?.first?.coding?.first?.display
                        ?? encounter.reasonCode?.first?.text
                    let title = reason
                        ?? encounter.type?.first?.coding?.first?.display
                        ?? encounter.type?.first?.text
                        ?? encounter.class?.display
                        ?? "Visit"
                    return .init(title: title,
                          subtitle: subtitle,
                          date: encounter.period?.start,
                          value: encounter.status?.capitalizingFirstLetter())
                }, fetch: {
                    guard let sdk = sdkManager.sdk else { return [] }
                    let encounters = await viewModel.fetchEncounters(groupCode.entry, sdk: sdk)
                    return encounters.sorted { ($0.period?.start ?? "") > ($1.period?.start ?? "") }
                }, detailView: { encounter in
                    EncounterInlineDetail(encounter: encounter)
                }
            )
            .navigationTitle(category.title)
        case .immunization:
            HealthDataGroupItemsView(
                id: \BWell.Immunization.id,
                rowContent: { (immunization: BWell.Immunization) in
                    .init(title: immunization.vaccineCode?.coding?.first?.display ?? immunization.vaccineCode?.text,
                          subtitle: immunization.site?.text,
                          date: immunization.occurrenceDateTime,
                          value: immunization.status?.capitalizingFirstLetter())
                }, fetch: {
                    guard let sdk = sdkManager.sdk else { return [] }
                    return await viewModel.fetchImmunizations(groupCode.entry, sdk: sdk)
                }, detailView: { immunization in
                    ImmunizationInlineDetail(immunization: immunization)
                }
            )
            .navigationTitle(category.title)
        case .labs:
            ObservationChartItemsView(
                chartType: .line,
                chartTitle: groupCode.entry.display ?? "Lab Results",
                id: \BWell.Observation.id,
                rowContent: { (lab: BWell.Observation) in
                    .init(title: lab.code?.coding?.first?.display ?? lab.code?.text,
                          subtitle: lab.interpretation?.first?.text ?? lab.interpretation?.first?.coding?.first?.display,
                          date: lab.effectiveDateTime,
                          value: observationValueText(lab))
                }, fetch: {
                    guard let sdk = sdkManager.sdk else { return [] }
                    let labs = await viewModel.fetchLabs(groupCode.entry, sdk: sdk)
                    return labs.sorted { ($0.effectiveDateTime ?? "") > ($1.effectiveDateTime ?? "") }
                }, detailView: { lab in
                    LabInlineDetail(lab: lab)
                }
            )
            .navigationTitle(category.title)
        case .medications:
            HealthDataGroupItemsView(
                id: \BWell.MedicationStatement.id,
                rowContent: { (medication: BWell.MedicationStatement) in
                    .init(title: medication.medicationCodeableConcept?.coding?.first?.display ?? medication.medicationCodeableConcept?.text,
                          subtitle: medication.dosage?.first?.text,
                          date: medication.effectivePeriod?.start,
                          value: medication.status?.capitalizingFirstLetter())
                }, fetch: {
                    guard let sdk = sdkManager.sdk else { return [] }
                    return await viewModel.fetchMedicationStatements(groupCode.entry, sdk: sdk)
                }, detailView: { medication in
                    MedicationInlineDetail(medication: medication)
                }
            )
            .navigationTitle(category.title)
        case .procedure:
            HealthDataGroupItemsView(
                id: \BWell.Procedure.id,
                rowContent: { (procedure: BWell.Procedure) in
                    .init(title: procedure.code?.coding?.first?.display ?? procedure.code?.text,
                          subtitle: procedure.bodySite?.first?.text ?? procedure.bodySite?.first?.coding?.first?.display,
                          date: procedure.performedDateTime,
                          value: procedure.status?.capitalizingFirstLetter())
                }, fetch: {
                    guard let sdk = sdkManager.sdk else { return [] }
                    return await viewModel.fetchProcedures(groupCode.entry, sdk: sdk)
                }, detailView: { procedure in
                    ProcedureInlineDetail(procedure: procedure)
                }
            )
            .navigationTitle(category.title)
        case .vitalSigns:
            ObservationChartItemsView(
                chartType: .line,
                chartTitle: groupCode.entry.display ?? "Vital Signs",
                id: \BWell.Observation.id,
                rowContent: { (vitalSign: BWell.Observation) in
                    .init(title: vitalSign.code?.coding?.first?.display ?? vitalSign.code?.text,
                          subtitle: vitalSign.interpretation?.first?.text,
                          date: vitalSign.effectiveDateTime,
                          value: observationValueText(vitalSign))
                }, fetch: {
                    guard let sdk = sdkManager.sdk else { return [] }
                    let vitals = await viewModel.fetchVitalSigns(groupCode.entry, sdk: sdk)
                    return vitals.sorted { ($0.effectiveDateTime ?? "") > ($1.effectiveDateTime ?? "") }
                }, detailView: { vitalSign in
                    VitalSignInlineDetail(vitalSign: vitalSign)
                }
            )
            .navigationTitle(category.title)
        }
    }

    private func observationValueText(_ obs: BWell.Observation) -> String? {
        if let qty = obs.valueQuantity, let value = qty.value {
            let unit = qty.unit ?? ""
            return "\(value) \(unit)".trimmingCharacters(in: .whitespaces)
        } else if let str = obs.valueString {
            return str
        }
        return nil
    }

    private func encounterPractitionerName(_ encounter: BWell.Encounter) -> String? {
        for p in encounter.participant ?? [] {
            if case .practitioner(let doc) = p.individual?.resource {
                if let name = doc.name?.first {
                    let given = name.given?.joined(separator: " ") ?? ""
                    let family = name.family ?? ""
                    let prefix = name.prefix?.first ?? ""
                    let parts = [prefix, given, family].filter { !$0.isEmpty }
                    if !parts.isEmpty { return parts.joined(separator: " ") }
                }
            }
            // Fallback to display on the reference
            if let display = p.individual?.display, !display.isEmpty {
                return display
            }
        }
        return nil
    }

    private func allergyCriticalityColor(_ criticality: String?) -> Color {
        switch criticality?.lowercased() {
        case "high": return .red
        case "low": return .green
        default: return .secondary
        }
    }
}

// MARK: - Inline Detail Views (compact field/value pairs for collapsible cards)

private struct DetailRow: View {
    let label: String
    let value: String?

    var body: some View {
        if let value, !value.isEmpty {
            HStack(alignment: .top, spacing: 4) {
                Text(label)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .frame(width: 100, alignment: .trailing)
                Text(value)
                    .font(.caption)
                    .foregroundStyle(.primary)
                Spacer()
            }
        }
    }
}

struct AllergyInlineDetail: View {
    let allergy: BWell.AllergyIntolerance

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            DetailRow(label: "Category:", value: allergy.category?.first?.capitalizingFirstLetter())
            DetailRow(label: "Clinical Status:", value: allergy.clinicalStatus?.text ?? allergy.clinicalStatus?.coding?.first?.display)
            DetailRow(label: "Verification:", value: allergy.verificationStatus?.text ?? allergy.verificationStatus?.coding?.first?.display)
            DetailRow(label: "Criticality:", value: allergy.criticality)
            DetailRow(label: "Type:", value: allergy.type)
            DetailRow(label: "Onset:", value: allergy.onsetDateTime?.dateFormatter())
            DetailRow(label: "Recorded:", value: allergy.recordedDate?.dateFormatter())

            if let reactions = allergy.reaction, !reactions.isEmpty {
                Text("Reactions")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .padding(.top, 4)
                ForEach(reactions, id: \.id) { reaction in
                    if let manifestations = reaction.manifestation {
                        ForEach(manifestations, id: \.id) { m in
                            HStack(spacing: 4) {
                                Text("•")
                                Text(m.coding?.first?.display ?? m.text ?? "")
                            }
                            .font(.caption)
                            .foregroundStyle(.primary)
                        }
                    }
                    if let severity = reaction.severity {
                        DetailRow(label: "Severity:", value: severity)
                    }
                }
            }
        }
    }
}

struct CarePlanInlineDetail: View {
    let carePlan: BWell.CarePlan

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            if let status = carePlan.status {
                HStack {
                    Text(status.capitalizingFirstLetter())
                        .font(.caption)
                        .fontWeight(.medium)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(statusColor(status).opacity(0.15))
                        .foregroundStyle(statusColor(status))
                        .clipShape(Capsule())
                }
            }
            DetailRow(label: "Start:", value: carePlan.period?.start?.dateFormatter())
            DetailRow(label: "End:", value: carePlan.period?.end?.dateFormatter())
            DetailRow(label: "Created:", value: carePlan.created?.dateFormatter())
            DetailRow(label: "Category:", value: carePlan.category?.first?.coding?.first?.display)

            if let description = carePlan.description, !description.isEmpty {
                Text(description)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .padding(.top, 4)
            }
        }
    }

    private func statusColor(_ status: String) -> Color {
        switch status.lowercased() {
        case "active": return .green
        case "completed": return .blue
        case "revoked", "cancelled": return .red
        default: return .gray
        }
    }
}

struct ConditionInlineDetail: View {
    let condition: BWell.Condition

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            DetailRow(label: "Clinical Status:", value: condition.clinicalStatus?.text ?? condition.clinicalStatus?.coding?.first?.display)
            DetailRow(label: "Category:", value: condition.category?.first?.coding?.first?.display)
            DetailRow(label: "Verification:", value: condition.verificationStatus?.text ?? condition.verificationStatus?.coding?.first?.display)
            DetailRow(label: "Severity:", value: condition.severity?.text ?? condition.severity?.coding?.first?.display)
            DetailRow(label: "Onset:", value: condition.onsetDateTime?.dateFormatter())
            DetailRow(label: "Recorded:", value: condition.recordedDate?.dateFormatter())

            if let bodySites = condition.bodySite, !bodySites.isEmpty {
                DetailRow(label: "Body Site:", value: bodySites.compactMap { $0.coding?.first?.display }.joined(separator: ", "))
            }
        }
    }
}

struct EncounterInlineDetail: View {
    let encounter: BWell.Encounter

    private var practitionerName: String? {
        for p in encounter.participant ?? [] {
            if case .practitioner(let doc) = p.individual?.resource {
                if let name = doc.name?.first {
                    let given = name.given?.joined(separator: " ") ?? ""
                    let family = name.family ?? ""
                    let prefix = name.prefix?.first ?? ""
                    let parts = [prefix, given, family].filter { !$0.isEmpty }
                    if !parts.isEmpty { return parts.joined(separator: " ") }
                }
            }
            if let display = p.individual?.display, !display.isEmpty {
                return display
            }
        }
        return nil
    }

    private var facilityName: String? {
        encounter.serviceProvider?.resource?.name
            ?? encounter.location?.first?.location?.resource?.name
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            if let provider = practitionerName {
                DetailRow(label: "Provider:", value: provider)
            }

            if let facility = facilityName {
                DetailRow(label: "Facility:", value: facility)
            }

            if let reasons = encounter.reasonCode, !reasons.isEmpty {
                let reasonTexts = reasons.compactMap { $0.coding?.first?.display ?? $0.text }
                if !reasonTexts.isEmpty {
                    DetailRow(label: "Reason:", value: reasonTexts.joined(separator: "; "))
                }
            }

            DetailRow(label: "Type:", value: encounter.type?.first?.coding?.first?.display ?? encounter.type?.first?.text)

            if let classDisplay = encounter.class?.display, classDisplay != encounter.type?.first?.coding?.first?.display {
                DetailRow(label: "Class:", value: classDisplay)
            }

            DetailRow(label: "Date:", value: encounter.period?.start?.dateFormatter())
            if let end = encounter.period?.end, end != encounter.period?.start {
                DetailRow(label: "Through:", value: end.dateFormatter())
            }

            if encounter.status?.lowercased() != "finished" {
                DetailRow(label: "Status:", value: encounter.status?.capitalizingFirstLetter())
            }

            DetailRow(label: "Discharge:", value: encounter.hospitalization?.dischargeDisposition?.text)
        }
    }
}

struct ImmunizationInlineDetail: View {
    let immunization: BWell.Immunization

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            DetailRow(label: "Occurrence:", value: immunization.occurrenceDateTime?.dateFormatter())
            DetailRow(label: "Expiration:", value: immunization.expirationDate?.dateFormatter())
            DetailRow(label: "Status:", value: immunization.status?.capitalizingFirstLetter())
            DetailRow(label: "Site:", value: immunization.site?.text ?? immunization.site?.coding?.first?.display)
            DetailRow(label: "Route:", value: immunization.route?.text)
            DetailRow(label: "Lot Number:", value: immunization.lotNumber)
        }
    }
}

struct LabInlineDetail: View {
    let lab: BWell.Observation

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            // Value
            if let qty = lab.valueQuantity, let value = qty.value {
                DetailRow(label: "Value:", value: "\(value) \(qty.unit ?? "")".trimmingCharacters(in: .whitespaces))
            } else if let str = lab.valueString {
                DetailRow(label: "Value:", value: str)
            }

            // Reference Range
            if let refRange = lab.referenceRange?.first {
                if let text = refRange.text {
                    DetailRow(label: "Ref Range:", value: text)
                } else if let low = refRange.low?.value, let high = refRange.high?.value {
                    let unit = refRange.low?.unit ?? refRange.high?.unit ?? ""
                    DetailRow(label: "Ref Range:", value: "\(low) - \(high) \(unit)".trimmingCharacters(in: .whitespaces))
                }
            }

            if let interpretation = lab.interpretation?.first {
                DetailRow(label: "Interpretation:", value: interpretation.text ?? interpretation.coding?.first?.display)
            }

            DetailRow(label: "Status:", value: lab.status?.capitalizingFirstLetter())
            DetailRow(label: "Effective:", value: lab.effectiveDateTime?.dateFormatter())

            if let notes = lab.note, !notes.isEmpty {
                ForEach(notes, id: \.id) { note in
                    DetailRow(label: "Note:", value: note.text)
                }
            }
        }
    }
}

struct MedicationInlineDetail: View {
    let medication: BWell.MedicationStatement

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            DetailRow(label: "Status:", value: medication.status?.capitalizingFirstLetter())
            DetailRow(label: "Start:", value: medication.effectivePeriod?.start?.dateFormatter())
            DetailRow(label: "End:", value: medication.effectivePeriod?.end?.dateFormatter())

            if let reasons = medication.reasonCode, !reasons.isEmpty {
                ForEach(reasons, id: \.id) { reason in
                    DetailRow(label: "Reason:", value: reason.text ?? reason.coding?.first?.display)
                }
            }

            if let dosages = medication.dosage, !dosages.isEmpty {
                ForEach(dosages, id: \.id) { dose in
                    DetailRow(label: "Dosage:", value: dose.text)
                }
            }
        }
    }
}

struct ProcedureInlineDetail: View {
    let procedure: BWell.Procedure

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            DetailRow(label: "Performed:", value: procedure.performedDateTime?.dateFormatter())
            if let period = procedure.performedPeriod {
                DetailRow(label: "Period Start:", value: period.start?.dateFormatter())
                DetailRow(label: "Period End:", value: period.end?.dateFormatter())
            }
            DetailRow(label: "Status:", value: procedure.status?.capitalizingFirstLetter())
            DetailRow(label: "Reason:", value: procedure.reasonCode?.first?.text ?? procedure.reasonCode?.first?.coding?.first?.display)
            DetailRow(label: "Body Site:", value: procedure.bodySite?.first?.text ?? procedure.bodySite?.first?.coding?.first?.display)
            DetailRow(label: "Outcome:", value: procedure.outcome?.text ?? procedure.outcome?.coding?.first?.display)
        }
    }
}

struct VitalSignInlineDetail: View {
    let vitalSign: BWell.Observation

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            // Value
            if let qty = vitalSign.valueQuantity, let value = qty.value {
                DetailRow(label: "Value:", value: "\(value) \(qty.unit ?? "")".trimmingCharacters(in: .whitespaces))
            } else if let str = vitalSign.valueString {
                DetailRow(label: "Value:", value: str)
            }

            // Reference Range
            if let refRange = vitalSign.referenceRange?.first {
                let low = refRange.low?.value.map { "\($0)" } ?? ""
                let high = refRange.high?.value.map { "\($0)" } ?? ""
                let unit = refRange.low?.unit ?? refRange.high?.unit ?? ""
                if !low.isEmpty || !high.isEmpty {
                    DetailRow(label: "Ref Range:", value: "\(low) - \(high) \(unit)".trimmingCharacters(in: .whitespaces))
                }
            }

            if let interpretation = vitalSign.interpretation?.first {
                DetailRow(label: "Interpretation:", value: interpretation.text ?? interpretation.coding?.first?.display)
            }

            DetailRow(label: "Status:", value: vitalSign.status?.capitalizingFirstLetter())
            DetailRow(label: "Effective:", value: vitalSign.effectiveDateTime?.dateFormatter())
        }
    }
}
