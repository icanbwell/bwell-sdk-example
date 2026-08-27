//
//  SDKState.swift
//  bwell-swift-ios
//
//  SDK lifecycle states.
//

import Foundation

enum SDKState: Equatable {
    case uninitialized
    case initializing
    case initialized
    case authenticating
    case authenticated
    case failed(SDKError)

    static func == (lhs: SDKState, rhs: SDKState) -> Bool {
        switch (lhs, rhs) {
        case (.uninitialized, .uninitialized),
             (.initializing, .initializing),
             (.initialized, .initialized),
             (.authenticating, .authenticating),
             (.authenticated, .authenticated):
            return true
        case (.failed(let lErr), .failed(let rErr)):
            return lErr.localizedDescription == rErr.localizedDescription
        default:
            return false
        }
    }
}
