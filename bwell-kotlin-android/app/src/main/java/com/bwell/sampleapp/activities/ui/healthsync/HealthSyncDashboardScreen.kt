package com.bwell.sampleapp.activities.ui.healthsync

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class DashboardTab(val title: String) {
    METRICS("Metrics"),
    BODY_SCORE("Body Score"),
    PLAYGROUND("Playground"),
}

/**
 * Shown instead of the raw Playground when an on-device adapter is
 * configured - ported from Swift's HealthSyncDashboardView. A polished
 * Metrics/Body Score view for the data that adapter actually produces, plus
 * the Playground itself as a third tab for anyone who still wants the raw
 * test bench.
 */
@Composable
fun HealthSyncDashboardScreen(
    dashboardViewModel: HealthSyncDashboardViewModel,
    playgroundViewModel: HealthSyncPlaygroundViewModel,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { dashboardViewModel.attach() }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            DashboardTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(tab.title) },
                )
            }
        }

        when (DashboardTab.entries[selectedTab]) {
            DashboardTab.METRICS -> MetricsTab(dashboardViewModel)
            DashboardTab.BODY_SCORE -> BodyScoreTab(dashboardViewModel)
            DashboardTab.PLAYGROUND -> HealthSyncPlaygroundScreen(playgroundViewModel)
        }
    }
}

@Composable
private fun MetricsTab(viewModel: HealthSyncDashboardViewModel) {
    val state by viewModel.metricsState.collectAsStateWithLifecycle()
    when (val current = state) {
        is SyncGatedState.Loading -> CenteredProgress()
        is SyncGatedState.Empty -> SyncPromptView(progress = null, errorMessage = current.errorMessage, onSync = viewModel::syncMetrics)
        is SyncGatedState.Syncing -> SyncPromptView(progress = current.progress, errorMessage = null, onSync = viewModel::syncMetrics)
        is SyncGatedState.Loaded -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(current.value) { group ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            group.coding?.display ?: group.name ?: "Metric",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        group.sourceDisplay?.firstOrNull()?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BodyScoreTab(viewModel: HealthSyncDashboardViewModel) {
    val state by viewModel.bodyScoreState.collectAsStateWithLifecycle()
    when (val current = state) {
        is SyncGatedState.Loading -> CenteredProgress()
        is SyncGatedState.Empty -> SyncPromptView(progress = null, errorMessage = current.errorMessage, onSync = viewModel::syncBodyScore)
        is SyncGatedState.Syncing -> SyncPromptView(progress = current.progress, errorMessage = null, onSync = viewModel::syncBodyScore)
        is SyncGatedState.Loaded -> {
            val score = current.value
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Biological Age", style = MaterialTheme.typography.bodySmall)
                            Text(
                                score.bioAge?.let { "%.0f".format(it) } ?: "—",
                                style = MaterialTheme.typography.displaySmall,
                            )
                        }
                    }
                }
                items(score.bodySystems.orEmpty()) { system ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(system.title ?: system.bodySystemId ?: "Body system")
                            system.grade?.let { Text("Grade $it", style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredProgress() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

/**
 * `progress` non-null means actively syncing - shows the real record count
 * sync() returned, plus a numeric attempt counter, not just a spinner with
 * no way to tell if it's stuck. Ported from Swift's SyncPromptView.
 */
@Composable
private fun SyncPromptView(progress: SyncProgress?, errorMessage: String?, onSync: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Filled.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)

            if (progress != null) {
                Text("Syncing with b.well…", style = MaterialTheme.typography.titleMedium)
                val recordsSynced = progress.recordsSynced
                if (recordsSynced != null) {
                    Text(
                        "Synced $recordsSynced record${if (recordsSynced == 1) "" else "s"} from your device. " +
                            "Waiting for b.well to process ${if (recordsSynced == 0) "" else "them"}…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        "Reading from your device…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (progress.attempt > 0) {
                    LinearProgressIndicator(
                        progress = progress.attempt.toFloat() / progress.maxAttempts,
                        modifier = Modifier.width(200.dp).padding(top = 12.dp),
                    )
                    Text(
                        "Checking (${progress.attempt}/${progress.maxAttempts})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                }
            } else {
                Text(
                    if (errorMessage != null) "Sync incomplete" else "No data yet",
                    style = MaterialTheme.typography.titleMedium,
                )
                errorMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onSync, modifier = Modifier.padding(top = 12.dp)) {
                    Text("Sync with b.well")
                }
            }
        }
    }
}
