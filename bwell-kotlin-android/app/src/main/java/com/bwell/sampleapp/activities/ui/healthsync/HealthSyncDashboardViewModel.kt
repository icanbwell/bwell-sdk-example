package com.bwell.sampleapp.activities.ui.healthsync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.healthdata.healthsummary.devicemetrics.DeviceMetricsGroup
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.HealthScore
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.HealthSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Live progress while a sync-then-poll is in flight - ported from Swift's SyncProgress. */
data class SyncProgress(
    val recordsSynced: Int? = null,
    val attempt: Int = 0,
    val maxAttempts: Int,
)

/** Ported from Swift's SyncGatedState<Value>. */
sealed interface SyncGatedState<out Value> {
    data object Loading : SyncGatedState<Nothing>
    data class Empty(val errorMessage: String? = null) : SyncGatedState<Nothing>
    data class Syncing(val progress: SyncProgress) : SyncGatedState<Nothing>
    data class Loaded<Value>(val value: Value) : SyncGatedState<Value>
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

    // Reachable before login (SDK not yet initialized) - BaseSdk's accessors
    // throw synchronously in that case, not a BWellResult error, so both
    // fetchers treat that the same as "no data yet" rather than crashing.
    private suspend fun fetchMetrics(): List<DeviceMetricsGroup> = try {
        (repository.getDeviceMetricsGroups() as? BWellResult.ResourceCollection)?.data.orEmpty()
    } catch (e: Exception) {
        emptyList()
    }

    private suspend fun fetchBodyScore(): HealthScore? = try {
        (repository.getHealthScore() as? BWellResult.SingleResource)?.data?.resource
    } catch (e: Exception) {
        null
    }

    private sealed interface PollOutcome<out Value> {
        data class Found<Value>(val value: Value) : PollOutcome<Value>
        data class GaveUp(val errorMessage: String?) : PollOutcome<Nothing>
    }

    /**
     * Triggers a real sync() over the standard window, reports its actual
     * record count via [onProgress] immediately, then polls [fetch] -
     * reporting each attempt - until it returns a non-empty value or
     * [MAX_POLL_ATTEMPTS] is reached. A sync() failure is terminal (surfaced,
     * not silently swallowed) rather than polling blind against a call that
     * never ran.
     */
    private suspend fun <Value> triggerSyncThenPoll(
        fetch: suspend () -> Value,
        isEmpty: (Value) -> Boolean,
        onProgress: (SyncProgress) -> Unit,
    ): PollOutcome<Value> {
        var progress = SyncProgress(maxAttempts = MAX_POLL_ATTEMPTS)
        onProgress(progress)

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
            progress = progress.copy(recordsSynced = counts?.total ?: 0)
            onProgress(progress)
        } catch (e: Exception) {
            return PollOutcome.GaveUp("Sync failed: ${e.message}")
        }

        repeat(MAX_POLL_ATTEMPTS) { index ->
            progress = progress.copy(attempt = index + 1)
            onProgress(progress)
            val value = fetch()
            if (!isEmpty(value)) return PollOutcome.Found(value)
            kotlinx.coroutines.delay(POLL_INTERVAL_MS)
        }

        val stillEmptyMessage = if ((progress.recordsSynced ?: 0) == 0) {
            "Synced 0 records from your device - nothing to process. Check connect/requestPermissions ran first."
        } else {
            "Synced ${progress.recordsSynced} records, but b.well hasn't finished processing them yet. Try again shortly."
        }
        return PollOutcome.GaveUp(stillEmptyMessage)
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
