//
//  HealthSummaryView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//
import Foundation
import SwiftUI
import BWellSDK

struct HealthSummaryView: View {
    @EnvironmentObject private var router: NavigationRouter
    @EnvironmentObject private var sdkManager: BWellSDKManager

    @EnvironmentObject private var viewModel: HealthSummaryViewModel
    @Binding var showMenu: Bool

    var body: some View {
        List {
            ForEach(HealthDataSummaryModel.allCases) { item in
                Button {
                    router.path.append(item)
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: item.icon)
                            .frame(width: 24, alignment: .center)

                        Text(item.title)

                        Spacer()

                        if viewModel.isLoading {
                            ProgressView()
                        } else {
                            if let total = viewModel.healthSummary.first(where: {
                                $0.category == item.category
                            })?.total {
                                Text("\(total)")
                                    .foregroundStyle(.secondary)
                            }
                        }

                        Image(systemName: "chevron.right")
                            .font(.caption)
                            .frame(width: 24)
                            .foregroundStyle(.gray)
                    }
                }
            }.listRowSeparator(.hidden)
        }
        .padding(.top, 10)
        .bwellNavigationBar(showMenu: $showMenu, navigationTitle: "Health Summary")
        .listStyle(.plain)
        .listRowSeparator(.hidden)
        .task {
            if viewModel.healthSummary.isEmpty {
                await viewModel.getHealthDataSummary()
            }
        }
        .navigationDestination(for: HealthDataSummaryModel.self) { category in
            HealthSummaryDetailView(category: category)
                .environmentObject(viewModel)
        }
    }
}

struct HealthSummaryDetailView: View {
    @EnvironmentObject private var vieModel: HealthSummaryViewModel
    var category: HealthDataSummaryModel

    var body: some View {
        Group {
            category.view
        }
    }
}

#Preview {
    HealthSummaryView(showMenu: .constant(false))
        .environmentObject(BWellSDKManager.shared)
        .environmentObject(NavigationRouter())
        .environmentObject(SideMenuOptionViewModel())
}
