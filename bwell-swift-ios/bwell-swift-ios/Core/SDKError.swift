//
//  SDKError.swift
//  bwell-swift-ios
//
//  Typed errors for SDK operations with user-friendly messages.
//

import Foundation

enum SDKError: LocalizedError {
    case notInitialized
    case invalidCredentials
    case authenticationFailed(underlying: Error)
    case networkError(underlying: Error)
    case sessionExpired
    case consentCreationFailed(underlying: Error)

    var errorDescription: String? {
        switch self {
        case .notInitialized:
            return "SDK must be initialized before use. Please provide a valid client key."
        case .invalidCredentials:
            return "The provided credentials are invalid. Please check and try again."
        case .authenticationFailed(let error):
            return "Authentication failed: \(error.localizedDescription)"
        case .networkError(let error):
            return "Network error: \(error.localizedDescription)"
        case .sessionExpired:
            return "Your session has expired. Please log in again."
        case .consentCreationFailed(let error):
            return "Failed to create consent: \(error.localizedDescription)"
        }
    }

    var recoverySuggestion: String? {
        switch self {
        case .notInitialized:
            return "Initialize the SDK with a valid client key from your bWell dashboard."
        case .invalidCredentials:
            return "Verify your email and password are correct."
        case .authenticationFailed, .networkError:
            return "Check your network connection and try again."
        case .sessionExpired:
            return "Please log in again to continue."
        case .consentCreationFailed:
            return "Try logging in again. Contact support if the problem persists."
        }
    }
}
