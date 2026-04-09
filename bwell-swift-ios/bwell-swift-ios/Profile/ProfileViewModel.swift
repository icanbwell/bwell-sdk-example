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
    @Published var errorMessage: String?
    @Published var isLoading: Bool = false

    // Verification
    @Published var verificationStatus: String?
    @Published var isVerified = false
    @Published var verificationURL: String?
    @Published var isLoadingVerification = false

    var isVerificationFailed: Bool { verificationStatus == "val-fail" }

    // User's name
    @Published var givenName: String = "John"
    @Published var familyName: String = "Doe"

    // User's address
    @Published var addressLineOne: String = "145 W Osten St"
    @Published var addressLineTwo: String = "Suite 300"
    @Published var country: String = "United States"
    @Published var postalCode: String = "21230"
    @Published var state: String = "Maryland"
    @Published var city: String = "Baltimore"

    // Other user's information
    @Published var gender: BWell.Gender = .male
    @Published var birthdate: String = "January 2, 1990"
    @Published var language: String = "English"
    @Published var email: String = "example@example.com"
    @Published var workPhone: String = "221-923-1033"
    @Published var homePhone: String = "221-629-2253"
    @Published var mobilePhone: String = "221-812-7803"

    func getUserProfile(sdk: BWellClient) async {
        isLoading = true
        do {
            guard let profileInformation = try await sdk.user.getProfile() else {
                isLoading = false
                return
            }

            parseData(data: profileInformation)
            isLoading = false
        } catch {
            print("Failed retrieving user's profile information: \(error)")
            isLoading = false
        }
    }

    func updateUserProfile(sdk: BWellClient) async {
        isLoading = true
        errorMessage = nil
        do {
            print("new state: \(state)")
            let given: [String] = givenName.components(separatedBy: " ")

            let userName: BWell.HumanName = .init(family: familyName, given: given)

            let updateProfileRequest: BWell.UpdateUserProfileRequest = .init(name: userName,
                                                                             addressStreet: addressLineOne,
                                                                             addressUnit: addressLineTwo,
                                                                             city: city,
                                                                             stateOrProvidence: state,
                                                                             postageOrZipCode: postalCode,
                                                                             homePhone: homePhone.isEmpty ? nil : homePhone,
                                                                             mobilePhone: mobilePhone.isEmpty ? nil : mobilePhone,
                                                                             workPhone: workPhone.isEmpty ? nil : workPhone,
                                                                             email: email.isEmpty ? nil : email,
                                                                             birthDate: birthdate,
                                                                             gender: gender,
                                                                             language: language)

            _ = try await sdk.user.updateProfile(updateProfileRequest)

            isLoading = false
        } catch {
            NSLog("[Profile] Update failed: %@", String(describing: error))
            errorMessage = String(describing: error)
            isLoading = false
        }
    }

    func getVerificationStatus(sdk: BWellClient) async {
        isLoadingVerification = true
        do {
            let result = try await sdk.user.getVerificationStatus()
            verificationStatus = result?.status
            isVerified = result?.status == "validated"
        } catch {
            verificationStatus = nil
        }
        isLoadingVerification = false
    }

    func createVerificationURL(sdk: BWellClient) async {
        do {
            let json = """
            {"callbackURL":"bwell://ial2-callback","includeAttributeMatchingCheck":true}
            """
            let request = try JSONDecoder().decode(
                BWell.CreateVerificationURLRequest.self,
                from: json.data(using: .utf8)!
            )
            verificationURL = try await sdk.user.createVerificationURL(request)
        } catch {
            errorMessage = "Failed to generate verification URL."
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
            for contactPoint in telecom {
                if contactPoint.use == "email" {
                    email = contactPoint.value ?? ""
                } else if contactPoint.use == "mobile" {
                    mobilePhone = contactPoint.value ?? ""
                } else if contactPoint.use == "work" {
                    workPhone = contactPoint.value ?? ""
                } else if contactPoint.use == "home" {
                    homePhone = contactPoint.value ?? ""
                }
            }
        }
    }
}
