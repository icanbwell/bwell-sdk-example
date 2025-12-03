//
//  HealthSummaryViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//
import Foundation
import BWellSDK

@MainActor
final class HealthSummaryViewModel: ObservableObject {
    private var sdkManager: BWellSDKManager?
    @Published var errorMessage: String?
    @Published var isLoading: Bool = false

    // Published properties
    @Published var healthSummary: [BWell.HealthSummary.Resource] = []
    @Published var allergyIntolerances: [BWell.AllergyIntolerance] = []
    @Published var carePlans: [BWell.CarePlan] = []
    @Published var conditions: [BWell.Condition] = []
    @Published var immunizations: [BWell.Immunization] = []
    @Published var labs: [BWell.Observation] = []
    @Published var procedures: [BWell.Procedure] = []
    @Published var vitalSigns: [BWell.Observation] = []
    @Published var medications: [BWell.MedicationStatement] = []
    @Published var encounters: [BWell.Encounter] = []

    // Group Published properties
    @Published var allergyIntoleranceGroups: [BWell.AllergyIntoleranceGroup] = []
    @Published var conditionGroups: [BWell.ConditionGroup] = []
    @Published var carePlanGroups: [BWell.CarePlanGroup] = []
    @Published var immunizationGroups: [BWell.ImmunizationGroup] = []
    @Published var labGroups: [BWell.LabGroups] = []
    @Published var procedureGroups: [BWell.ProcedureGroups] = []
    @Published var vitalSignGroups: [BWell.VitalSignGroups] = []
    @Published var medicationGroups: [BWell.MedicationGroup] = []
    @Published var encounterGroups: [BWell.EncounterGroup] = []

    init() {
        sdkManager = .shared
    }

    // MARK: - Get Summary
    func getHealthDataSummary() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let response  = try await sdkManager.health().getHealthSummary()

            for resource in response.resources {
                healthSummary.append(resource)
            }
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            isLoading = false
        }
    }

    // MARK: - Allergy Intolerance
    func getAllergyIntoleranceGroups() async {
        guard let sdkManager = sdkManager else { return }

        let allergyIntoleranceGroups = await fetchGroupData { request in
            try await sdkManager.health().getAllergyIntoleranceGroups(request)
        }

        self.allergyIntoleranceGroups = allergyIntoleranceGroups

        print("\n Item: \(allergyIntoleranceGroups.count)\n response: \(allergyIntoleranceGroups)")
    }

    func getAllergyIntolerances(_ groupCode: BWell.Coding) async {
        guard let sdkManager = sdkManager else { return }

        let request = createHealthDataRequest(with: groupCode)

        let allergyIntolerances = await fetchData(
            fetch:  {
                try await sdkManager.health().getAllergyIntolerances(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.allergyIntolerances = allergyIntolerances
    }

    // MARK: - Care Plan
    func getCarePlanGroups() async {
        guard let sdkManager = sdkManager else { return }

        let carePlanGroups = await fetchGroupData { request in
            try await sdkManager.health().getCarePlanGroups(request)
        }

        self.carePlanGroups = carePlanGroups
    }

    func getCarePlans(_ groupCode: BWell.Coding) async {
        guard let sdkManager = sdkManager else { return }

        let request = createHealthDataRequest(with: groupCode)

        let carePlans = await fetchData(
            fetch:  {
                try await sdkManager.health().getCarePlans(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.carePlans = carePlans
    }

    // MARK: - Conditions
    func getConditionGroups() async {
        guard let sdkManager = sdkManager else { return }

        let conditionGroups = await fetchGroupData { request in
            try await sdkManager.health().getConditionGroups(request)
        }

        self.conditionGroups = conditionGroups
    }

    func getConditions(_ groupCode: BWell.Coding) async {
        guard let sdkManager = sdkManager else { return }

        let request = createHealthDataRequest(with: groupCode)

        let conditions = await fetchData(
            fetch:  {
                try await sdkManager.health().getConditions(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.conditions = conditions
    }

    // MARK: - Immunization
    func getImmunizationGroups() async {
        guard let sdkManager = sdkManager else { return }

        let immunizationGroups = await fetchGroupData { request in
            try await sdkManager.health().getImmunizationGroups(request)
        }

        self.immunizationGroups = immunizationGroups
    }

    func getImmunizations(_ groupCode: BWell.Coding) async {
        guard let sdkManager = sdkManager else { return }

        let request = createHealthDataRequest(with: groupCode)

        let immunizations = await fetchData(
            fetch:  {
                try await sdkManager.health().getImmunizations(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.immunizations = immunizations
    }

    // MARK: - Labs
    func getLabGroups() async {
        guard let sdkManager = sdkManager else { return }

        let labGroups = await fetchGroupData { request in
            try await sdkManager.health().getLabGroups(request)
        }

        self.labGroups = labGroups
    }

    func getLabs(_ groupCode: BWell.Coding) async {
        guard let sdkManager = sdkManager else { return }

        let request = createHealthDataRequest(with: groupCode)

        let labs = await fetchData(
            fetch:  {
                try await sdkManager.health().getLabs(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.labs = labs
    }

    // MARK: - Procedures
    func getProcedureGroups() async {
        guard let sdkManager = sdkManager else { return }

        let procedureGroups = await fetchGroupData { request in
            try await sdkManager.health().getProcedureGroups(request)
        }

        self.procedureGroups = procedureGroups
    }

    func getProcedures(_ groupCode: BWell.Coding) async {
        guard let sdkManager = sdkManager else { return }

        let request = createHealthDataRequest(with: groupCode)

        let procedures = await fetchData(
            fetch:  {
                try await sdkManager.health().getProcedures(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.procedures = procedures
    }

    // MARK: - Vital Signs
    func getVitalSignGroups() async {
        guard let sdkManager = sdkManager else { return }

        let vitalSignGroups = await fetchGroupData { request in
            try await sdkManager.health().getVitalSignGroups(request)
        }

        self.vitalSignGroups = vitalSignGroups
    }

    func getVialSigns(_ groupCode: BWell.Coding) async {
        guard let sdkManager = sdkManager else { return }

        let request = createHealthDataRequest(with: groupCode)

        let vitalSigns = await fetchData(
            fetch:  {
                try await sdkManager.health().getVitalSigns(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.vitalSigns = vitalSigns
    }

    // MARK: - Medications
    func getMedicationGroups() async {
        guard let sdkManager = sdkManager else { return }

        let medicationGroups = await fetchGroupData { request in
            try await sdkManager.health().getMedicationGroups(request)
        }

        self.medicationGroups = medicationGroups
    }

    func getMedicationStatements(_ groupCode: BWell.Coding) async {
        guard let sdkManager = sdkManager else { return }

        let request = createHealthDataRequest(with: groupCode)

        let medications = await fetchData(
            fetch:  {
                try await sdkManager.health().getMedicationStatements(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.medications = medications
    }

    // MARK: - Encounters
    func getEncounterGroups() async {
        guard let sdkManager = sdkManager else { return }

        let encounterGroups = await fetchGroupData { request in
            try await sdkManager.health().getEncounterGroups(request)
        }

        self.encounterGroups = encounterGroups
    }

    func getEncounters(_ groupCode: BWell.Coding) async {
        guard let sdkManager = sdkManager else { return }
        
        let request = createHealthDataRequest(with: groupCode)

        let encounters = await fetchData(
            fetch:  {
                try await sdkManager.health().getEncounters(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.encounters = encounters
    }
}

// MARK: - Helper Functions
extension HealthSummaryViewModel {
    private func createHealthDataRequest(with groupCode: BWell.Coding) -> BWell.HealthDataRequest {
        let searchTokenValue = BWell.SearchToken.Value(system: groupCode.system, code: groupCode.code)
        var request = BWell.HealthDataRequest(page: 0)

        request.groupCode = .init(value: searchTokenValue)

        return request
    }

    private func fetchData<DataType, Response>(fetch: () async throws -> Response,
                                               extract: (Response) -> [DataType]) async -> [DataType] {
        isLoading = true

        do {
            guard let _ = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return []
            }

            let response = try await fetch()

            isLoading = false

            return extract(response)
        } catch {
            errorMessage = "Failed to fetch data: \(error)"
            isLoading = false
            return []
        }
    }

    private func fetchGroupData<GroupType>(fetch: (BWell.HealthDataGroupRequest) async throws -> BWell.GroupResult<GroupType>) async -> [GroupType] {
        isLoading = true

        do {
            guard let _ = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return []
            }

            let request = BWell.HealthDataGroupRequest(page: 0)
            let response = try await fetch(request)

            isLoading = false
            return response.resources ?? []
        } catch {
            errorMessage = "Failed to fetch group data: \(error)"
            isLoading = false
            return []
        }
    }
}
