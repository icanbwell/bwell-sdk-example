//
//  CareTeamMembersView.swift
//  bwell-swift-ios
//

import SwiftUI
import BWellSDK

struct CareTeamMembersView: View {
    @EnvironmentObject private var sdkManager: SDKManager
    @StateObject private var viewModel = CareTeamMembersViewModel()
    @State private var selectedMember: DisplayableCareTeamMember?

    var body: some View {
        VStack(spacing: 0) {
            if viewModel.isLoading && viewModel.members.isEmpty {
                ProgressView("Loading care team...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.errorMessage {
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 40))
                        .foregroundStyle(.secondary)
                    Text(error)
                        .font(.subheadline)
                        .foregroundStyle(.red)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if viewModel.members.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "person.3")
                        .font(.system(size: 40))
                        .foregroundStyle(.secondary)
                    Text("No care team members")
                        .font(.headline)
                        .foregroundStyle(.primary)
                    Text("Add providers to your care team from their profile page.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 40)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                membersList
            }
        }
        .navigationTitle("My Care Team")
        .navigationBarTitleDisplayMode(.large)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbarBackgroundVisibility(.visible, for: .navigationBar)
        .toolbarBackground(.bwellPurple, for: .navigationBar)
        .onAppear {
            guard let sdk = sdkManager.sdk else { return }
            viewModel.configure(sdk: sdk)
            Task { await viewModel.loadMembers() }
        }
        .alert("Error", isPresented: .constant(viewModel.mutationError != nil)) {
            Button("OK") { viewModel.mutationError = nil }
        } message: {
            if let error = viewModel.mutationError {
                Text(error)
            }
        }
        .sheet(item: $selectedMember) { member in
            NavigationStack {
                ProviderDetailView(result: member.toSearchResult())
                    .environmentObject(sdkManager)
            }
        }
        .onChange(of: selectedMember) { oldValue, newValue in
            if oldValue != nil && newValue == nil {
                Task { await viewModel.loadMembers() }
            }
        }
    }

    private var membersList: some View {
        List {
            Section {
                ForEach(viewModel.members) { member in
                    CareTeamMemberRow(
                        member: member,
                        onTap: {
                            selectedMember = member
                        },
                        onRemoveFromCareTeam: {
                            Task { await viewModel.removeMember(member) }
                        }
                    )
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
    let member: DisplayableCareTeamMember
    let onTap: () -> Void
    let onRemoveFromCareTeam: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Circle()
                .fill(Color.bwellPurple.opacity(0.15))
                .frame(width: 44, height: 44)
                .overlay {
                    Image(systemName: memberIcon)
                        .font(.title3)
                        .foregroundStyle(.bwellPurple)
                }

            VStack(alignment: .leading, spacing: 6) {
                Text(member.displayName)
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundStyle(.primary)

                HStack(spacing: 6) {
                    Chip(text: "Care Team", color: .teal)

                    if member.isPCP {
                        Chip(text: "PCP", color: .orange)
                    }
                }
            }

            Spacer()

            Button {
                onRemoveFromCareTeam()
            } label: {
                Image(systemName: "person.badge.minus")
                    .font(.body)
                    .foregroundStyle(.red)
                    .padding(8)
            }
            .buttonStyle(.borderless)
        }
        .padding(.vertical, 6)
        .contentShape(Rectangle())
        .onTapGesture {
            onTap()
        }
    }

    private var memberIcon: String {
        guard let type = member.type else { return "person.fill" }
        switch type.lowercased() {
        case "organization": return "building.2.fill"
        case "practitionerrole", "practitioner": return "stethoscope"
        case "relatedperson": return "person.2.fill"
        default: return "person.fill"
        }
    }
}

// MARK: - Chip

private struct Chip: View {
    let text: String
    let color: Color

    var body: some View {
        Text(text)
            .font(.caption2)
            .fontWeight(.medium)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(color.opacity(0.12))
            .foregroundStyle(color)
            .clipShape(Capsule())
    }
}
