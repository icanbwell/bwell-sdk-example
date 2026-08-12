package com.bwell.sampleapp.activities.ui.healthsync

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
}
