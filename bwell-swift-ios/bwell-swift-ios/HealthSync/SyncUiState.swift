//
//  SyncUiState.swift
//  bwell-swift-ios
//
//  Ported from bwell-sdk-swift/Examples/HealthSyncSampleApp/Sources/SyncUiState.swift.
//  The Health Sync demo's own screen state machine. Not SDK surface.
//

import Foundation

enum SyncUiState {
    case setup(error: String? = nil)
    case playground
    case reauthenticate(error: String? = nil)
}
