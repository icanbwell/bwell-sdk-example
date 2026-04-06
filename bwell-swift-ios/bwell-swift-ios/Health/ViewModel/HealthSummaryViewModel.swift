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

    // MARK: - Get Summary
    func getHealthDataSummary(sdk: BWellSDK) async {
        isLoading = true
        errorMessage = nil
        do {
            let response = try await sdk.health.getHealthSummary()

            for resource in response.resources {
                healthSummary.append(resource)
            }
            NSLog("[HealthSummary] Loaded %d summary resources", healthSummary.count)
            isLoading = false
        } catch {
            NSLog("[HealthSummary] Error fetching summary: %@", error.localizedDescription)
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            isLoading = false
        }
    }

    // MARK: - Allergy Intolerance
    func getAllergyIntoleranceGroups(sdk: BWellSDK) async {
        let groups = await fetchGroupData(sdk: sdk, category: "AllergyIntolerance") { request in
            try await sdk.health.getAllergyIntoleranceGroups(request)
        }
        self.allergyIntoleranceGroups = groups
    }

    func getAllergyIntolerances(_ groupCode: BWell.Coding, sdk: BWellSDK) async {
        let request = createHealthDataRequest(with: groupCode)
        let items = await fetchData(sdk: sdk, category: "AllergyIntolerance",
            fetch: {
                try await sdk.health.getAllergyIntolerances(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.allergyIntolerances = items
    }

    // MARK: - Care Plan
    func getCarePlanGroups(sdk: BWellSDK) async {
        let groups = await fetchGroupData(sdk: sdk, category: "CarePlan") { request in
            try await sdk.health.getCarePlanGroups(request)
        }
        self.carePlanGroups = groups
    }

    func getCarePlans(_ groupCode: BWell.Coding, sdk: BWellSDK) async {
        let request = createHealthDataRequest(with: groupCode)
        let items = await fetchData(sdk: sdk, category: "CarePlan",
            fetch: {
                try await sdk.health.getCarePlans(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.carePlans = items
    }

    // MARK: - Conditions
    func getConditionGroups(sdk: BWellSDK) async {
        let groups = await fetchGroupData(sdk: sdk, category: "Condition") { request in
            try await sdk.health.getConditionGroups(request)
        }
        self.conditionGroups = groups
    }

    func getConditions(_ groupCode: BWell.Coding, sdk: BWellSDK) async {
        let request = createHealthDataRequest(with: groupCode)
        let items = await fetchData(sdk: sdk, category: "Condition",
            fetch: {
                try await sdk.health.getConditions(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.conditions = items
    }

    // MARK: - Immunization
    func getImmunizationGroups(sdk: BWellSDK) async {
        let groups = await fetchGroupData(sdk: sdk, category: "Immunization") { request in
            try await sdk.health.getImmunizationGroups(request)
        }
        self.immunizationGroups = groups
    }

    func getImmunizations(_ groupCode: BWell.Coding, sdk: BWellSDK) async {
        let request = createHealthDataRequest(with: groupCode)
        let items = await fetchData(sdk: sdk, category: "Immunization",
            fetch: {
                try await sdk.health.getImmunizations(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.immunizations = items
    }

    // MARK: - Labs
    func getLabGroups(sdk: BWellSDK) async {
        let groups = await fetchGroupData(sdk: sdk, category: "Labs") { request in
            try await sdk.health.getLabGroups(request)
        }
        self.labGroups = groups
    }

    func getLabs(_ groupCode: BWell.Coding, sdk: BWellSDK) async {
        let request = createHealthDataRequest(with: groupCode)
        let items = await fetchData(sdk: sdk, category: "Labs",
            fetch: {
                try await sdk.health.getLabs(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.labs = items
    }

    // MARK: - Procedures
    func getProcedureGroups(sdk: BWellSDK) async {
        let groups = await fetchGroupData(sdk: sdk, category: "Procedure") { request in
            try await sdk.health.getProcedureGroups(request)
        }
        self.procedureGroups = groups
    }

    func getProcedures(_ groupCode: BWell.Coding, sdk: BWellSDK) async {
        let request = createHealthDataRequest(with: groupCode)
        let items = await fetchData(sdk: sdk, category: "Procedure",
            fetch: {
                try await sdk.health.getProcedures(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.procedures = items
    }

    // MARK: - Vital Signs
    func getVitalSignGroups(sdk: BWellSDK) async {
        let groups = await fetchGroupData(sdk: sdk, category: "VitalSigns") { request in
            try await sdk.health.getVitalSignGroups(request)
        }
        self.vitalSignGroups = groups
    }

    func getVitalSigns(_ groupCode: BWell.Coding, sdk: BWellSDK) async {
        let request = createHealthDataRequest(with: groupCode)
        let items = await fetchData(sdk: sdk, category: "VitalSigns",
            fetch: {
                try await sdk.health.getVitalSigns(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.vitalSigns = items
    }

    // MARK: - Medications
    func getMedicationGroups(sdk: BWellSDK) async {
        let groups = await fetchGroupData(sdk: sdk, category: "Medication") { request in
            try await sdk.health.getMedicationGroups(request)
        }
        self.medicationGroups = groups
    }

    func getMedicationStatements(_ groupCode: BWell.Coding, sdk: BWellSDK) async {
        let request = createHealthDataRequest(with: groupCode)
        let items = await fetchData(sdk: sdk, category: "Medication",
            fetch: {
                try await sdk.health.getMedicationStatements(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.medications = items
    }

    // MARK: - Encounters
    func getEncounterGroups(sdk: BWellSDK) async {
        let groups = await fetchGroupData(sdk: sdk, category: "Encounter") { request in
            try await sdk.health.getEncounterGroups(request)
        }
        self.encounterGroups = groups
    }

    func getEncounters(_ groupCode: BWell.Coding, sdk: BWellSDK) async {
        let request = createHealthDataRequest(with: groupCode)
        let items = await fetchData(sdk: sdk, category: "Encounter",
            fetch: {
                try await sdk.health.getEncounters(request)
            }, extract: { response in
                return response.entry?.compactMap { $0.resource } ?? []
            }
        )
        self.encounters = items
    }
}

// MARK: - Fetch and Return (for self-contained items views)
extension HealthSummaryViewModel {
    func fetchAllergyIntolerances(_ groupCode: BWell.Coding, sdk: BWellSDK) async -> [BWell.AllergyIntolerance] {
        let request = createHealthDataRequest(with: groupCode)
        return await fetchDataReturn(sdk: sdk, category: "AllergyIntolerance",
            fetch: { try await sdk.health.getAllergyIntolerances(request) },
            extract: { $0.entry?.compactMap { $0.resource } ?? [] })
    }

    func fetchCarePlans(_ groupCode: BWell.Coding, sdk: BWellSDK) async -> [BWell.CarePlan] {
        let request = createHealthDataRequest(with: groupCode)
        return await fetchDataReturn(sdk: sdk, category: "CarePlan",
            fetch: { try await sdk.health.getCarePlans(request) },
            extract: { $0.entry?.compactMap { $0.resource } ?? [] })
    }

    func fetchAllCarePlans(sdk: BWellSDK) async -> [BWell.CarePlan] {
        let request = BWell.HealthDataRequest(page: 0)
        return await fetchDataReturn(sdk: sdk, category: "CarePlan(all)",
            fetch: { try await sdk.health.getCarePlans(request) },
            extract: { $0.entry?.compactMap { $0.resource } ?? [] })
    }

    func fetchConditions(_ groupCode: BWell.Coding, sdk: BWellSDK) async -> [BWell.Condition] {
        let request = createHealthDataRequest(with: groupCode)
        return await fetchDataReturn(sdk: sdk, category: "Condition",
            fetch: { try await sdk.health.getConditions(request) },
            extract: { $0.entry?.compactMap { $0.resource } ?? [] })
    }

    func fetchEncounters(_ groupCode: BWell.Coding, sdk: BWellSDK) async -> [BWell.Encounter] {
        let request = createHealthDataRequest(with: groupCode)
        return await fetchDataReturn(sdk: sdk, category: "Encounter",
            fetch: { try await sdk.health.getEncounters(request) },
            extract: { $0.entry?.compactMap { $0.resource } ?? [] })
    }

    func fetchImmunizations(_ groupCode: BWell.Coding, sdk: BWellSDK) async -> [BWell.Immunization] {
        let request = createHealthDataRequest(with: groupCode)
        return await fetchDataReturn(sdk: sdk, category: "Immunization",
            fetch: { try await sdk.health.getImmunizations(request) },
            extract: { $0.entry?.compactMap { $0.resource } ?? [] })
    }

    func fetchLabs(_ groupCode: BWell.Coding, sdk: BWellSDK) async -> [BWell.Observation] {
        let request = createHealthDataRequest(with: groupCode)
        return await fetchDataReturn(sdk: sdk, category: "Labs",
            fetch: { try await sdk.health.getLabs(request) },
            extract: { $0.entry?.compactMap { $0.resource } ?? [] })
    }

    func fetchMedicationStatements(_ groupCode: BWell.Coding, sdk: BWellSDK) async -> [BWell.MedicationStatement] {
        let request = createHealthDataRequest(with: groupCode)
        return await fetchDataReturn(sdk: sdk, category: "Medication",
            fetch: { try await sdk.health.getMedicationStatements(request) },
            extract: { $0.entry?.compactMap { $0.resource } ?? [] })
    }

    func fetchProcedures(_ groupCode: BWell.Coding, sdk: BWellSDK) async -> [BWell.Procedure] {
        let request = createHealthDataRequest(with: groupCode)
        return await fetchDataReturn(sdk: sdk, category: "Procedure",
            fetch: { try await sdk.health.getProcedures(request) },
            extract: { $0.entry?.compactMap { $0.resource } ?? [] })
    }

    func fetchVitalSigns(_ groupCode: BWell.Coding, sdk: BWellSDK) async -> [BWell.Observation] {
        let request = createHealthDataRequest(with: groupCode)
        return await fetchDataReturn(sdk: sdk, category: "VitalSigns",
            fetch: { try await sdk.health.getVitalSigns(request) },
            extract: { $0.entry?.compactMap { $0.resource } ?? [] })
    }
}

// MARK: - Helper Functions
extension HealthSummaryViewModel {
    private func createHealthDataRequest(with groupCode: BWell.Coding) -> BWell.HealthDataRequest {
        NSLog("[HealthData] Creating request with groupCode system=%@, code=%@",
              groupCode.system ?? "nil", groupCode.code ?? "nil")
        let searchTokenValue = BWell.SearchToken.Value(system: groupCode.system, code: groupCode.code)
        var request = BWell.HealthDataRequest(page: 0)
        request.groupCode = .init(value: searchTokenValue)
        return request
    }

    private func fetchData<DataType, Response>(sdk: BWellSDK,
                                               category: String,
                                               fetch: () async throws -> Response,
                                               extract: (Response) -> [DataType]) async -> [DataType] {
        do {
            let response = try await fetch()
            let items = extract(response)
            NSLog("[HealthData] %@ items fetched: %d", category, items.count)
            return items
        } catch {
            NSLog("[HealthData] %@ items fetch error: %@", category, error.localizedDescription)
            errorMessage = "Failed to fetch \(category) data: \(error.localizedDescription)"
            return []
        }
    }

    private func fetchDataReturn<DataType, Response>(sdk: BWellSDK,
                                               category: String,
                                               fetch: () async throws -> Response,
                                               extract: (Response) -> [DataType]) async -> [DataType] {
        do {
            let response = try await fetch()
            let items = extract(response)
            NSLog("[HealthData] %@ items fetched (return): %d", category, items.count)
            return items
        } catch {
            NSLog("[HealthData] %@ items fetch error (return): %@", category, error.localizedDescription)
            return []
        }
    }

    private func fetchGroupData<GroupType>(sdk: BWellSDK,
                                           category: String,
                                           fetch: (BWell.HealthDataGroupRequest) async throws -> BWell.GroupResult<GroupType>) async -> [GroupType] {
        do {
            let request = BWell.HealthDataGroupRequest(page: 0)
            let response = try await fetch(request)
            let groups = response.resources ?? []
            NSLog("[HealthData] %@ groups fetched: %d", category, groups.count)
            return groups
        } catch {
            NSLog("[HealthData] %@ groups fetch error: %@", category, error.localizedDescription)
            errorMessage = "Failed to fetch \(category) groups: \(error.localizedDescription)"
            return []
        }
    }
}
