//
//  BWellSDKManager+Helpers.swift
//  bwell-swift-ios
//
//  Created by Ivan Villanueva on 11/11/25.
//
import Foundation

enum SDKState {
    case uninitialized
    case initializing
    case initialized
    case checkingSession
    case unauthenticated
    case authenticating
    case authenticated

    case failed(Error)
}

extension SDKState: Equatable {
    static func == (lhs: SDKState, rhs: SDKState) -> Bool {
        switch (lhs, rhs) {
        case (.uninitialized, .uninitialized):
            return true
        case (.initializing, .initializing):
            return true
        case (.initialized, .initialized):
            return true
        case (.unauthenticated, .unauthenticated):
            return true
        case (.authenticating, .authenticating):
            return true
        case (.authenticated, .authenticated):
            return true
        case (.failed, .failed):
            return false
        default:
            return false
        }
    }
}

enum SDKError: Error {
    case notInitialized
    case invalidCredentials
    case createConsentNotCreated

    var errorDescription: String? {
        switch self {
            case .notInitialized:
                return "The BWell SDK has not been initialized. Please call initializeSDK() before using any SDK functionality."
            case .invalidCredentials:
                return "The provided credentials for the BWell SDK are invalid."
            case .createConsentNotCreated:
                return "The TOS consent could not be created."
        }
    }
}
