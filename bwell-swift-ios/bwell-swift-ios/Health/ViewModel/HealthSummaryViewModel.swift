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
    @Published var medications: [BWell.MedicationStatements] = []
    @Published var encounters: [BWell.Encounter] = []


    // Group Published properties
    @Published var allergyIntoleranceGroup: BWell.GroupResult<BWell.AllergyIntoleranceGroup>? = nil
    @Published var conditionsGroup: BWell.GroupResult<BWell.ConditionGroup>? = nil
    @Published var carePlansIntoleranceGroup: BWell.GroupResult<BWell.CarePlanGroup>? = nil

    func setup(sdkManager: BWellSDKManager) {
        self.sdkManager = sdkManager
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
    func getAllergyIntolerances() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }
            // Create the health data request
            let request = BWell.HealthDataRequest(page: 1)
            let response = try await sdkManager.health().getAllergyIntolerances(request)

            guard let entries = response.entry else {
                isLoading = false
                return
            }

            for entry in entries {
                if let allergyIntolerace = entry.resource {
                    allergyIntolerances.append(allergyIntolerace)
                }
            }
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            isLoading = false
        }
    }

    func getAllergyIntoleranceGroups() async {
        do {
            guard let sdkManager else { return }

            let request = BWell.HealthDataGroupRequest(page: 1)
            allergyIntoleranceGroup = try await sdkManager.health().getAllergyIntoleranceGroups(request)

            if let response = allergyIntoleranceGroup {
                print(response)
            }
        } catch {
            errorMessage = "Failed to fetch allergy intolerance group: \(error.localizedDescription)"
            allergyIntoleranceGroup = nil
        }
    }

    // MARK: - Care Plan
    func getCarePlans() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }
            let request = BWell.HealthDataRequest(page: 1)
            let response = try await sdkManager.health().getCarePlans(request)

            guard let entries = response.entry else {
                isLoading = false
                return
            }

            for entry in entries {
                if let carePlan = entry.resource {
                    carePlans.append(carePlan)
                }
            }
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            isLoading = false
        }
    }

    // TODO: Add Care Plan Groups here

    // MARK: - Conditions
    func getConditions() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let request = BWell.HealthDataRequest(page: 1)
            let response = try await sdkManager.health().getConditions(request)

            guard let entries = response.entry else {
                isLoading = false
                return
            }

            for entry in entries {
                if let condition = entry.resource {
                    conditions.append(condition)
                }
            }
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            isLoading = false
        }
    }

    // MARK: - Immunization
    func getImmunizations() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let request = BWell.HealthDataRequest(page: 1)
            let response = try await sdkManager.health().getImmunizations(request)

            guard let entries = response.entry else {
                isLoading = false
                return
            }

            for entry in entries {
                if let immunization = entry.resource {
                    immunizations.append(immunization)
                }
            }
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            isLoading = false
        }
    }

    // MARK: - Labs
    func getLabs() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let request = BWell.HealthDataRequest(page: 1)
            let response = try await sdkManager.health().getLabs(request)

            guard let entries = response.entry else {
                isLoading = false
                return
            }

            for entry in entries {
                if let lab = entry.resource {
                    labs.append(lab)
                }
            }
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            isLoading = false
        }
    }

    // MARK: - Procedures
    func getProcedures() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let request = BWell.HealthDataRequest(page: 1)
            let response = try await sdkManager.health().getProcedures(request)

            guard let entries = response.entry else {
                isLoading = false
                return
            }

            for entry in entries {
                if let procedure = entry.resource {
                    procedures.append(procedure)
                }
            }
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            isLoading = false
        }
    }

    // MARK: - Vital Signs
    func getVialSigns() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let request = BWell.HealthDataRequest(page: 1)
            let response = try await sdkManager.health().getVitalSigns(request)

            guard let entries = response.entry else {
                isLoading = false
                return
            }

            for entry in entries {
                if let vitalSign = entry.resource {
                    vitalSigns.append(vitalSign)
                }
            }
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            isLoading = false
        }
    }

    // MARK: - Medications
    func getMedicationStatements() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let request = BWell.HealthDataRequest(page: 1)
            let response = try await sdkManager.health().getMedicationStatement(request)

            guard let entries = response.entry else {
                isLoading = false
                return
            }

            for entry in entries {
                if let statement = entry.resource {
                    medications.append(statement)
                }
            }
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            isLoading = false
        }
    }

    // MARK: - Encounters
    func getEncounters() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            let request = BWell.HealthDataRequest(page: 1)
            let response = try await sdkManager.health().getEncounters(request)

            guard let entries = response.entry else {
                isLoading = false
                return
            }

            for entry in entries {
                if let encounter = entry.resource {
                    encounters.append(encounter)
                }
            }
            isLoading = false
        } catch {
            errorMessage = "Failed to fetch health data: \(error.localizedDescription)"
            isLoading = false
        }
    }
}
