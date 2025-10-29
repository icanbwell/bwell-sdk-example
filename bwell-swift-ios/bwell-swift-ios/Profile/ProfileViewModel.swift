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
    @Published var addressLine: String = ""
    @Published var country: String = ""
    @Published var postalCode: String = ""
    @Published var state: String = ""
    @Published var city: String = ""

    // Other user's information
    @Published var gender: String = ""
    @Published var birthdate: String = ""
    @Published var language: String = "No language provided"
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
            print("PROFILE INFORMATION: \n \(String(describing: profileInformation))")
            isLoading = false
        } catch {
            print("❌ Failed with error: \(error)")
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
                addressLine = first.line?.joined(separator: ", ") ?? ""
                country = first.country ?? ""
                state = first.state ?? ""
                postalCode = first.postalCode ?? ""
                city = first.city ?? ""
            }
        }

        if let gender = data.gender {
            self.gender = switch gender {
                case .male: "Male"
                case .female: "Female"
                case .other: "Other"
                case .unknown: "Unknown"
            }
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

extension String {
    func dateFormatter() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"

        guard let date = formatter.date(from: self) else { return self }

        let outputFormatter = DateFormatter()
        outputFormatter.dateFormat = "MMMM dd, yyyy"

        return outputFormatter.string(from: date)
    }
}
