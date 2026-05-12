//
//  CareTeamMembersViewModel.swift
//  bwell-swift-ios
//

import Foundation
import BWellSDK

struct DisplayableCareTeamMember: Identifiable, Equatable {
    let id: String
    let display: String?
    let reference: String?
    let type: String?
    let roles: [BWell.CodeableConcept]?

    static func == (lhs: DisplayableCareTeamMember, rhs: DisplayableCareTeamMember) -> Bool {
        lhs.id == rhs.id
    }

    var displayName: String {
        if let display, !display.isEmpty { return display }
        if let reference, !reference.isEmpty {
            let parts = reference.split(separator: "/")
            if parts.count >= 2 {
                return String(parts.last!)
            }
            return reference
        }
        return id
    }

    var isPCP: Bool {
        guard let roles else { return false }
        return roles.contains { concept in
            if let text = concept.text, text.localizedCaseInsensitiveContains("pcp") { return true }
            if let codings = concept.coding {
                return codings.contains { coding in
                    coding.code?.localizedCaseInsensitiveContains("pcp") == true ||
                    coding.display?.localizedCaseInsensitiveContains("primary care") == true
                }
            }
            return false
        }
    }

    func toSearchResult() -> BWell.SearchHealthResourcesResults.Result {
        let json: [String: Any] = ["id": id, "content": displayName]
        let data = try! JSONSerialization.data(withJSONObject: json)
        return try! JSONDecoder().decode(BWell.SearchHealthResourcesResults.Result.self, from: data)
    }
}

@MainActor
final class CareTeamMembersViewModel: ObservableObject {
    @Published var members: [DisplayableCareTeamMember] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var mutationError: String?

    private weak var sdk: BWellClient?

    func configure(sdk: BWellClient) {
        self.sdk = sdk
    }

    func loadMembers() async {
        guard let sdk else { return }
        isLoading = true
        errorMessage = nil

        do {
            let request = BWell.CareTeamsRequest(page: 0)
            let response = try await sdk.health.getCareTeams(request)
            let careTeams = response.entry?.compactMap { $0.resource } ?? []

            var seen = Set<String>()
            var allMembers: [DisplayableCareTeamMember] = []
            for team in careTeams {
                guard let participants = team.participant else { continue }
                for participant in participants {
                    let memberId = Self.extractResourceId(from: participant.member)
                    guard !seen.contains(memberId) else { continue }
                    seen.insert(memberId)
                    let memberType = participant.member?.type ?? Self.extractResourceType(from: participant.member?.reference)
                    let member = DisplayableCareTeamMember(
                        id: memberId,
                        display: participant.member?.display,
                        reference: participant.member?.reference,
                        type: memberType,
                        roles: participant.role
                    )
                    allMembers.append(member)
                }
            }
            members = allMembers

            #if DEBUG
            print("=== Care Team Members (from getCareTeams) ===")
            print("Teams: \(careTeams.count), Total participants: \(allMembers.count)")
            for member in allMembers {
                print("  id=\(member.id), display=\(member.display ?? "nil"), type=\(member.type ?? "nil"), pcp=\(member.isPCP)")
            }
            print("===============================================")
            #endif
        } catch {
            errorMessage = "Failed to load care team members: \(error.localizedDescription)"
        }
        isLoading = false
    }

    private static func extractResourceId(from member: BWell.CareTeam.Participant.MemberReference?) -> String {
        if let reference = member?.reference {
            let parts = reference.split(separator: "/")
            if parts.count >= 2 {
                return String(parts.last!)
            }
            return reference
        }
        return member?.id ?? UUID().uuidString
    }

    private static func extractResourceType(from reference: String?) -> String? {
        guard let reference else { return nil }
        let parts = reference.split(separator: "/")
        if parts.count >= 2 {
            return String(parts.first!)
        }
        return nil
    }

    func removeMember(_ member: DisplayableCareTeamMember) async {
        guard let sdk else { return }

        let memberType = Self.careTeamMemberType(from: member.type)
        #if DEBUG
        print("removeCareTeamMember attempting: id=\(member.id), type=\(member.type ?? "nil") → \(memberType)")
        #endif

        do {
            let request = BWell.RemoveCareTeamMemberRequest(id: member.id, type: memberType)
            let result = try await sdk.health.removeCareTeamMember(request)
            #if DEBUG
            print("removeCareTeamMember success: id=\(result.id ?? "nil")")
            #endif
            members.removeAll { $0.id == member.id }
        } catch {
            #if DEBUG
            print("removeCareTeamMember FAILED: \(error)")
            #endif
            mutationError = "Failed to remove member: \(error.localizedDescription)"
        }
    }

    private static func careTeamMemberType(from type: String?) -> BWell.CareTeamMemberType {
        guard let type else { return .Practitioner }
        switch type.lowercased() {
        case "practitioner": return .Practitioner
        case "practitionerrole": return .PractitionerRole
        case "organization": return .Organization
        case "patient": return .Patient
        case "relatedperson": return .RelatedPerson
        default: return .Practitioner
        }
    }
}
