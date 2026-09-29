package com.bwell.sampleapp.activities.ui.healthsync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthSyncProcessingTest {

    private val estimateMs = HealthSyncProcessing.PROCESSING_ESTIMATE_MS

    @Test
    fun `null data is inactive`() {
        assertEquals(ProcessingProgress.Inactive, HealthSyncProcessing.computeProgress(null, nowMs = 0))
    }

    @Test
    fun `zero total is inactive`() {
        val data = ProcessingData(total = 0, resources = emptyList(), startedAtMs = 0)
        assertEquals(ProcessingProgress.Inactive, HealthSyncProcessing.computeProgress(data, nowMs = 0))
    }

    @Test
    fun `empty resources is inactive`() {
        val data = ProcessingData(total = 10, resources = emptyList(), startedAtMs = 0)
        assertEquals(ProcessingProgress.Inactive, HealthSyncProcessing.computeProgress(data, nowMs = 0))
    }

    @Test
    fun `stale snapshot is inactive`() {
        val data = ProcessingData(total = 10, resources = listOf(ProcessingResource("Steps", 10)), startedAtMs = 0)
        val staleNow = estimateMs * HealthSyncProcessing.STALE_FACTOR + 1
        assertEquals(ProcessingProgress.Inactive, HealthSyncProcessing.computeProgress(data, nowMs = staleNow))
    }

    @Test
    fun `negative elapsed (clock skew) is inactive`() {
        val data = ProcessingData(total = 10, resources = listOf(ProcessingResource("Steps", 10)), startedAtMs = 1_000)
        assertEquals(ProcessingProgress.Inactive, HealthSyncProcessing.computeProgress(data, nowMs = 0))
    }

    @Test
    fun `single resource at start is 0 percent and processing`() {
        val data = ProcessingData(total = 10, resources = listOf(ProcessingResource("Steps", 10)), startedAtMs = 0)
        val progress = HealthSyncProcessing.computeProgress(data, nowMs = 0) as ProcessingProgress.Active
        assertEquals(0, progress.percent)
        assertEquals("Steps", progress.currentLabel)
        assertEquals(ProcessingResourceState.PROCESSING, progress.currentState)
        assertEquals(10, progress.total)
    }

    @Test
    fun `single resource at halfway elapsed is roughly 50 percent`() {
        val data = ProcessingData(total = 10, resources = listOf(ProcessingResource("Steps", 10)), startedAtMs = 0)
        val progress = HealthSyncProcessing.computeProgress(data, nowMs = estimateMs / 2) as ProcessingProgress.Active
        assertEquals(50, progress.percent)
    }

    @Test
    fun `percent never exceeds the fill cap even long after the estimate`() {
        val data = ProcessingData(total = 10, resources = listOf(ProcessingResource("Steps", 10)), startedAtMs = 0)
        val progress = HealthSyncProcessing.computeProgress(data, nowMs = estimateMs) as ProcessingProgress.Active
        assertEquals(HealthSyncProcessing.FILL_CAP_PERCENT, progress.percent)
    }

    @Test
    fun `slices are record-weighted - a resource with 3x the records gets 3x the time`() {
        val data = ProcessingData(
            total = 40,
            resources = listOf(ProcessingResource("Small", 10), ProcessingResource("Big", 30)),
            startedAtMs = 0,
        )
        val slices = HealthSyncProcessing.computeSlices(data)
        val smallDuration = slices[0].endMs - slices[0].startMs
        val bigDuration = slices[1].endMs - slices[1].startMs
        // Big has 3x Small's records, so it should get ~3x the time slice.
        assertEquals(3.0, bigDuration.toDouble() / smallDuration.toDouble(), 0.01)
    }

    @Test
    fun `second resource starts exactly where the first ends`() {
        val data = ProcessingData(
            total = 20,
            resources = listOf(ProcessingResource("A", 10), ProcessingResource("B", 10)),
            startedAtMs = 0,
        )
        val slices = HealthSyncProcessing.computeSlices(data)
        assertEquals(slices[0].endMs, slices[1].startMs)
    }

    @Test
    fun `a non-last resource shows processed just before its slice ends`() {
        val data = ProcessingData(
            total = 20,
            resources = listOf(ProcessingResource("A", 10), ProcessingResource("B", 10)),
            startedAtMs = 0,
        )
        val slices = HealthSyncProcessing.computeSlices(data)
        val justBeforeEnd = slices[0].endMs - HealthSyncProcessing.RESOURCE_DONE_BEAT_MS / 2
        val progress = HealthSyncProcessing.computeProgress(data, nowMs = justBeforeEnd) as ProcessingProgress.Active
        assertEquals("A", progress.currentLabel)
        assertEquals(ProcessingResourceState.PROCESSED, progress.currentState)
    }

    @Test
    fun `the last resource never reports processed, even near its own slice end`() {
        val data = ProcessingData(total = 10, resources = listOf(ProcessingResource("Only", 10)), startedAtMs = 0)
        val slices = HealthSyncProcessing.computeSlices(data)
        val justBeforeEnd = slices[0].endMs - 1
        val progress = HealthSyncProcessing.computeProgress(data, nowMs = justBeforeEnd) as ProcessingProgress.Active
        assertEquals(ProcessingResourceState.PROCESSING, progress.currentState)
    }

    @Test
    fun `elapsed past all slices clamps to the last slice, not an out-of-bounds index`() {
        val data = ProcessingData(
            total = 20,
            resources = listOf(ProcessingResource("A", 10), ProcessingResource("B", 10)),
            startedAtMs = 0,
        )
        // Within the fresh window but past every slice's end.
        val progress = HealthSyncProcessing.computeProgress(data, nowMs = estimateMs + 1) as ProcessingProgress.Active
        assertEquals("B", progress.currentLabel)
    }

    @Test
    fun `formatResourceLabel converts SNAKE_CASE to Title Case`() {
        assertEquals("Activity Summary", HealthSyncProcessing.formatResourceLabel("ACTIVITY_SUMMARY"))
        assertEquals("Sleep", HealthSyncProcessing.formatResourceLabel("SLEEP"))
    }

    @Test
    fun `isFreshSnapshot is true within the window and false outside it`() {
        val data = ProcessingData(total = 10, resources = listOf(ProcessingResource("A", 10)), startedAtMs = 1_000)
        assertTrue(HealthSyncProcessing.isFreshSnapshot(data, nowMs = 1_000))
        assertTrue(HealthSyncProcessing.isFreshSnapshot(data, nowMs = 1_000 + estimateMs * HealthSyncProcessing.STALE_FACTOR))
        assertEquals(false, HealthSyncProcessing.isFreshSnapshot(data, nowMs = 999))
    }
}
