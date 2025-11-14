//
//  ManageConnectionsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//

import Foundation
import SwiftUI

struct ManageConnectionsView: View {
    @EnvironmentObject private var router: NavigationRouter
    @EnvironmentObject private var sdkManager: BWellSDKManager
    @ObservedObject private var viewModel = ManageConnectionsViewModel()
    @Binding var showMenu: Bool

    var body: some View {
        VStack(alignment: .leading) {
            if viewModel.isLoading {
                ProgressView("Loading connections...")
            } else {


                if viewModel.memberConnections.isEmpty {
                    HStack {
                        Spacer()
                        Text("No member connections found.")
                        Spacer()
                    }
                } else {
                    Text("Connections Description", tableName: "Localizable")
                    List {
                        ForEach(viewModel.memberConnections, id: \.id) { connection in
                            ListItem(connectionName: connection.name, status: connection.status)
                        }
                    }.listStyle(.plain)

                    Spacer()

                    Text("Record location status: ")
                        .fontWeight(.medium)
                }
            }
        }
        .padding()
        .bwellNavigationBar(showMenu: $showMenu, navigationTitle: "Manage Connections") {
            Button {
                router.navigate(to: .connections)
            } label: {
                Image(systemName: "plus")
            }
        }
        .task {
            if viewModel.memberConnections.isEmpty {
                await viewModel.getConnections()
            }
        }
    }
}

private struct ListItem: View {
    var connectionName: String
    var status: BWellWrapper.connectionStatus

    var body: some View {
        HStack(alignment: .center) {
            Image(systemName: "personalhotspot.circle")
                .font(.title)
                .frame(width: 25)

            VStack(alignment: .leading, spacing: 3) {
                Text(connectionName)
                    .fontWeight(.semibold)

                Text(status.description())
                    .padding(5)
                    .background(.bwellGreen)
                    .font(.system(size: 16, weight: .bold, design: .rounded))
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 5))
            }
            Spacer()

            Button {
                print("Elipsis button tapped.")
            } label: {
                Image(systemName: "elipsis")
                    .rotationEffect(Angle(degrees: 90))
                    .font(.title3)
                    .frame(width: 25)
            }
        }.listRowSeparator(.hidden, edges: .all)
    }
}

#Preview {
    ManageConnectionsView(showMenu: .constant(false))
        .environmentObject(BWellSDKManager.shared)
        .environmentObject(NavigationRouter())
        .environmentObject(SideMenuOptionViewModel())
}
