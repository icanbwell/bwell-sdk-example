package com.bwell.sampleapp.activities.ui.healthsync

import com.bwell.common.models.domain.data.DeviceProviderList
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.HealthSyncRepository

/**
 * Test double for [HealthSyncRepository] - records call counts so tests can
 * assert a gated endpoint never reaches the repository, and returns canned
 * results so dispatch/error-mapping can be tested without a real [BWellSdk].
 */
class FakeHealthSyncRepository(
    private var configured: Boolean = false,
) : HealthSyncRepository() {

    var connectCallCount = 0
    var connectResult: BWellResult<Unit> = BWellResult.SingleResource(data = Unit, error = null)

    var getDeviceProvidersCallCount = 0
    var deviceProvidersResult: BWellResult<DeviceProviderList> =
        BWellResult.SingleResource(data = DeviceProviderList(providers = emptyList()), error = null)

    override fun isHealthSyncConfigured(): Boolean = configured

    override suspend fun connect(): BWellResult<Unit> {
        connectCallCount++
        return connectResult
    }

    override suspend fun getDeviceProviders(): BWellResult<DeviceProviderList> {
        getDeviceProvidersCallCount++
        return deviceProvidersResult
    }
}
