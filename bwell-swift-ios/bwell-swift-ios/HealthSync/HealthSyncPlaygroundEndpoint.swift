//
//  HealthSyncPlaygroundEndpoint.swift
//  bwell-swift-ios
//
//  Ported from bwell-sdk-swift/Examples/HealthSyncSampleApp/Sources/PlaygroundEndpoint.swift.
//  The composition primitives (`connect`/`disconnect`), the HealthSync port's
//  own primitives (`requestPermissions`/`sync`), and the 9 SDK methods
//  `ui-platform` uses. `getHealthScore`/`getBodySystemScore` are blocked per
//  Bill's DCON-4456 ruling. `getDeviceMetrics`/`getDeviceMetricGroups` are
//  also blocked: `getDeviceMetricGroups` was dropped from the SDK per Bill's
//  #927 review, which also removes the only way to discover a valid
//  `groupCode` to drill `getDeviceMetrics` into one specific metric. None of
//  this is a bug in this demo.
//

import Foundation

enum HealthSyncPlaygroundEndpoint: String, PlaygroundEndpoint {
    case connect
    case disconnect
    case requestPermissions
    case sync
    case getOauthUrl
    case setupMobileSync
    case getDeviceUserStatus
    case getDeviceProviderStatus
    case getDeviceProviders
    case deleteConnection
    case getDeviceMetrics
    case getDeviceMetricGroups
    case getHealthScore
    case getBodySystemScore

    var id: String { rawValue }

    var title: String { rawValue }

    var isBlocked: Bool {
        switch self {
        case .getHealthScore, .getBodySystemScore, .getDeviceMetrics, .getDeviceMetricGroups: true
        default: false
        }
    }

    var blockedReason: String? {
        switch self {
        case .getHealthScore, .getBodySystemScore:
            return "Blocked on DCON-4456 — not available on the Swift SDK yet"
        case .getDeviceMetrics, .getDeviceMetricGroups:
            return "Blocked in this demo — getDeviceMetricGroups was dropped from the SDK per #927 review, which also removed the only way to pick a specific metric to drill into. Returns once DCON-4456 ships a real grouping resolver"
        default:
            return nil
        }
    }

    /// Plain-English "what this does", naming the real SDK method(s) it
    /// calls under the hood — vendor-neutral (no mention of the on-device
    /// health source vendor).
    var explanation: String {
        switch self {
        case .connect:
            return "Starts syncing your health data with b.well. Calls setupMobileSync, then starts your on-device session."
        case .disconnect:
            return "Stops syncing and removes your connection from b.well. Ends your on-device session, then calls deleteConnection."
        case .requestPermissions:
            return "Asks your device for permission to read your health data. Calls requestPermissions."
        case .sync:
            return "Reads your health data from this device and uploads it to b.well. Calls sync."
        case .getOauthUrl:
            return "Gets a link to authorize a cloud health provider account. Calls getOauthUrl."
        case .setupMobileSync:
            return "Sets up the credentials this device needs to sync health data. Calls setupMobileSync."
        case .getDeviceUserStatus:
            return "Checks whether this connection already has a device user set up. Calls getDeviceUserStatus."
        case .getDeviceProviderStatus:
            return "Checks whether a specific health provider is already connected. Calls getDeviceProviderStatus."
        case .getDeviceProviders:
            return "Lists every health data provider available to connect. Calls getDeviceProviders."
        case .deleteConnection:
            return "Permanently deletes a connection and all its data. Calls deleteConnection."
        case .getDeviceMetrics:
            return "Would return your raw synced health records. Calls getDeviceMetrics."
        case .getDeviceMetricGroups:
            return "Would return your synced health records grouped by metric type. Calls getDeviceMetricGroups."
        case .getHealthScore:
            return "Would return your overall health score. Calls getHealthScore."
        case .getBodySystemScore:
            return "Would return a health score broken down by body system. Calls getBodySystemScore."
        }
    }
}
