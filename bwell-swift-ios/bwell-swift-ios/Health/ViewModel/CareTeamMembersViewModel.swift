//
//  CareTeamMembersViewModel.swift
//  bwell-swift-ios
//
//  ViewModel for care team member queries and mutations.
//

import Foundation
import BWellSDK

@MainActor
final class CareTeamMembersViewModel: ObservableObject {
    @Published var members: [BWell.CareTeamMember] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var mutationError: String?
    @Published var showPlanIds = false
    @Published var planIdsMessage = ""

    @Published var newMemberReference = ""
    @Published var newMemberType = ""
    @Published var newMemberDisplay = ""

    private weak var sdk: BWellClient?

    func configure(sdk: BWellClient) {
        self.sdk = sdk
    }

    func loadMembers() async {
        guard let sdk else { return }
        isLoading = true
        errorMessage = nil

        do {
            let request = BWell.CareTeamMembersRequest(page: 0, pageSize: 20)
            let response = try await sdk.health.getCareTeamMembers(request)
            members = response.members ?? []

            #if DEBUG
            print("=== Care Team Members Response ===")
            print("Total: \(response.pagingInfo?.totalItems ?? 0)")
            for member in members {
                print("  id=\(member.id ?? "nil"), roles=\(member.role?.compactMap { $0.text } ?? [])")
            }
            print("===================================")
            #endif
        } catch {
            errorMessage = "Failed to load care team members: \(error.localizedDescription)"
        }
        isLoading = false
    }

    func addMember() async {
        guard let sdk else { return }
        let reference = newMemberReference.trimmingCharacters(in: .whitespaces)
        guard !reference.isEmpty else { return }

        let type = newMemberType.trimmingCharacters(in: .whitespaces)
        let display = newMemberDisplay.trimmingCharacters(in: .whitespaces)

        let participant = BWell.CareTeamParticipantInput(
            member: BWell.ReferenceInput(
                reference: reference,
                type: type.isEmpty ? nil : type,
                display: display.isEmpty ? nil : display
            )
        )

        do {
            let request = BWell.AddCareTeamMemberRequest(participant: participant)
            let result = try await sdk.health.addCareTeamMember(request)
            #if DEBUG
            print("addCareTeamMember success: id=\(result.id ?? "nil")")
            #endif
            newMemberReference = ""
            newMemberType = ""
            newMemberDisplay = ""
            await loadMembers()
        } catch {
            mutationError = "Failed to add member: \(error.localizedDescription)"
        }
    }

    func removeMember(_ member: BWell.CareTeamMember) async {
        guard let sdk, let memberId = member.id else { return }

        let participant = BWell.CareTeamParticipantInput(
            member: BWell.ReferenceInput(reference: memberId)
        )

        do {
            let request = BWell.RemoveCareTeamMemberRequest(participant: participant)
            let result = try await sdk.health.removeCareTeamMember(request)
            #if DEBUG
            print("removeCareTeamMember success: id=\(result.id ?? "nil")")
            #endif
            await loadMembers()
        } catch {
            mutationError = "Failed to remove member: \(error.localizedDescription)"
        }
    }

    func updateMember(_ member: BWell.CareTeamMember, newDisplay: String) async {
        guard let sdk, let memberId = member.id else { return }

        let participant = BWell.CareTeamParticipantInput(
            member: BWell.ReferenceInput(reference: memberId, display: newDisplay)
        )

        do {
            let request = BWell.UpdateCareTeamMemberRequest(participant: participant)
            let result = try await sdk.health.updateCareTeamMember(request)
            #if DEBUG
            print("updateCareTeamMember success: id=\(result.id ?? "nil")")
            #endif
            await loadMembers()
        } catch {
            mutationError = "Failed to update member: \(error.localizedDescription)"
        }
    }

    func getMemberPlanIdentifiers() async {
        guard let sdk else { return }

        do {
            let result = try await sdk.health.getMemberPlanIdentifiers()
            let ids = result.identifiers?.compactMap { $0 } ?? []
            planIdsMessage = ids.isEmpty ? "No plan identifiers found." : ids.joined(separator: "\n")
            showPlanIds = true
            #if DEBUG
            print("getMemberPlanIdentifiers: \(ids)")
            #endif
        } catch {
            mutationError = "Failed to get plan identifiers: \(error.localizedDescription)"
        }
    }
}
