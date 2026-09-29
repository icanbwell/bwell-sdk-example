package com.bwell.sampleapp.activities.ui.healthsync

/**
 * Ported from ui-platform's `libs/mfe-devices/src/lib/processing` (Device
 * ProcessingState/useProcessingProgress/constants). sync() only tells us the
 * on-device record count, not real server-side processing progress - this
 * simulates it: each resource type gets a slice of a fixed estimate window
 * proportional to its record count, and the fill rises through that slice
 * as time elapses. Only real data arrival (the poll in
 * [HealthSyncDashboardViewModel]) ever actually completes the wait - the
 * timer alone is capped below 100% (see [HealthSyncProcessing.FILL_CAP_PERCENT]).
 */

/** One synced resource type and how many records were submitted for it. */
data class ProcessingResource(val label: String, val count: Int)

/** Snapshot captured when sync() completes, driving the simulated processing animation. */
data class ProcessingData(
    val total: Int,
    val resources: List<ProcessingResource>,
    val startedAtMs: Long,
)

data class ProcessingSlice(
    val label: String,
    val count: Int,
    val startMs: Long,
    val endMs: Long,
    val completedBefore: Int,
)

enum class ProcessingResourceState { PROCESSING, PROCESSED }

sealed interface ProcessingProgress {
    /** No snapshot, or the snapshot fell outside the processing window (stale). */
    data object Inactive : ProcessingProgress

    data class Active(
        val total: Int,
        /** Fill level 0-95 - capped, never completes until real data arrives. */
        val percent: Int,
        val currentLabel: String,
        val currentState: ProcessingResourceState,
    ) : ProcessingProgress
}

object HealthSyncProcessing {
    /** Processing window the animation is paced to (padded past the ~5-min average). */
    const val PROCESSING_ESTIMATE_MS = 7 * 60 * 1000L

    /**
     * The fill never reaches 100% on the timer alone - only the real poll
     * arrival completes it. Keeps the animation honest if the backend runs long.
     */
    const val FILL_CAP_PERCENT = 95

    /** How long a resource shows the "processed" beat before the next one. */
    const val RESOURCE_DONE_BEAT_MS = 1_200L

    /** Snapshots older than this multiple of the estimate are treated as stale. */
    const val STALE_FACTOR = 2

    fun isFreshSnapshot(data: ProcessingData, nowMs: Long): Boolean {
        val elapsed = nowMs - data.startedAtMs
        return elapsed in 0..(PROCESSING_ESTIMATE_MS * STALE_FACTOR)
    }

    /**
     * Per-resource time slices of the estimate, record-weighted: a resource
     * with twice the records of another gets twice the time slice.
     */
    fun computeSlices(data: ProcessingData): List<ProcessingSlice> {
        if (data.total <= 0) return emptyList()
        var cursorMs = 0.0
        var recordsBefore = 0
        return data.resources.map { resource ->
            val start = cursorMs
            cursorMs += (resource.count.toDouble() / data.total) * PROCESSING_ESTIMATE_MS
            val slice = ProcessingSlice(
                label = resource.label,
                count = resource.count,
                startMs = start.toLong(),
                endMs = cursorMs.toLong(),
                completedBefore = recordsBefore,
            )
            recordsBefore += resource.count
            slice
        }
    }

    /** Pure: maps a snapshot + a clock value to the current animation state. */
    fun computeProgress(data: ProcessingData?, nowMs: Long): ProcessingProgress {
        if (data == null || data.total <= 0 || data.resources.isEmpty()) return ProcessingProgress.Inactive
        if (!isFreshSnapshot(data, nowMs)) return ProcessingProgress.Inactive

        val slices = computeSlices(data)
        val elapsed = nowMs - data.startedAtMs
        val index = slices.indexOfFirst { elapsed < it.endMs }.let { if (it == -1) slices.lastIndex else it }
        val slice = slices[index]
        val isLast = index == slices.lastIndex

        val sliceDuration = (slice.endMs - slice.startMs).coerceAtLeast(1L)
        val sliceProgress = ((elapsed - slice.startMs).toDouble() / sliceDuration).coerceIn(0.0, 1.0)
        val processedRecords = slice.completedBefore + sliceProgress * slice.count

        // A non-last resource shows "processed" for a short beat before the
        // next one; the last resource holds at "processing" until real data
        // arrives (there's nothing after it to advance to).
        val inDoneBeat = !isLast && (slice.endMs - elapsed) <= RESOURCE_DONE_BEAT_MS

        return ProcessingProgress.Active(
            total = data.total,
            percent = ((processedRecords / data.total) * 100).toInt().coerceIn(0, FILL_CAP_PERCENT),
            currentLabel = slice.label,
            currentState = if (inDoneBeat) ProcessingResourceState.PROCESSED else ProcessingResourceState.PROCESSING,
        )
    }

    /** "ACTIVITY_SUMMARY" -> "Activity Summary" - only formatting done on the raw HealthDataType name. */
    fun formatResourceLabel(rawName: String): String =
        rawName.split("_").joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercase) }
}
