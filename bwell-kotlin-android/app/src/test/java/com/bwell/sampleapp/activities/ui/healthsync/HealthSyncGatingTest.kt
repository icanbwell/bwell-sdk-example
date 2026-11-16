package com.bwell.sampleapp.activities.ui.healthsync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthSyncGatingTest {

    private val gatedEndpoints = setOf(
        HealthSyncPlaygroundEndpoint.CONNECT,
        HealthSyncPlaygroundEndpoint.DISCONNECT,
        HealthSyncPlaygroundEndpoint.REQUEST_PERMISSIONS,
        HealthSyncPlaygroundEndpoint.SYNC,
        HealthSyncPlaygroundEndpoint.GET_CURRENT_USER,
    )

    @Test
    fun `gated endpoints are blocked when not configured`() {
        gatedEndpoints.forEach { endpoint ->
            assertTrue(
                "$endpoint should be blocked when not configured",
                HealthSyncGating.isBlocked(endpoint, configured = false),
            )
        }
    }

    @Test
    fun `gated endpoints are not blocked when configured`() {
        gatedEndpoints.forEach { endpoint ->
            assertFalse(
                "$endpoint should not be blocked when configured",
                HealthSyncGating.isBlocked(endpoint, configured = true),
            )
        }
    }

    @Test
    fun `non-gated endpoints are never blocked, configured or not`() {
        val nonGated = HealthSyncPlaygroundEndpoint.entries.toSet() - gatedEndpoints
        nonGated.forEach { endpoint ->
            assertFalse(HealthSyncGating.isBlocked(endpoint, configured = false))
            assertFalse(HealthSyncGating.isBlocked(endpoint, configured = true))
        }
    }

    @Test
    fun `blockedReason returns the locked copy exactly when blocked`() {
        assertEquals(
            HEALTH_SYNC_NOT_CONFIGURED_MESSAGE,
            HealthSyncGating.blockedReason(HealthSyncPlaygroundEndpoint.SYNC, configured = false),
        )
    }

    @Test
    fun `blockedReason is null when not blocked`() {
        assertNull(HealthSyncGating.blockedReason(HealthSyncPlaygroundEndpoint.SYNC, configured = true))
        assertNull(HealthSyncGating.blockedReason(HealthSyncPlaygroundEndpoint.GET_DEVICE_PROVIDERS, configured = false))
    }

    @Test
    fun `exactly the 5 healthSync-backed Mobile Sync endpoints are gated`() {
        val actuallyGated = HealthSyncPlaygroundEndpoint.entries
            .filter { HealthSyncGating.isBlocked(it, configured = false) }
            .toSet()
        assertEquals(gatedEndpoints, actuallyGated)
    }
}
