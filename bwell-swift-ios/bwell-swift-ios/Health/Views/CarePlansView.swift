//
//  CarePlansView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 30/10/25.
//

import Foundation
import SwiftUI
import BWellSDK

// NOTE: Backend inconsistency — health summary reports 2 care plans, groups API returns 0,
// but direct getCarePlans() returns 5. The fallback to direct fetch below works around this.
// This is a known backend issue, not a client bug.
struct CarePlansView: View {
    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @EnvironmentObject private var sdkManager: SDKManager
    @State private var useDirectFetch = false

    var body: some View {
        Group {
            if useDirectFetch {
                // Fallback: fetch all care plans directly (no groups available)
                HealthDataGroupItemsView(
                    id: \BWell.CarePlan.id,
                    rowContent: { (carePlan: BWell.CarePlan) in
                        .init(title: carePlan.title,
                              subtitle: carePlan.category?.first?.coding?.first?.display,
                              date: carePlan.period?.start,
                              value: carePlan.status?.capitalizingFirstLetter())
                    }, fetch: {
                        guard let sdk = sdkManager.sdk else { return [] }
                        return await viewModel.fetchAllCarePlans(sdk: sdk)
                    }, detailView: { entry in
                        CarePlanInlineDetail(carePlan: entry)
                    }
                )
            } else {
                HealthDataGroupListView(
                    groups: viewModel.carePlanGroups,
                    id: \.id,
                    fetch: {
                        guard let sdk = sdkManager.sdk else { return }
                        await viewModel.getCarePlanGroups(sdk: sdk)
                        // If groups are empty, switch to direct fetch
                        if viewModel.carePlanGroups.isEmpty {
                            useDirectFetch = true
                        }
                    }, rowContent: { group in
                        .init(title: group.name, date: group.period?.start)
                    }, destination: { group in
                        guard let id = group.id, let coding = group.coding else { return nil }
                        return .healthGroupItems(category: .carePlan, groupCode: BWellHealthDataWrapper(id, coding))
                    }
                )
            }
        }
        .navigationTitle("Care Plans")
    }
}

// MARK: - Care Plan Sheet View
struct CarePlanSheetView: View {
    var carePlan: BWellWrapper.carePlan

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text(carePlan.title ?? "Care Plan")
                    .font(.title3)
                    .fontWeight(.semibold)
                    .padding(.top, 16)

                if let status = carePlan.status {
                    CarePlanStatusBadge(status: status)
                }

                Divider()

                if let start = carePlan.period?.start {
                    DetailedItemView(title: "Start date: ", content: start.dateFormatter())
                }
                if let end = carePlan.period?.end {
                    DetailedItemView(title: "End date: ", content: end.dateFormatter())
                }
                if let created = carePlan.created {
                    DetailedItemView(title: "Created: ", content: created.dateFormatter())
                }

                DetailedItemView(title: "Category: ", content: carePlan.category?.first?.coding?.first?.display)

                if let description = carePlan.description, !description.isEmpty {
                    Divider()
                    Text("Description")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                    Text(description)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
            .padding(.horizontal)
        }
    }
}

private struct CarePlanStatusBadge: View {
    let status: String
    var body: some View {
        Text(status.capitalizingFirstLetter())
            .font(.caption)
            .fontWeight(.medium)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(statusColor.opacity(0.15))
            .foregroundStyle(statusColor)
            .clipShape(Capsule())
    }

    private var statusColor: Color {
        switch status.lowercased() {
        case "active": return .green
        case "completed": return .blue
        case "revoked", "cancelled": return .red
        default: return .gray
        }
    }
}
