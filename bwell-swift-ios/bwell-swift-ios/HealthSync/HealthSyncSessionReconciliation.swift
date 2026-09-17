//
//  HealthSyncSessionReconciliation.swift
//  bwell-swift-ios
//
//  TEMPORARY demo-side workaround for a known SDK gap (DCON-4936, fixed in
//  icanbwell/bwell-sdk#955 - not yet published to a real release). connect()
//  throws HealthSyncError.sessionConflict if the on-device session belongs
//  to a different user than the one currently logged into b.well; nothing
//  reconciles that automatically yet.
//
//  Public-API-only reconciliation, matching what #955 does inside the SDK
//  itself: getCurrentUser() (the live on-device session's user, or nil on a
//  fresh install/device) vs getDeviceUserStatus() (whether THIS logged-in
//  b.well user already has a device-user record, and if so, which one).
//  Ends the stale session first if they disagree - covers both real cases
//  confirmed against the live adapter: a brand-new user with no prior
//  record (`exists: false`), and an existing user whose own record isn't
//  the one with the currently active session (`exists: true`, different id).
//
//  Delete this file and its one call site in each ViewModel once #955 ships
//  in a real bwell-sdk-swift-package release - connect() will do this itself.
//

import BWellHealthSync
import BWellSDK

func reconcileHealthSyncSessionIfNeeded(client: BWellClient, connectionId: String) async throws {
    let healthSync = try client.healthSync
    guard let currentUser = try await healthSync.currentUser() else {
        return // first sync ever on this device - nothing to reconcile
    }

    let status = try await client.device.getDeviceUserStatus(.init(connectionId: connectionId))
    let matches = status.exists && status.deviceUserId == currentUser.userId
    if !matches {
        try await healthSync.endSession()
    }
}
