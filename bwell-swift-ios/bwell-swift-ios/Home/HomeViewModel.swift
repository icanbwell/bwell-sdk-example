//
//  HomeViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 11/11/25.
//
import Foundation
import SwiftUI
import BWellSDK

struct HighlightVital {
    let name: String
    let displayValue: String
    let date: String
}

struct HighlightLab {
    let name: String
    let displayValue: String
    let interpretation: String?
}

struct HighlightEncounter {
    let typeName: String
    let date: String
}

struct SparklineSeries: Identifiable {
    let id = UUID()
    let name: String
    let latestValue: String
    let unit: String
    let values: [Double]
    let color: Color
}

@MainActor
final class HomeViewModel: ObservableObject {
    @Published var firstName: String = ""
    @Published var isLoading: Bool = true
    @Published var hasConnections = false
    @Published var healthMatchConsented: Bool?
    @Published var showHealthMatchSheet = false
    @Published var healthSummaryCount = 0
    @Published var summaryResources: [BWell.HealthSummary.Resource] = []

    // Highlights
    @Published var recentVitals: [HighlightVital] = []
    @Published var recentEncounter: HighlightEncounter?
    @Published var flaggedLab: HighlightLab?
    @Published var sparklineData: [SparklineSeries] = []
    @Published var recentLabs: [BWell.Observation] = []

    // Tasks
    @Published var pendingTasks: [BWell.Task] = []
    @Published var isLoadingTasks = false

    func reset() {
        recentVitals = []
        recentEncounter = nil
        flaggedLab = nil
        sparklineData = []
        recentLabs = []
        summaryResources = []
        healthSummaryCount = 0
        pendingTasks = []
        isLoading = true
    }

    func loadProfile(sdk: BWellClient) async {
        do {
            let user = try await sdk.user.getProfile()
            self.firstName = user?.name?.first?.given?.first ?? "User"
        } catch {
            self.firstName = "User"
        }
    }

    func loadHomeData(sdk: BWellClient) async {
        defer { isLoading = false }
        async let profileTask: Void = loadProfile(sdk: sdk)
        async let connectionsTask: Void = loadConnections(sdk: sdk)
        async let consentsTask: Void = loadConsents(sdk: sdk)
        async let summaryTask: Void = loadHealthSummary(sdk: sdk)
        async let vitalsTask: Void = loadRecentVitals(sdk: sdk)
        async let labsTask: Void = loadRecentLabs(sdk: sdk)
        async let encountersTask: Void = loadRecentEncounters(sdk: sdk)
        async let tasksTask: Void = loadPendingTasks(sdk: sdk)
        _ = await (profileTask, connectionsTask, consentsTask, summaryTask, vitalsTask, labsTask, encountersTask, tasksTask)
    }

    private func loadConnections(sdk: BWellClient) async {
        do {
            let connections = try await sdk.connection.getMemberConnections()
            hasConnections = !connections.isEmpty
        } catch {
            hasConnections = false
        }
    }

    private func loadConsents(sdk: BWellClient) async {
        do {
            let result = try await sdk.user.getConsents(nil)
            if let consents = result?.entry, !consents.isEmpty {
                healthMatchConsented = true
            } else {
                healthMatchConsented = false
            }
        } catch {
            healthMatchConsented = nil
        }
    }

    private func loadHealthSummary(sdk: BWellClient) async {
        do {
            let summary = try await sdk.health.getHealthSummary()
            summaryResources = summary.resources
            healthSummaryCount = summary.resources.reduce(0) { $0 + $1.total }
        } catch {
            healthSummaryCount = 0
            summaryResources = []
        }
    }

    private func loadRecentVitals(sdk: BWellClient) async {
        do {
            let request = BWell.HealthDataRequest(page: 0, pageSize: 20)
            let response = try await sdk.health.getVitalSigns(request)
            let vitals = response.entry?.compactMap { $0.resource } ?? []

            // Build highlights from the most recent vitals
            var highlights: [HighlightVital] = []
            var sparklines: [String: (values: [Double], unit: String, color: Color)] = [:]

            for vital in vitals {
                let name = vital.code?.coding?.first?.display ?? vital.code?.text ?? "Vital Sign"
                let dateStr = vital.effectiveDateTime ?? ""

                if let qty = vital.valueQuantity, let value = qty.value {
                    let unit = qty.unit ?? ""
                    let formatted = value.truncatingRemainder(dividingBy: 1) == 0
                        ? String(format: "%.0f", value)
                        : String(format: "%.1f", value)
                    let displayVal = "\(formatted) \(unit)".trimmingCharacters(in: .whitespaces)
                    highlights.append(HighlightVital(name: name, displayValue: displayVal, date: dateStr.dateFormatter()))

                    // Collect sparkline data keyed by name
                    if sparklines[name] == nil {
                        let color: Color = sparklines.count % 2 == 0 ? .pink : .bwellBlue
                        sparklines[name] = (values: [], unit: unit, color: color)
                    }
                    sparklines[name]?.values.append(value)
                }
            }

            recentVitals = Array(highlights.prefix(3))

            // Build sparkline series (reverse so oldest is first)
            sparklineData = sparklines.prefix(4).map { key, data in
                SparklineSeries(
                    name: key,
                    latestValue: data.values.first.map { String(format: "%.0f", $0) } ?? "",
                    unit: data.unit,
                    values: data.values.reversed(),
                    color: data.color
                )
            }
        } catch {
            recentVitals = []
            sparklineData = []
        }
    }

    private func loadRecentLabs(sdk: BWellClient) async {
        do {
            let request = BWell.HealthDataRequest(page: 0, pageSize: 20)
            let response = try await sdk.health.getLabs(request)
            let labs = response.entry?.compactMap { $0.resource } ?? []
            recentLabs = labs

            // Find first out-of-range lab
            for lab in labs {
                let interp = lab.interpretation?.first?.text ?? lab.interpretation?.first?.coding?.first?.display
                if let interp, interp.lowercased().contains("high") || interp.lowercased().contains("low") || interp.lowercased().contains("abnormal") {
                    let name = lab.code?.coding?.first?.display ?? lab.code?.text ?? "Lab"
                    var displayVal = ""
                    if let qty = lab.valueQuantity, let value = qty.value {
                        let unit = qty.unit ?? ""
                        displayVal = "\(value) \(unit)".trimmingCharacters(in: .whitespaces)
                    } else if let str = lab.valueString {
                        displayVal = str
                    }
                    flaggedLab = HighlightLab(name: name, displayValue: displayVal, interpretation: interp)
                    break
                }
            }
        } catch {
            recentLabs = []
            flaggedLab = nil
        }
    }

    private func loadPendingTasks(sdk: BWellClient) async {
        isLoadingTasks = true
        do {
            let request = BWell.TasksRequest(page: 0, pageSize: 10)
            let response = try await sdk.activity.getTasks(request)
            let allTasks = response.entry?.compactMap { $0.resource } ?? []
            let terminalStatuses: Set<String> = ["completed", "cancelled", "rejected", "entered-in-error", "failed"]
            pendingTasks = allTasks.filter { task in
                guard let status = task.status?.lowercased() else { return true }
                return !terminalStatuses.contains(status)
            }
        } catch {
            NSLog("[Home] getTasks error: %@", String(describing: error))
            pendingTasks = []
        }
        isLoadingTasks = false
    }

    private func loadRecentEncounters(sdk: BWellClient) async {
        do {
            let request = BWell.HealthDataRequest(page: 0, pageSize: 5)
            let response = try await sdk.health.getEncounters(request)
            let encounters = response.entry?.compactMap { $0.resource } ?? []

            if let latest = encounters.first {
                let typeName = latest.type?.first?.coding?.first?.display
                    ?? latest.type?.first?.text
                    ?? latest.class?.display
                    ?? "Visit"
                let dateStr = latest.period?.start?.dateFormatter() ?? ""
                recentEncounter = HighlightEncounter(typeName: typeName, date: dateStr)
            }
        } catch {
            recentEncounter = nil
        }
    }
}
