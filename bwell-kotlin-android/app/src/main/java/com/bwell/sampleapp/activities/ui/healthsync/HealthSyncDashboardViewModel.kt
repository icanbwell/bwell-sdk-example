package com.bwell.sampleapp.activities.ui.healthsync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.healthdata.healthsummary.devicemetrics.DeviceMetricsGroup
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.HealthScore
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthsync.model.SyncCounts
import com.bwell.sampleapp.repository.HealthSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Live progress while a sync-then-poll is in flight. `processingData` is
 * null only in the brief window before sync() itself has returned (we don't
 * know per-type record counts yet) - once set, the UI ticks its own
 * simulated-processing clock against it (see HealthSyncProcessing) rather
 * than this class carrying a live percent itself.
 */
data class SyncProgress(val processingData: ProcessingData? = null)

/** Ported from Swift's SyncGatedState<Value>. */
sealed interface SyncGatedState<out Value> {
    data object Loading : SyncGatedState<Nothing>
    data class Empty(val errorMessage: String? = null) : SyncGatedState<Nothing>
    data class Syncing(val progress: SyncProgress) : SyncGatedState<Nothing>
    data class Loaded<Value>(val value: Value) : SyncGatedState<Value>
}

/**
 * One card's worth of grid data - [DeviceMetricsGroup.id] is optional (a
 * server-provided FHIR id, not guaranteed), so identity is index-qualified
 * rather than relying on it directly.
 */
data class MetricGridItem(val id: String, val group: DeviceMetricsGroup)

/** One category section in the Metrics grid. */
data class MetricCategoryGroup(val id: String, val label: String, val items: List<MetricGridItem>)

private const val DISPLAY_GROUP_SYSTEM = "https://www.icanbwell.com/display-group"

/**
 * Groups device-metrics cards by their FHIR `category` coding whose system
 * is b.well's display-group system - ported from Swift's
 * groupMetricsByCategory (itself ported from ui-platform's mfe-devices
 * groupMetricsByDisplayGroup). Anything with no matching coding falls into
 * a single "Other" bucket rather than being dropped, mirroring that same
 * fallback.
 */
fun groupMetricsByCategory(groups: List<DeviceMetricsGroup>): List<MetricCategoryGroup> {
    val order = mutableListOf<String>()
    val labels = mutableMapOf<String, String>()
    val items = mutableMapOf<String, MutableList<MetricGridItem>>()

    groups.forEachIndexed { index, group ->
        val coding = group.category
            ?.flatMap { it.coding.orEmpty() }
            ?.firstOrNull { it.system == DISPLAY_GROUP_SYSTEM }
        val code = coding?.code ?: "other"
        val item = MetricGridItem(id = "$code-$index", group = group)

        val existing = items[code]
        if (existing != null) {
            existing.add(item)
        } else {
            order.add(code)
            labels[code] = coding?.display ?: "Other"
            items[code] = mutableListOf(item)
        }
    }

    return order.map { code -> MetricCategoryGroup(id = code, label = labels[code] ?: "Other", items = items[code].orEmpty()) }
}

/**
 * Backs [HealthSyncDashboardScreen] (the Metrics/Body Score tabs shown when
 * an on-device adapter is configured) - ported from Swift's
 * HealthSyncDashboardViewModel. Shares the same "fetch, and if empty let the
 * user trigger sync() then poll until data shows up" shape for both tabs,
 * factored into [triggerSyncThenPoll] instead of duplicating it.
 *
 * DCON-4936 (session-conflict-on-user-switch) is not yet in a Kotlin SDK
 * release (bwell-sdk-kotlin 1.20.0 predates PR #955) - [reconcileSessionIfNeeded]
 * is the same TEMPORARY, public-API-only workaround Swift already has in
 * HealthSyncSessionReconciliation.swift. Delete both once #955 ships.
 */
class HealthSyncDashboardViewModel(private val repository: HealthSyncRepository) : ViewModel() {

    private companion object {
        // Server-side processing after sync() isn't instant (matches the Read
        // Data note elsewhere: "Data appears within 5 min") - poll instead of
        // a single re-fetch, but give up rather than polling forever.
        const val POLL_INTERVAL_MS = 10_000L
        const val MAX_POLL_ATTEMPTS = 30

        const val TAG = "HealthSyncDashboard"
    }

    private val _metricsState = MutableStateFlow<SyncGatedState<List<DeviceMetricsGroup>>>(SyncGatedState.Loading)
    val metricsState: StateFlow<SyncGatedState<List<DeviceMetricsGroup>>> = _metricsState.asStateFlow()

    private val _bodyScoreState = MutableStateFlow<SyncGatedState<HealthScore>>(SyncGatedState.Loading)
    val bodyScoreState: StateFlow<SyncGatedState<HealthScore>> = _bodyScoreState.asStateFlow()

    private var attached = false

    fun attach() {
        if (attached) return
        attached = true
        viewModelScope.launch { refreshMetrics() }
        viewModelScope.launch { refreshBodyScore() }
    }

    suspend fun refreshMetrics() {
        val groups = fetchMetrics()
        _metricsState.value = if (groups.isEmpty()) SyncGatedState.Empty() else SyncGatedState.Loaded(groups)
    }

    suspend fun refreshBodyScore() {
        val score = fetchBodyScore()
        _bodyScoreState.value = if (score == null) SyncGatedState.Empty() else SyncGatedState.Loaded(score)
    }

    fun syncMetrics() {
        viewModelScope.launch {
            when (val outcome = triggerSyncThenPoll(fetch = ::fetchMetrics, isEmpty = { it.isEmpty() }) {
                _metricsState.value = SyncGatedState.Syncing(it)
            }) {
                is PollOutcome.Found -> _metricsState.value = SyncGatedState.Loaded(outcome.value)
                is PollOutcome.GaveUp -> _metricsState.value = SyncGatedState.Empty(outcome.errorMessage)
            }
        }
    }

    fun syncBodyScore() {
        viewModelScope.launch {
            when (
                val outcome = triggerSyncThenPoll(fetch = ::fetchBodyScore, isEmpty = { it == null }) {
                    _bodyScoreState.value = SyncGatedState.Syncing(it)
                }
            ) {
                is PollOutcome.Found -> outcome.value?.let { _bodyScoreState.value = SyncGatedState.Loaded(it) }
                is PollOutcome.GaveUp -> _bodyScoreState.value = SyncGatedState.Empty(outcome.errorMessage)
            }
        }
    }

    // Reachable before login - see guardedCall's doc. Both fetchers treat a
    // throw the same as "no data yet" rather than crashing.
    private suspend fun fetchMetrics(): List<DeviceMetricsGroup> =
        guardedCall(TAG, "fetchMetrics", emptyList()) {
            (repository.getDeviceMetricsGroups() as? BWellResult.ResourceCollection)?.data.orEmpty()
        }

    private suspend fun fetchBodyScore(): HealthScore? =
        guardedCall(TAG, "fetchBodyScore", null) {
            (repository.getHealthScore() as? BWellResult.SingleResource)?.data?.resource
        }

    private sealed interface PollOutcome<out Value> {
        data class Found<Value>(val value: Value) : PollOutcome<Value>
        data class GaveUp(val errorMessage: String?) : PollOutcome<Nothing>
    }

    /**
     * Triggers a real sync() over the standard window, captures its actual
     * per-type record counts as [ProcessingData] and reports it via
     * [onProgress] immediately, then polls [fetch] - real completion
     * detection, independent of the simulated processing clock the UI ticks
     * against - until it returns a non-empty value or [MAX_POLL_ATTEMPTS] is
     * reached. A sync() failure is terminal (surfaced, not silently
     * swallowed) rather than polling blind against a call that never ran.
     */
    private suspend fun <Value> triggerSyncThenPoll(
        fetch: suspend () -> Value,
        isEmpty: (Value) -> Boolean,
        onProgress: (SyncProgress) -> Unit,
    ): PollOutcome<Value> {
        onProgress(SyncProgress())
        var recordsSynced = 0

        try {
            reconcileSessionIfNeeded()
            val connectResult = repository.connect()
            if (!connectResult.success()) {
                return PollOutcome.GaveUp("Sync failed: ${connectResult.error?.message()}")
            }
            val permissionsResult = repository.requestPermissions()
            if (!permissionsResult.success()) {
                return PollOutcome.GaveUp("Sync failed: ${permissionsResult.error?.message()}")
            }
            val syncResult = repository.sync()
            if (!syncResult.success()) {
                return PollOutcome.GaveUp("Sync failed: ${syncResult.error?.message()}")
            }
            val counts = (syncResult as? BWellResult.SingleResource)?.data
            recordsSynced = counts?.total ?: 0
            onProgress(SyncProgress(processingData = counts?.toProcessingData()))
        } catch (e: Exception) {
            return PollOutcome.GaveUp("Sync failed: ${e.message}")
        }

        repeat(MAX_POLL_ATTEMPTS) { index ->
            val value = fetch()
            if (!isEmpty(value)) return PollOutcome.Found(value)
            // Skip the pacing delay after the last attempt - there's no next
            // attempt to pace towards, so it was just a dead 10s wait
            // immediately before the give-up message.
            if (index < MAX_POLL_ATTEMPTS - 1) kotlinx.coroutines.delay(POLL_INTERVAL_MS)
        }

        val stillEmptyMessage = if (recordsSynced == 0) {
            "Synced 0 records from your device - nothing to process. Check connect/requestPermissions ran first."
        } else {
            "Synced $recordsSynced records, but b.well hasn't finished processing them yet. Try again shortly."
        }
        return PollOutcome.GaveUp(stillEmptyMessage)
    }

    /** Only resource types with records synced feed the processing animation. */
    private fun SyncCounts.toProcessingData(): ProcessingData? {
        val resources = perType
            .filter { (_, count) -> count > 0 }
            .map { (type, count) -> ProcessingResource(HealthSyncProcessing.formatResourceLabel(type.name), count) }
        if (resources.isEmpty()) return null
        return ProcessingData(total = resources.sumOf { it.count }, resources = resources, startedAtMs = System.currentTimeMillis())
    }

    /**
     * Public-API-only reconciliation, matching what bwell-sdk PR #955 does
     * inside the SDK itself: currentUser() (the live on-device session's
     * user, or null on a fresh install/device) vs getDeviceUserStatus()
     * (whether this logged-in b.well user already has a device-user record,
     * and if so, which one). Ends the stale session first if they disagree.
     */
    private suspend fun reconcileSessionIfNeeded() {
        val currentUserResult = repository.currentUser()
        val currentUser = (currentUserResult as? BWellResult.SingleResource)?.data ?: return

        val statusResult = repository.getDeviceUserStatus(HealthSyncPlaygroundViewModel.PLAYGROUND_CONNECTION_ID)
        val status = (statusResult as? BWellResult.SingleResource)?.data ?: return
        val matches = status.exists && status.deviceUserId == currentUser.userId
        if (!matches) {
            repository.endSession()
        }
    }
}
