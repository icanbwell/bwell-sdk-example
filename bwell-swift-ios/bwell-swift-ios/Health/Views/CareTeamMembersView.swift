//
//  CareTeamMembersView.swift
//  bwell-swift-ios
//
//  Displays care team members with add/remove/update mutation support.
//

import SwiftUI
import BWellSDK

struct CareTeamMembersView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @StateObject private var viewModel = CareTeamMembersViewModel()

    var body: some View {
        VStack(spacing: 0) {
            if viewModel.isLoading && viewModel.members.isEmpty {
                ProgressView("Loading care team members...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.errorMessage {
                VStack(spacing: 8) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.largeTitle)
                        .foregroundStyle(.secondary)
                    Text(error)
                        .foregroundStyle(.red)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                addMemberSection
                if !viewModel.members.isEmpty {
                    membersList
                } else {
                    VStack(spacing: 8) {
                        Image(systemName: "person.3")
                            .font(.largeTitle)
                            .foregroundStyle(.secondary)
                        Text("No care team members found.")
                            .foregroundStyle(.secondary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
        }
        .navigationTitle("Care Team Members")
        .navigationBarTitleDisplayMode(.large)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button { Task { await viewModel.getMemberPlanIdentifiers() } } label: {
                    Image(systemName: "creditcard")
                }
            }
        }
        .task {
            guard let sdk = sdkManager.sdk else { return }
            viewModel.configure(sdk: sdk)
            await viewModel.loadMembers()
        }
        .alert("Plan Identifiers", isPresented: $viewModel.showPlanIds) {
            Button("OK") {}
        } message: {
            Text(viewModel.planIdsMessage)
        }
        .alert("Error", isPresented: .constant(viewModel.mutationError != nil)) {
            Button("OK") { viewModel.mutationError = nil }
        } message: {
            if let error = viewModel.mutationError {
                Text(error)
            }
        }
    }

    private var addMemberSection: some View {
        VStack(spacing: 8) {
            HStack(spacing: 8) {
                TextField("Reference (e.g. Practitioner/123)", text: $viewModel.newMemberReference)
                    .textFieldStyle(.roundedBorder)
                    .font(.caption)
                TextField("Display name", text: $viewModel.newMemberDisplay)
                    .textFieldStyle(.roundedBorder)
                    .font(.caption)
            }
            HStack {
                TextField("Type (e.g. Practitioner)", text: $viewModel.newMemberType)
                    .textFieldStyle(.roundedBorder)
                    .font(.caption)
                Button("Add Member") {
                    Task { await viewModel.addMember() }
                }
                .buttonStyle(.borderedProminent)
                .tint(.bwellPurple)
                .font(.caption)
                .disabled(viewModel.newMemberReference.isEmpty)
            }
        }
        .padding()
        .background(Color(.systemGray6))
    }

    private var membersList: some View {
        List {
            Section {
                ForEach(viewModel.members) { member in
                    CareTeamMemberRow(member: member)
                        .swipeActions(edge: .trailing) {
                            Button(role: .destructive) {
                                Task { await viewModel.removeMember(member) }
                            } label: {
                                Label("Remove", systemImage: "trash")
                            }
                        }
                }
            } header: {
                Text("\(viewModel.members.count) Member\(viewModel.members.count == 1 ? "" : "s")")
                    .textCase(nil)
            }
        }
        .listStyle(.plain)
        .refreshable {
            await viewModel.loadMembers()
        }
    }
}

// MARK: - Member Row

private struct CareTeamMemberRow: View {
    let member: BWell.CareTeamMember

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(member.id ?? "Unknown")
                .font(.body)
                .fontWeight(.medium)
                .foregroundStyle(.primary)

            if let roles = member.role, !roles.isEmpty {
                let roleText = roles.compactMap { $0.text ?? $0.coding?.first?.display }.joined(separator: ", ")
                if !roleText.isEmpty {
                    Text(roleText)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }

            if let period = member.period {
                HStack(spacing: 4) {
                    if let start = period.start {
                        Text(start)
                    }
                    if period.start != nil && period.end != nil {
                        Text("-")
                    }
                    if let end = period.end {
                        Text(end)
                    }
                }
                .font(.caption2)
                .foregroundStyle(.tertiary)
            }
        }
        .padding(.vertical, 4)
    }
}
