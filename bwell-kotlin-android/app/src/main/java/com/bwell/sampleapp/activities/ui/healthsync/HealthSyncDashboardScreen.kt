package com.bwell.sampleapp.activities.ui.healthsync

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.BodySystemSummary
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.HealthScore
import com.bwell.sampleapp.repository.HealthSyncRepository
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

/** Navigation for the two detail screens reachable from this dashboard - hand-rolled state instead of pulling in navigation-compose for two destinations, mirroring Swift's NavigationStack push but scoped entirely to this feature. */
private sealed interface DetailRoute {
    data class Metric(val title: String, val groupCode: String?) : DetailRoute
    data class BodySystemDetail(val bodySystemId: String, val title: String) : DetailRoute
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
    repository: HealthSyncRepository,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { dashboardViewModel.attach() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var detailRoute by remember { mutableStateOf<DetailRoute?>(null) }

    BackHandler(enabled = detailRoute != null) { detailRoute = null }

    val route = detailRoute
    if (route != null) {
        when (route) {
            is DetailRoute.Metric -> MetricDetailScreen(
                title = route.title,
                groupCode = route.groupCode,
                repository = repository,
                onBack = { detailRoute = null },
                modifier = modifier.fillMaxSize(),
            )
            is DetailRoute.BodySystemDetail -> BodySystemDetailScreen(
                bodySystemId = route.bodySystemId,
                title = route.title,
                repository = repository,
                onBack = { detailRoute = null },
                modifier = modifier.fillMaxSize(),
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TabRow(selectedTabIndex = selectedTab) {
            DashboardTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(tab.title) },
                )
            }
        }

        // weight(1f), not left unweighted - otherwise each tab body measures
        // fillMaxSize() against the Column's full height rather than what's
        // actually left below the TabRow, throwing off SyncPromptView's
        // vertical centering by roughly half the TabRow's height.
        Box(modifier = Modifier.weight(1f)) {
            when (DashboardTab.entries[selectedTab]) {
                DashboardTab.METRICS -> MetricsTab(dashboardViewModel) { title, groupCode ->
                    detailRoute = DetailRoute.Metric(title, groupCode)
                }
                DashboardTab.BODY_SCORE -> BodyScoreTab(dashboardViewModel) { bodySystemId, title ->
                    detailRoute = DetailRoute.BodySystemDetail(bodySystemId, title)
                }
                DashboardTab.PLAYGROUND -> HealthSyncPlaygroundScreen(playgroundViewModel)
            }
        }
    }
}

@Composable
private fun MetricsTab(viewModel: HealthSyncDashboardViewModel, onMetricClick: (title: String, groupCode: String?) -> Unit) {
    val state by viewModel.metricsState.collectAsStateWithLifecycle()
    when (val current = state) {
        is SyncGatedState.Loading -> CenteredProgress()
        is SyncGatedState.Empty -> SyncPromptView(progress = null, errorMessage = current.errorMessage, onSync = viewModel::syncMetrics)
        is SyncGatedState.Syncing -> SyncPromptView(progress = current.progress, errorMessage = null, onSync = viewModel::syncMetrics)
        is SyncGatedState.Loaded -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            groupMetricsByCategory(current.value).forEach { category ->
                item(key = "${category.id}-header") {
                    Text(
                        category.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(category.items.chunked(2), key = { row -> row.joinToString("-") { it.id } }) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEach { item ->
                            val group = item.group
                            val parts = formattedQuantityParts(group.value?.valueQuantity)
                            val title = group.coding?.display ?: group.name ?: "Metric"
                            MetricCardView(
                                title = title,
                                value = parts?.first,
                                unit = parts?.second,
                                footer = group.effectiveDateTime?.let { "Updated on ${it.toDisplayDate()}" },
                                onClick = { onMetricClick(title, group.coding?.code) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BodyScoreTab(viewModel: HealthSyncDashboardViewModel, onBodySystemClick: (bodySystemId: String, title: String) -> Unit) {
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Text("Health Score", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                item { BioAgeCard(score) }
                if (score.trendDirection != null) {
                    item {
                        HealthTrendAlertCard(
                            label = "health",
                            trendDirection = score.trendDirection,
                            trendRate = score.trendRate,
                            periodStart = score.periodStart,
                        )
                    }
                }
                if (bodySystems.isNotEmpty()) {
                    item {
                        Text(
                            "Body Systems",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                items(bodySystems, key = { it.bodySystemId ?: it.hashCode().toString() }) { system ->
                    // bodySystemId is an optional FHIR-sourced field - without one there's
                    // nowhere to navigate (getBodySystemScore needs a real id), so the row
                    // is shown but left non-interactive rather than firing a doomed call
                    // with an empty id.
                    val bodySystemId = system.bodySystemId
                    BodySystemRow(
                        system = system,
                        onClick = bodySystemId?.let { id -> { onBodySystemClick(id, system.title ?: "Body system") } },
                    )
                }
                item {
                    PeriodMetadataView(
                        calculationDate = score.calculationDate,
                        periodStart = score.periodStart,
                        periodEnd = score.periodEnd,
                    )
                }
            }
        }
    }
}

@Composable
private fun BioAgeCard(score: HealthScore) {
    val grade = score.overallScore?.let { HealthGrade.from(it) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, HealthCardBorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text("Biological Age", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                score.bioAge?.let { "%.0f".format(it) } ?: "—",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "years old",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        if (grade != null) {
            Spacer(Modifier.height(8.dp))
            HealthGradeBadge(grade)
        }
    }
}

@Composable
private fun BodySystemRow(system: BodySystemSummary, onClick: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, HealthCardBorderColor, RoundedCornerShape(16.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFEAECF0)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    HealthMetricIcon.iconFor(system.title),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                system.title ?: system.bodySystemId ?: "Body system",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (onClick != null) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        system.score?.let { HealthBarView(it) }
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
 * completes the wait. "Syncing with b.well…" and the gauge container appear
 * immediately on tap - only the gauge's own content morphs from an
 * indeterminate spinner to the numbered fill once real counts arrive,
 * instead of swapping to a visually distinct "reading" screen first.
 */
@Composable
private fun SyncPromptView(progress: SyncProgress?, errorMessage: String?, onSync: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (progress != null) {
                Text(
                    "Syncing with b.well…",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                val processingData = progress.processingData
                if (processingData == null) {
                    IndeterminateGauge()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Reading from your device…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
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
                            IndeterminateGauge()
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Waiting for b.well to process your data…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
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
 * Same size/shape/shadow as [ProcessingFillGauge] below, shown before
 * sync() has returned real per-type counts - keeps the sync screen's
 * layout continuous (title + circle + subtext) instead of jump-cutting to
 * a visually distinct "reading from device" screen.
 */
@Composable
private fun IndeterminateGauge() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .shadow(elevation = 10.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color.White, ProcessingGaugeBackground))),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
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
            .shadow(elevation = 10.dp, shape = CircleShape, clip = false)
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
    val parts = formattedQuantityParts(quantity) ?: return null
    return "${parts.first} ${parts.second}".trim()
}

/** Same rounding rule as [formattedQuantity], but value/unit kept separate - the Metrics grid card renders the unit smaller, below the value. */
internal fun formattedQuantityParts(quantity: Quantity?): Pair<String, String>? {
    val value = quantity?.value ?: return null
    val formatted = if (value % 1.0 == 0.0) "%.0f".format(value) else "%.1f".format(value)
    return formatted to (quantity.unit ?: "")
}
