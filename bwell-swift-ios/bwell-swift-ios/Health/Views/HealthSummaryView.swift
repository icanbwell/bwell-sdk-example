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
    @State private var showMenu: Bool = false
    @ObservedObject private var viewModel = HealthSummaryViewModel()
    @EnvironmentObject var sdkManager: BWellSDKManager

    var body: some View {
        List {
            ForEach(HealthDataSummaryModel.allCases) { item in
                NavigationLink {
                    DetailView(view: getView(from: item))
                        .navigationTitle(item.title)
                        .navigationBarTitleDisplayMode(.inline)
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: item.icon)
                            .frame(width: 24, alignment: .center)
                        Text(item.title)
                        Spacer()

                        if viewModel.isLoading {
                            ProgressView()
                        } else {
                            if let total = viewModel.healthSummary.first(where: { $0.category == item.category})?.total {
                                Text("\(total)")
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                }
            }.listRowSeparator(.hidden)
        }
        .padding(.top, 20)
        .bwellNavigationBar(showMenu: $showMenu, navigationTitle: "Health Summary")
        .listStyle(.plain)
        .listRowSeparator(.hidden)
        .onAppear {
            viewModel.setup(sdkManager: sdkManager)
        }
        .task {
            if viewModel.healthSummary.isEmpty {
                await viewModel.getHealthDataSummary()
            }
        }
    }

    @ViewBuilder
    func getView(from item: HealthDataSummaryModel) -> some View {
        switch item {
            case .allergyIntolerance:
                AllergyIntolerancesView(viewModel: viewModel)
            case .carePlan:
                CarePlansView(viewModel: viewModel)
            case .condition:
                ConditionsView(viewModel: viewModel)
            case .immunization:
                ImmunizationsView(viewModel: viewModel)
            case .labs:
                LabsView(viewModel: viewModel)
            case .medications:
                MedicationsView(viewModel: viewModel)
            case .procedure:
                ProceduresView(viewModel: viewModel)
            case .vitalSigns:
                VitalSignsView(viewModel: viewModel)
            case .encounter:
                EncountersView(viewModel: viewModel)
        }
    }
}

private struct DetailView<Content: View>: View {
    var view: Content

    var body: some View {
        ZStack(alignment: .topLeading) {
            view
        }
    }
}

#Preview {
    HealthSummaryView()
        .environmentObject(BWellSDKManager.shared)
        .environmentObject(NavigationRouter())
        .environmentObject(SideMenuOptionViewModel())
}
