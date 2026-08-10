//
//  HealthSyncPlaygroundEndpoint.swift
//  bwell-swift-ios
//
//  Adapted from bwell-sdk-swift/Examples/HealthSyncSampleApp/Sources/PlaygroundEndpoint.swift.
//  The composition primitives (`connect`/`disconnect`), the HealthSync port's
//  own primitives (`requestPermissions`/`sync`), and the 9 SDK methods
//  `ui-platform` uses. `getHealthScore`/`getBodySystemScore`/`getDeviceMetrics`/
//  `getDeviceMetricsGroups` are all live on the real SDK - confirmed against
//  bwell-sdk-swift 1.7.0's `HealthDataManager` (the internal sample's
//  "blocked" notes were stale, and it also had the last one's name wrong -
//  it's `getDeviceMetricsGroups`, not `getDeviceMetricGroups`). No blocked
//  endpoints remain in this set.
//

import Foundation

enum HealthSyncPlaygroundGroup: String, CaseIterable {
    case mobileSync = "Mobile Sync"
    case cloudProviders = "Cloud Providers"
    case readData = "Read Data"
}

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
    case getDeviceMetricsGroups
    case getHealthScore
    case getBodySystemScore

    var id: String { rawValue }

    var title: String { rawValue }

    var isBlocked: Bool { false }

    var blockedReason: String? { nil }

    var group: HealthSyncPlaygroundGroup {
        switch self {
        case .setupMobileSync, .getDeviceUserStatus, .requestPermissions, .connect, .sync, .disconnect:
            return .mobileSync
        case .getDeviceProviders, .getOauthUrl, .getDeviceProviderStatus, .deleteConnection:
            return .cloudProviders
        case .getDeviceMetrics, .getDeviceMetricsGroups, .getHealthScore, .getBodySystemScore:
            return .readData
        }
    }

    /// Per-method doc page - confirmed live on developer.bwell.com's Swift
    /// SDK section for 12 of 14 endpoints. `getOauthUrl`/`deleteConnection`
    /// have no Swift-specific page (only Android/Kotlin equivalents exist),
    /// so they fall through to nil rather than link the wrong platform's doc.
    var documentationURL: URL? {
        let path: String?
        switch self {
        case .connect: path = "connect-mobile-sync"
        case .disconnect: path = "disconnect-mobile-sync"
        case .requestPermissions: path = "request-permissions"
        case .sync: path = "sync-health-data"
        case .setupMobileSync: path = "setup-mobile-sync"
        case .getDeviceUserStatus: path = "get-device-user-status"
        case .getDeviceProviderStatus: path = "get-device-provider-status"
        case .getDeviceProviders: path = "get-device-providers"
        case .getDeviceMetrics: path = "device-metrics"
        case .getDeviceMetricsGroups: path = "device-metrics-groups"
        case .getHealthScore: path = "health-score"
        case .getBodySystemScore: path = "body-system-score"
        case .getOauthUrl, .deleteConnection: path = nil
        }
        return path.flatMap { URL(string: "https://developer.bwell.com/docs/\($0)") }
    }

    /// Longer, doc-sourced description for the info sheet - quoted directly
    /// from each method's real developer.bwell.com page (see docs above).
    var documentationSummary: String? {
        switch self {
        case .connect:
            return "The connect method in the b.well SDK establishes an on-device health sync session for a given source, composing the underlying provisioning steps automatically. This is the recommended entry point for connecting an on-device health source — prefer it over calling setupMobileSync directly."
        case .disconnect:
            return "The disconnect method in the b.well SDK ends an on-device health sync session and removes the underlying connection."
        case .requestPermissions:
            return "The requestPermissions method in the b.well SDK requests user authorization for the specified health data types from the connected on-device health source."
        case .sync:
            return "The sync method in the b.well SDK reads health data of the specified types, within a given date range, from the connected on-device health source."
        case .setupMobileSync:
            return "The setupMobileSync method in the b.well SDK provisions the backend credentials needed to sync data from an on-device health source (e.g. a phone or wearable's local health store) for a given connection."
        case .getDeviceUserStatus:
            return "The getDeviceUserStatus method in the b.well SDK checks whether a device-user record already exists for a given connection, and returns its identifier if so."
        case .getDeviceProviderStatus:
            return "The getDeviceProviderStatus method in the b.well SDK performs a lightweight, database-only check of whether a given connection is currently connected, without making an external network call to the provider itself."
        case .getDeviceProviders:
            return "The getDeviceProviders method in the b.well SDK retrieves the list of health data providers available for connection, along with each provider's connection status."
        case .getDeviceMetrics:
            return "The getDeviceMetrics method in the b.well SDK retrieves a flat, paginated list of raw Observation resources synced from a connected device or on-device health source."
        case .getDeviceMetricsGroups:
            return "The getDeviceMetricsGroups method in the b.well SDK fetches a list of DeviceMetricsGroup resources, each representing connected-device metrics organized into a named group."
        case .getHealthScore:
            return "The getHealthScore method in the b.well SDK retrieves the authenticated patient's overall health score, including its trend, contributing body systems, and daily score history."
        case .getBodySystemScore:
            return "The getBodySystemScore method in the b.well SDK retrieves a detailed score for a single body system (e.g. cardiovascular, sleep, respiratory, musculoskeletal) for the authenticated patient."
        case .getOauthUrl, .deleteConnection:
            return nil
        }
    }

    var parameterInfo: String? {
        switch self {
        case .getOauthUrl, .getDeviceProviderStatus:
            return "Provider - pick a cloud health provider from the dropdown."
        case .deleteConnection:
            return "Provider - pick a provider to prefill the connection id.\nconnectionId - the connection to permanently delete."
        case .getBodySystemScore:
            return "bodySystemId - which body system to score (e.g. cardiovascular, sleep, respiratory, musculoskeletal)."
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
            return "Returns your raw synced health records. Calls getDeviceMetrics."
        case .getDeviceMetricsGroups:
            return "Returns your synced health records grouped by metric type. Calls getDeviceMetricsGroups."
        case .getHealthScore:
            return "Returns your overall health score. Calls getHealthScore."
        case .getBodySystemScore:
            return "Returns a health score for one body system. Calls getBodySystemScore."
        }
    }
}
