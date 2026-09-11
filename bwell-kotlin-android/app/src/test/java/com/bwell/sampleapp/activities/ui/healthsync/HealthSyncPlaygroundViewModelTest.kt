package com.bwell.sampleapp.activities.ui.healthsync

import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.healthdata.healthsummary.devicemetrics.DeviceMetricsGroup
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.BodySystemSummary
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.HealthScore
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.HealthScoreResult
import com.bwell.common.models.responses.BWellResult
import com.bwell.common.models.responses.error.BWellError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HealthSyncPlaygroundViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `run does not call the repository for a blocked endpoint`() = runTest(dispatcher) {
        val repository = FakeHealthSyncRepository(configured = false)
        val viewModel = HealthSyncPlaygroundViewModel(repository)

        viewModel.run(HealthSyncPlaygroundEndpoint.CONNECT)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, repository.connectCallCount)
        assertEquals(
            PlaygroundCardState.Error(HEALTH_SYNC_NOT_CONFIGURED_MESSAGE),
            viewModel.results.value[HealthSyncPlaygroundEndpoint.CONNECT],
        )
    }

    @Test
    fun `run dispatches to the repository for a gated endpoint once configured`() = runTest(dispatcher) {
        val repository = FakeHealthSyncRepository(configured = true)
        val viewModel = HealthSyncPlaygroundViewModel(repository)

        viewModel.run(HealthSyncPlaygroundEndpoint.CONNECT)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.connectCallCount)
        assertTrue(viewModel.results.value[HealthSyncPlaygroundEndpoint.CONNECT] is PlaygroundCardState.Success)
    }

    @Test
    fun `run dispatches to the repository for a non-gated endpoint regardless of configuration`() = runTest(dispatcher) {
        val repository = FakeHealthSyncRepository(configured = false)
        val viewModel = HealthSyncPlaygroundViewModel(repository)

        viewModel.run(HealthSyncPlaygroundEndpoint.GET_DEVICE_PROVIDERS)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.getDeviceProvidersCallCount)
        assertTrue(viewModel.results.value[HealthSyncPlaygroundEndpoint.GET_DEVICE_PROVIDERS] is PlaygroundCardState.Success)
    }

    @Test
    fun `run surfaces a repository error as an Error card state`() = runTest(dispatcher) {
        val repository = FakeHealthSyncRepository(configured = false).apply {
            deviceProvidersResult = BWellResult.SingleResource(data = null, error = BWellError(Exception("boom")))
        }
        val viewModel = HealthSyncPlaygroundViewModel(repository)

        viewModel.run(HealthSyncPlaygroundEndpoint.GET_DEVICE_PROVIDERS)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.results.value[HealthSyncPlaygroundEndpoint.GET_DEVICE_PROVIDERS] is PlaygroundCardState.Error)
    }

    private fun deviceMetricsGroup(code: String, display: String?) = DeviceMetricsGroup(
        id = null,
        name = null,
        source = null,
        sourceDisplay = null,
        category = null,
        coding = Coding(code = code, display = display),
        effectiveDateTime = null,
        value = null,
        interpretation = null,
        referenceRange = null,
        component = null,
        references = null,
    )

    private fun healthScore(bodySystems: List<BodySystemSummary>) = HealthScore(
        id = null,
        overallScore = null,
        overallGrade = null,
        trendDirection = null,
        trendRate = null,
        calculationDate = null,
        periodStart = null,
        periodEnd = null,
        bodySystems = bodySystems,
        dailyScores = null,
        quality = null,
        recommendations = null,
        bioAge = null,
        actualAge = null,
        bioAgeOffset = null,
    )

    /**
     * Regression test for a real bug: after syncing via the Dashboard's
     * "Sync with b.well" button (a separate ViewModel entirely), switching
     * back to the Playground tab still showed GET_DEVICE_METRICS/
     * GET_BODY_SYSTEM_SCORE as disabled, because attach()'s one-time gate
     * meant their dropdowns never re-fetched. refreshDeviceDataOptions()
     * must re-hit the repository - and reflect new data - every time it's
     * called, unlike attach().
     */
    @Test
    fun `refreshDeviceDataOptions re-fetches and reflects newly synced data on every call`() = runTest(dispatcher) {
        val repository = FakeHealthSyncRepository(configured = true)
        val viewModel = HealthSyncPlaygroundViewModel(repository)

        viewModel.refreshDeviceDataOptions()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, repository.getDeviceMetricsGroupsCallCount)
        assertEquals(1, repository.getHealthScoreCallCount)
        assertTrue(viewModel.deviceMetricsCodeOptions.value.isEmpty())
        assertTrue(viewModel.bodySystemOptions.value.isEmpty())

        // Simulate data now existing because a sync ran via the Dashboard's
        // own ViewModel - this ViewModel has no other way to learn that.
        repository.deviceMetricsGroupsResult = BWellResult.ResourceCollection(
            data = listOf(deviceMetricsGroup(code = "steps", display = "Steps")),
            pagingInfo = null,
            error = null,
        )
        repository.healthScoreResult = BWellResult.SingleResource(
            data = HealthScoreResult(resource = healthScore(listOf(BodySystemSummary("cardio", "Cardiovascular", null, "A")))),
            error = null,
        )

        viewModel.refreshDeviceDataOptions()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, repository.getDeviceMetricsGroupsCallCount)
        assertEquals(2, repository.getHealthScoreCallCount)
        assertEquals(listOf("steps"), viewModel.deviceMetricsCodeOptions.value.map { it.code })
        assertEquals(listOf("cardio"), viewModel.bodySystemOptions.value.map { it.bodySystemId })
    }

    @Test
    fun `attach only loads device data options once, even called repeatedly`() = runTest(dispatcher) {
        val repository = FakeHealthSyncRepository(configured = true)
        val viewModel = HealthSyncPlaygroundViewModel(repository)

        viewModel.attach()
        viewModel.attach()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.getDeviceProvidersCallCount)
        assertEquals(0, repository.getDeviceMetricsGroupsCallCount)
        assertEquals(0, repository.getHealthScoreCallCount)
    }
}
