//
//  ProfileViewModel.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 29/10/25.
//

import Foundation
import BWellSDK

@MainActor
final class ProfileViewModel: ObservableObject {
    private var sdkManager: BWellSDKManager?
    @Published var errorMessage: String?
    @Published var isLoading: Bool = false

    // User's name
    @Published var givenName: String = "Given"
    @Published var familyName: String = "Family"

    // User's address
    @Published var addressLineOne: String = ""
    @Published var addressLineTwo: String = ""
    @Published var country: String = ""
    @Published var postalCode: String = ""
    @Published var state: String = ""
    @Published var city: String = ""

    // Other user's information
    @Published var gender: BWell.Gender = .unknown
    @Published var birthdate: String = ""
    @Published var language: String = "N/A"
    @Published var contactPoint: [String:String] = [
        "system": "",
        "value": "user@example.com",
        "use": ""
    ]

    init() { }

    func setup(sdkManager: BWellSDKManager) {
        self.sdkManager = sdkManager
    }

    func getUserProfile() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            guard let profileInformation = try await sdkManager.user().getProfile() else {
                isLoading = false
                return
            }

            parseData(data: profileInformation)
            isLoading = false
        } catch {
            print("❌ Failed with error: \(error)")
            isLoading = false
        }
    }

    func updateUserProfile() async {
        isLoading = true
        do {
            guard let sdkManager = sdkManager else {
                errorMessage = "SDK Manager not available"
                isLoading = false
                return
            }

            print("new state: \(state)")
            let given: [String] = givenName.components(separatedBy: " ")

            let userName: BWell.HumanName = .init(family: familyName, given: given)

            let updateProfileRequest: BWell.UpdateUserProfileRequest = .init(name: userName,
                                                                             addressStreet: addressLineOne,
                                                                             addressUnit: addressLineTwo,
                                                                             city: city,
                                                                             stateOrProvidence: state,
                                                                             postageOrZipCode: postalCode,
                                                                             birthDate: birthdate,
                                                                             gender: gender,
                                                                             language: language)

            _ = try await sdkManager.user().updateProfile(updateProfileRequest)
            isLoading = false
        } catch {
            isLoading = false
        }
    }

    private func parseData(data: BWell.GetUserProfileResult) {
        if let names = data.name {
            if let first = names.first {
                givenName = first.given?.joined(separator: " ") ?? ""
                familyName = first.family ?? ""
            }
        }

        if let address = data.address {
            if let first = address.first {
                if let lines = first.line {
                    addressLineOne = lines[0]

                    if lines.count > 1 {
                        addressLineTwo = lines[1]
                    }
                }
                country = first.country ?? ""
                state = first.state ?? ""
                postalCode = first.postalCode ?? ""
                city = first.city ?? ""
            }
        }

        if let gender = data.gender {
            self.gender = gender
        }

        if let birthdate = data.birthdate {
            self.birthdate = birthdate
        }

        if let language = data.language {
            self.language = language
        }

        if let telecom = data.telecom {
            if let contactPoint = telecom.first {
                guard let system = contactPoint.system,
                      let value = contactPoint.value,
                      let use = contactPoint.use else { return }

                self.contactPoint["system"] = system
                self.contactPoint["value"] = value
                self.contactPoint["use"] = use
            }
        }
    }
}
