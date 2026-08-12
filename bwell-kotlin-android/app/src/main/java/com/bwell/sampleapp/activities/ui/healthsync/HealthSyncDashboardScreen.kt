package com.bwell.sampleapp.activities.ui.healthsync

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bwell.common.models.domain.common.Quantity
import kotlinx.coroutines.delay

private enum class DashboardTab(val title: String) {
    METRICS("Metrics"),
    BODY_SCORE("Body Score"),
    PLAYGROUND("Playground"),
}

/** Shared with the "processed" checkmark tint so the two always match. */
private val ProcessingGreen = Color(0xFF12B76A)
private val ProcessingGreenLight = Color(0xFFD1FADF)
private val ProcessingGreenDark = Color(0xFF067647)
private val ProcessingGaugeBackground = Color(0xFFEEF3F1)

/** Shared look for every Metrics/Body Score data card - white on the app's tinted background, softly rounded, lightly elevated. */
private val DashboardCardShape = RoundedCornerShape(20.dp)

@Composable
private fun DashboardCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        modifier = modifier,
        shape = DashboardCardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        content = content,
    )
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
        is SyncGatedState.Loaded -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(current.value) { group ->
                DashboardCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                group.coding?.display ?: group.name ?: "Metric",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            group.sourceDisplay?.firstOrNull()?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        formattedQuantity(group.value?.valueQuantity)?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
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
            val bodySystems = score.bodySystems.orEmpty()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    DashboardCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "Biological Age",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                score.bioAge?.let { "%.0f".format(it) } ?: "—",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                if (bodySystems.isNotEmpty()) {
                    item {
                        Text(
                            "BODY SYSTEMS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
                items(bodySystems) { system ->
                    DashboardCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(system.title ?: system.bodySystemId ?: "Body system", style = MaterialTheme.typography.titleSmall)
                            system.grade?.let {
                                Text(
                                    "Grade $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
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
 * `progress` non-null means actively syncing. Ported from Swift's
 * SyncPromptView, redesigned to match ui-platform's DeviceProcessingState:
 * once sync() returns real per-type record counts, a simulated processing
 * gauge ticks (record-weighted across resource types, see
 * HealthSyncProcessing) instead of a bare "Checking (n/30)" counter - only
 * real data arrival (the poll this view doesn't own) ever actually
 * completes the wait.
 */
@Composable
private fun SyncPromptView(progress: SyncProgress?, errorMessage: String?, onSync: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (progress != null) {
                val processingData = progress.processingData
                if (processingData == null) {
                    Icon(Icons.Filled.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Syncing with b.well…",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "Reading from your device…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                } else {
                    var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
                    LaunchedEffect(processingData) {
                        while (true) {
                            tick = System.currentTimeMillis()
                            delay(500)
                        }
                    }
                    val computed = remember(processingData, tick) {
                        HealthSyncProcessing.computeProgress(processingData, tick)
                    }
                    Text(
                        "Syncing with b.well…",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(16.dp))
                    when (computed) {
                        is ProcessingProgress.Active -> {
                            ProcessingFillGauge(percent = computed.percent, total = computed.total)
                            Spacer(Modifier.height(16.dp))
                            val isDone = computed.currentState == ProcessingResourceState.PROCESSED
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${computed.currentLabel} ${if (isDone) "processed" else "processing"}${if (isDone) "" else "…"}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (isDone) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = ProcessingGreen,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                        is ProcessingProgress.Inactive -> {
                            Text(
                                "Waiting for b.well to process your data…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
            } else {
                Icon(Icons.Filled.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (errorMessage != null) "Sync incomplete" else "No data yet",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMessage?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    )
                }
                Button(onClick = onSync, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Sync with b.well")
                }
            }
        }
    }
}

/**
 * The "filling circle" gauge: a soft circle that fills bottom-up with green
 * as [percent] rises, with [total] centered - ported from ui-platform's
 * ProcessingFillGauge (the animated wave crest is simplified away; the
 * record-weighted timing logic it visualizes is the part that matters here).
 */
@Composable
private fun ProcessingFillGauge(percent: Int, total: Int) {
    val animatedFraction by animateFloatAsState(
        targetValue = percent / 100f,
        animationSpec = tween(700),
        label = "processingFill",
    )
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color.White, ProcessingGaugeBackground)))
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(current = percent.toFloat(), range = 0f..100f)
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val fillHeight = size.height * animatedFraction
            drawRect(
                brush = Brush.verticalGradient(listOf(ProcessingGreen, ProcessingGreenLight)),
                topLeft = Offset(0f, size.height - fillHeight),
                size = Size(size.width, fillHeight),
            )
        }
        Text(
            "$total",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = ProcessingGreenDark,
        )
    }
}

/** Internal, not private, so [HealthSyncDashboardScreenTest] can exercise its formatting branches directly. */
internal fun formattedQuantity(quantity: Quantity?): String? {
    val value = quantity?.value ?: return null
    val formatted = if (value % 1.0 == 0.0) "%.0f".format(value) else "%.1f".format(value)
    return "$formatted ${quantity.unit ?: ""}".trim()
}
