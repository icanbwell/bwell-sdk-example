package com.bwell.sampleapp.activities.ui.healthsync

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Single entry point for the Health Sync feature - ported from Swift's
 * HealthSyncPlaygroundRootView. Branches on
 * [com.bwell.healthsync.BWellHealthSync.isConfigured] (read eagerly at
 * [HealthSyncPlaygroundViewModel] construction, not lazily, so this decision
 * never blocks on attach()): the Dashboard when an on-device adapter is
 * configured, the raw Playground directly otherwise.
 */
@Composable
fun HealthSyncRootScreen(
    playgroundViewModel: HealthSyncPlaygroundViewModel,
    dashboardViewModel: HealthSyncDashboardViewModel,
    modifier: Modifier = Modifier,
) {
    val configured by playgroundViewModel.configured.collectAsStateWithLifecycle()
    if (configured) {
        HealthSyncDashboardScreen(dashboardViewModel, playgroundViewModel, modifier.fillMaxSize())
    } else {
        HealthSyncPlaygroundScreen(playgroundViewModel, modifier.fillMaxSize())
    }
}
