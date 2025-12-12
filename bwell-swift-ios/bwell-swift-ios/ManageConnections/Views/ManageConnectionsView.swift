//
//  ManageConnectionsView.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 31/10/25.
//

import Foundation
import SwiftUI
import BWellSDK

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
                if let errorMessage = viewModel.errorMessage {
                    HStack {
                        Spacer()
                        VStack(spacing: 8) {
                            Image(systemName: "exclamationmark.triangle")
                                .font(.largeTitle)
                                .foregroundColor(.red)
                            Text(errorMessage)
                                .foregroundColor(.red)
                        }
                        Spacer()
                    }
                } else if viewModel.memberConnections.isEmpty {
                    HStack {
                        Spacer()
                        Text("No member connections found.")
                        Spacer()
                    }
                } else {
                    Text("Connections Description", tableName: "Localizable")
                    List {
                        ForEach(viewModel.memberConnections, id: \.id) { connection in
                            ListItem(
                                connection: connection,
                                onDelete: {
                                    Task {
                                        await viewModel.deleteConnection(connectionId: connection.id)
                                    }
                                },
                                onDisconnect: {
                                    Task {
                                        await viewModel.disconnectConnection(connectionId: connection.id)
                                    }
                                }
                            )
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
    let connection: BWell.MemberConnectionResult
    let onDelete: () -> Void
    let onDisconnect: () -> Void

    @State private var showActionSheet = false
    @State private var showDeleteConfirmation = false
    @State private var showDisconnectConfirmation = false

    var body: some View {
        HStack(alignment: .center) {
            Image(systemName: "personalhotspot.circle")
                .font(.title)
                .frame(width: 25)

            VStack(alignment: .leading, spacing: 3) {
                Text(connection.name)
                    .fontWeight(.semibold)

                Text(connection.status.description())
                    .padding(5)
                    .background(.bwellGreen)
                    .font(.system(size: 16, weight: .bold, design: .rounded))
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 5))

                // Display timestamp information when available
                if let created = connection.created {
                    Text("Created: \(formatTimestamp(created))")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                if let lastSynced = connection.lastSynced {
                    Text("Last synced: \(formatTimestamp(lastSynced))")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
            Spacer()

            Button {
                showActionSheet = true
            } label: {
                Image(systemName: "ellipsis")
                    .rotationEffect(Angle(degrees: 90))
                    .font(.title3)
                    .frame(width: 25)
            }
        }
        .listRowSeparator(.hidden, edges: .all)
        .confirmationDialog("Connection Actions", isPresented: $showActionSheet, titleVisibility: .visible) {
            Button("Disconnect", role: .destructive) {
                showDisconnectConfirmation = true
            }
            Button("Delete", role: .destructive) {
                showDeleteConfirmation = true
            }
            Button("Cancel", role: .cancel) { }
        }
        .alert("Disconnect Connection", isPresented: $showDisconnectConfirmation) {
            Button("Cancel", role: .cancel) { }
            Button("Disconnect", role: .destructive) {
                onDisconnect()
            }
        } message: {
            Text("Are you sure you want to disconnect '\(connection.name)'? You can reconnect later.")
        }
        .alert("Delete Connection", isPresented: $showDeleteConfirmation) {
            Button("Cancel", role: .cancel) { }
            Button("Delete", role: .destructive) {
                onDelete()
            }
        } message: {
            Text("Are you sure you want to permanently delete '\(connection.name)'? This action cannot be undone.")
        }
    }

    /// Formats an ISO 8601 timestamp string into a user-friendly format
    private func formatTimestamp(_ isoString: String) -> String {
        let isoFormatter = ISO8601DateFormatter()
        isoFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]

        // Try parsing with fractional seconds first, then without
        if let date = isoFormatter.date(from: isoString) {
            return formatDate(date)
        }

        isoFormatter.formatOptions = [.withInternetDateTime]
        if let date = isoFormatter.date(from: isoString) {
            return formatDate(date)
        }

        // Return original string if parsing fails
        return isoString
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

#Preview {
    ManageConnectionsView(showMenu: .constant(false))
        .environmentObject(BWellSDKManager.shared)
        .environmentObject(NavigationRouter())
        .environmentObject(SideMenuOptionViewModel())
}
