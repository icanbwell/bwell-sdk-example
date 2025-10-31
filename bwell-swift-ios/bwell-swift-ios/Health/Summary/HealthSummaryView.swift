//
//  HealthSummaryView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 28/10/25.
//
import Foundation
import SwiftUI

struct HealthSummaryView: View {
    @State private var showMenu: Bool = false
    @ObservedObject private var viewModel = HealthSummaryViewModel()
    @EnvironmentObject var sdkManager: BWellSDKManager

    var body: some View {
        List {
            ForEach(HealthDataSummaryModel.allCases) { item in
                NavigationLink {
                    HealthDataDetailView(view: getView(from: item))
                        .navigationTitle(item.title)
                        .navigationBarTitleDisplayMode(.inline)
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: item.icon)
                            .frame(width: 24, alignment: .center)
                        Text(item.title)
                    }
                }
            }.listRowSeparator(.hidden)
        }
        .padding(.top, 20)
        .bwellNavigationBar(showMenu: $showMenu)
        .listStyle(.plain)
        .listRowSeparator(.hidden)
        .onAppear {
            viewModel.setup(sdkManager: sdkManager)
        }
    }

    @ViewBuilder
    func getView(from item: HealthDataSummaryModel) -> some View {
        switch item {
            case .allergyIntolerances:
                AllergyIntolerancesDetailView(viewModel: viewModel)
            case .carePlans:
                CarePlansView(viewModel: viewModel)
            default:
                GenericView()
                // TODO: Remove default case and implement the other cases
            // case .carePlans:
            // case .conditions:
            // case .encounters:
            // case .immunizations:
            // case .labs:
            // case .medications:
            // case .procedures:
            // case .vitalSigns:
        }
    }
}

#Preview {
    HealthSummaryView()
        .environmentObject(BWellSDKManager.shared)
        .environmentObject(NavigationRouter())
        .environmentObject(SideMenuOptionViewModel())
}
