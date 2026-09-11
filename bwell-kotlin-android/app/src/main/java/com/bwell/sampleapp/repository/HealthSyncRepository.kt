package com.bwell.sampleapp.repository

import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.data.DeviceProviderList
import com.bwell.common.models.domain.data.DeviceProviderStatus
import com.bwell.common.models.domain.data.DeviceUserStatus
import com.bwell.common.models.domain.data.MobileSyncCredentials
import com.bwell.common.models.domain.healthdata.common.observation.Observation
import com.bwell.common.models.domain.healthdata.healthsummary.devicemetrics.DeviceMetricsGroup
import com.bwell.common.models.responses.BWellResult
import com.bwell.common.models.responses.OperationOutcome
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.BodySystemScoreResult
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.HealthScoreResult
import com.bwell.healthdata.healthsummary.requests.devicemetrics.DeviceMetricsGroupsRequest
import com.bwell.healthdata.healthsummary.requests.devicemetrics.DeviceMetricsQueryRequest
import com.bwell.healthsync.BWellHealthSync
import com.bwell.healthsync.healthSync
import com.bwell.healthsync.model.DateRange
import com.bwell.healthsync.model.HealthDataType
import com.bwell.healthsync.model.HealthSyncUser
import com.bwell.healthsync.model.SyncCounts
import com.bwell.sampleapp.singletons.BWellSdk
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Wraps every b.well SDK call the Health Sync Playground/Dashboard uses -
 * matches the app's existing per-feature repository convention (see
 * InsuranceRepository). Deliberately thin: each method is a single SDK call,
 * no business logic, so the ViewModel stays the only place gating/state
 * decisions happen.
 *
 * The Mobile Sync methods here (connect/disconnect/requestPermissions/sync/
 * currentUser) all go through [com.bwell.healthsync.HealthSyncManager],
 * which throws IllegalStateException if
 * [com.bwell.healthsync.BWellHealthSync.isConfigured] is false - callers
 * must check that first (see HealthSyncGating).
 *
 * Open (and every member open) so tests can subclass with a fake instead of
 * touching the real static [BWellSdk] singleton - no DI framework exists in
 * this app to inject an interface instead.
 */
open class HealthSyncRepository {

    // 30-day window - matches Swift's Playground `sync` card and Dashboard's
    // "Sync with b.well" action.
    private val syncWindowDays = 30L

    open suspend fun connect(): BWellResult<Unit> = BWellSdk.healthSync.connect()

    open suspend fun disconnect(): BWellResult<Unit> = BWellSdk.healthSync.disconnect()

    open suspend fun requestPermissions(): BWellResult<Unit> =
        BWellSdk.healthSync.requestPermissions(HealthDataType.entries.toSet())

    open suspend fun sync(): BWellResult<SyncCounts> {
        val now = Instant.now()
        val window = DateRange(from = now.minus(syncWindowDays, ChronoUnit.DAYS), to = now)
        return BWellSdk.healthSync.sync(HealthDataType.entries.toSet(), window)
    }

    open suspend fun currentUser(): BWellResult<HealthSyncUser?> = BWellSdk.healthSync.currentUser()

    open suspend fun endSession(): BWellResult<Unit> = BWellSdk.healthSync.endSession()

    open suspend fun setupMobileSync(connectionId: String): BWellResult<MobileSyncCredentials> =
        BWellSdk.device.setupMobileSync(connectionId)

    open suspend fun getDeviceUserStatus(connectionId: String): BWellResult<DeviceUserStatus> =
        BWellSdk.device.getDeviceUserStatus(connectionId)

    open suspend fun getOauthUrl(providerSlug: String): BWellResult<String> =
        BWellSdk.connections.getOauthUrl(providerSlug)

    open suspend fun getDeviceProviderStatus(connectionId: String): BWellResult<DeviceProviderStatus> =
        BWellSdk.device.getDeviceProviderStatus(connectionId)

    open suspend fun getDeviceProviders(): BWellResult<DeviceProviderList> =
        BWellSdk.device.getDeviceProviders()

    open suspend fun deleteConnection(connectionId: String): OperationOutcome =
        BWellSdk.connections.deleteConnection(connectionId)

    // pageSize defaults to the SDK's own default (20) so the existing
    // Playground call site is unaffected - the Metric Detail screen passes
    // a larger one explicitly (see its ViewModel) since it has no
    // pagination UI of its own.
    open suspend fun getDeviceMetrics(groupCode: String?, pageSize: Int = 20): BWellResult<Observation> =
        BWellSdk.health.getDeviceMetrics(
            DeviceMetricsQueryRequest.Builder()
                .apply { groupCode?.let { groupCode(listOf(Coding(code = it))) } }
                .pageSize(pageSize)
                .build(),
        )

    open suspend fun getDeviceMetricsGroups(): BWellResult<DeviceMetricsGroup> =
        BWellSdk.health.getDeviceMetricsGroups(DeviceMetricsGroupsRequest.Builder().build())

    open suspend fun getHealthScore(): BWellResult<HealthScoreResult> = BWellSdk.health.getHealthScore()

    open suspend fun getBodySystemScore(bodySystemId: String): BWellResult<BodySystemScoreResult> =
        BWellSdk.health.getBodySystemScore(bodySystemId)

    open fun isHealthSyncConfigured(): Boolean = BWellHealthSync.isConfigured
}
