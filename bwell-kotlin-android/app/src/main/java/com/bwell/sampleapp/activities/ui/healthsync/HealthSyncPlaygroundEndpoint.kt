package com.bwell.sampleapp.activities.ui.healthsync

/**
 * Ported from bwell-swift-ios's HealthSyncPlaygroundEndpoint.swift - same 14
 * endpoints (+ getCurrentUser, 15 total) grouped identically, same
 * user-facing copy verbatim. [isBlocked]/[blockedReason] are intentionally
 * NOT stored here (Swift keeps them static `false`/`nil`, unused) - whether
 * an endpoint is blocked depends on runtime state
 * ([com.bwell.healthsync.BWellHealthSync.isConfigured]), so it's computed by
 * [HealthSyncGating] instead of baked into this otherwise-static spec.
 */
enum class HealthSyncPlaygroundGroup(val title: String) {
    MOBILE_SYNC("Mobile Sync"),
    CLOUD_PROVIDERS("Cloud Providers"),
    READ_DATA("Read Data"),
}

enum class HealthSyncPlaygroundEndpoint(
    val title: String,
    val group: HealthSyncPlaygroundGroup,
    val explanation: String,
    val documentationPath: String?,
    val documentationSummary: String?,
    val parameterInfo: String? = null,
) {
    CONNECT(
        title = "connect",
        group = HealthSyncPlaygroundGroup.MOBILE_SYNC,
        explanation = "Starts syncing your health data with b.well. Calls setupMobileSync, then starts your on-device session.",
        documentationPath = "connect-mobile-sync",
        documentationSummary = "The connect method in the b.well SDK establishes an on-device health sync session for a given source, composing the underlying provisioning steps automatically. This is the recommended entry point for connecting an on-device health source — prefer it over calling setupMobileSync directly.",
    ),
    DISCONNECT(
        title = "disconnect",
        group = HealthSyncPlaygroundGroup.MOBILE_SYNC,
        explanation = "Stops syncing and removes your connection from b.well. Ends your on-device session, then calls deleteConnection.",
        documentationPath = "disconnect-mobile-sync",
        documentationSummary = "The disconnect method in the b.well SDK ends an on-device health sync session and removes the underlying connection.",
    ),
    REQUEST_PERMISSIONS(
        title = "requestPermissions",
        group = HealthSyncPlaygroundGroup.MOBILE_SYNC,
        explanation = "Asks your device for permission to read your health data. Calls requestPermissions.",
        documentationPath = "request-permissions",
        documentationSummary = "The requestPermissions method in the b.well SDK requests user authorization for the specified health data types from the connected on-device health source.",
    ),
    SYNC(
        title = "sync",
        group = HealthSyncPlaygroundGroup.MOBILE_SYNC,
        explanation = "Reads your health data from this device and uploads it to b.well. Calls sync.",
        documentationPath = "sync-health-data",
        documentationSummary = "The sync method in the b.well SDK reads health data of the specified types, within a given date range, from the connected on-device health source.",
    ),
    GET_CURRENT_USER(
        title = "getCurrentUser",
        group = HealthSyncPlaygroundGroup.MOBILE_SYNC,
        explanation = "Shows the user of your active on-device session, if any. Calls currentUser.",
        documentationPath = null,
        documentationSummary = null,
    ),
    SETUP_MOBILE_SYNC(
        title = "setupMobileSync",
        group = HealthSyncPlaygroundGroup.MOBILE_SYNC,
        explanation = "Sets up the credentials this device needs to sync health data. Calls setupMobileSync.",
        documentationPath = "setup-mobile-sync",
        documentationSummary = "The setupMobileSync method in the b.well SDK provisions the backend credentials needed to sync data from an on-device health source (e.g. a phone or wearable's local health store) for a given connection.",
    ),
    GET_DEVICE_USER_STATUS(
        title = "getDeviceUserStatus",
        group = HealthSyncPlaygroundGroup.MOBILE_SYNC,
        explanation = "Checks whether this connection already has a device user set up. Calls getDeviceUserStatus.",
        documentationPath = "get-device-user-status",
        documentationSummary = "The getDeviceUserStatus method in the b.well SDK checks whether a device-user record already exists for a given connection, and returns its identifier if so.",
    ),
    GET_OAUTH_URL(
        title = "getOauthUrl",
        group = HealthSyncPlaygroundGroup.CLOUD_PROVIDERS,
        explanation = "Gets a link to authorize a cloud health provider account. Calls getOauthUrl.",
        documentationPath = null,
        documentationSummary = null,
        parameterInfo = "Provider - pick a cloud health provider from the dropdown.",
    ),
    GET_DEVICE_PROVIDER_STATUS(
        title = "getDeviceProviderStatus",
        group = HealthSyncPlaygroundGroup.CLOUD_PROVIDERS,
        explanation = "Checks whether a specific health provider is already connected. Calls getDeviceProviderStatus.",
        documentationPath = "get-device-provider-status",
        documentationSummary = "The getDeviceProviderStatus method in the b.well SDK performs a lightweight, database-only check of whether a given connection is currently connected, without making an external network call to the provider itself.",
        parameterInfo = "Provider - pick a cloud health provider from the dropdown.",
    ),
    GET_DEVICE_PROVIDERS(
        title = "getDeviceProviders",
        group = HealthSyncPlaygroundGroup.CLOUD_PROVIDERS,
        explanation = "Lists every health data provider available to connect. Calls getDeviceProviders.",
        documentationPath = "get-device-providers",
        documentationSummary = "The getDeviceProviders method in the b.well SDK retrieves the list of health data providers available for connection, along with each provider's connection status.",
    ),
    DELETE_CONNECTION(
        title = "deleteConnection",
        group = HealthSyncPlaygroundGroup.CLOUD_PROVIDERS,
        explanation = "Permanently deletes a connection and all its data. Calls deleteConnection.",
        documentationPath = null,
        documentationSummary = null,
        parameterInfo = "Provider - pick a provider to prefill the connection id.\nconnectionId - the connection to permanently delete.",
    ),
    GET_DEVICE_METRICS(
        title = "getDeviceMetrics",
        group = HealthSyncPlaygroundGroup.READ_DATA,
        explanation = "Returns your raw synced health records. Calls getDeviceMetrics.",
        documentationPath = "device-metrics",
        documentationSummary = "The getDeviceMetrics method in the b.well SDK retrieves a flat, paginated list of raw Observation resources synced from a connected device or on-device health source.",
        parameterInfo = "Code - which metric type to filter by. Populated automatically by calling getDeviceMetricsGroups() each time this card appears. Disabled until at least one code comes back - sync some data first (Mobile Sync group above).",
    ),
    GET_DEVICE_METRICS_GROUPS(
        title = "getDeviceMetricsGroups",
        group = HealthSyncPlaygroundGroup.READ_DATA,
        explanation = "Returns your synced health records grouped by metric type. Calls getDeviceMetricsGroups.",
        documentationPath = "device-metrics-groups",
        documentationSummary = "The getDeviceMetricsGroups method in the b.well SDK fetches a list of DeviceMetricsGroup resources, each representing connected-device metrics organized into a named group.",
    ),
    GET_HEALTH_SCORE(
        title = "getHealthScore",
        group = HealthSyncPlaygroundGroup.READ_DATA,
        explanation = "Returns your overall health score. Calls getHealthScore.",
        documentationPath = "health-score",
        documentationSummary = "The getHealthScore method in the b.well SDK retrieves the authenticated patient's overall health score, including its trend, contributing body systems, and daily score history.",
    ),
    GET_BODY_SYSTEM_SCORE(
        title = "getBodySystemScore",
        group = HealthSyncPlaygroundGroup.READ_DATA,
        explanation = "Returns a health score for one body system. Calls getBodySystemScore.",
        documentationPath = "body-system-score",
        documentationSummary = "The getBodySystemScore method in the b.well SDK retrieves a detailed score for a single body system (e.g. cardiovascular, sleep, respiratory, musculoskeletal) for the authenticated patient.",
        parameterInfo = "Body system - which body system to score. Populated automatically by calling getHealthScore() each time this card appears, from its contributing body systems. Disabled until at least one comes back - that requires enough synced data for a health score to be computed.",
    ),
    ;

    val documentationUrl: String?
        get() = documentationPath?.let { "https://developer.bwell.com/docs/$it" }
}

/**
 * The 5 endpoints that call [com.bwell.healthsync.HealthSyncManager]
 * directly and so require an on-device adapter to be registered via
 * [com.bwell.healthsync.BWellHealthSync.configure]. Everything else
 * (setupMobileSync/getDeviceUserStatus, Cloud Providers, Read Data) calls
 * core SDK managers that work in a credential-less build once logged in.
 */
private val HEALTH_SYNC_GATED_ENDPOINTS = setOf(
    HealthSyncPlaygroundEndpoint.CONNECT,
    HealthSyncPlaygroundEndpoint.DISCONNECT,
    HealthSyncPlaygroundEndpoint.REQUEST_PERMISSIONS,
    HealthSyncPlaygroundEndpoint.SYNC,
    HealthSyncPlaygroundEndpoint.GET_CURRENT_USER,
)

/** Locked copy - must match Swift's HealthSyncPlaygroundViewModel verbatim. */
const val HEALTH_SYNC_NOT_CONFIGURED_MESSAGE =
    "Health Sync on-device sync: some endpoints require a third-party health-data provider " +
        "integration. Credential provisioning for this integration is currently under " +
        "consideration — these endpoints will show a 'Health Sync Credentials Required' " +
        "state until that's finalized."

/**
 * Pre-emptive card-level gating: unlike Swift (which still lets a tap
 * happen and catches the resulting error), an unconfigured build disables
 * the 5 gated endpoints' Run buttons outright, so
 * [com.bwell.healthsync.BWellHealthSync]'s consuming code is never even
 * invoked while unconfigured (it would throw `IllegalStateException`, not
 * return a catchable error - see BWellHealthSync.kt).
 */
object HealthSyncGating {
    fun isBlocked(endpoint: HealthSyncPlaygroundEndpoint, configured: Boolean): Boolean =
        !configured && endpoint in HEALTH_SYNC_GATED_ENDPOINTS

    fun blockedReason(endpoint: HealthSyncPlaygroundEndpoint, configured: Boolean): String? =
        if (isBlocked(endpoint, configured)) HEALTH_SYNC_NOT_CONFIGURED_MESSAGE else null
}
