package com.bwell.sampleapp.activities.ui.healthsync

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bwell.common.models.domain.healthdata.common.observation.Observation
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.HealthSyncRepository

private const val OWNER_SECURITY_SYSTEM = "https://www.icanbwell.com/owner"

/** No pagination UI here (see the class doc below) - fetch once, generously. */
private const val METRIC_HISTORY_PAGE_SIZE = 50

private sealed interface MetricDetailState {
    data object Loading : MetricDetailState
    data object Empty : MetricDetailState
    data class Loaded(val observations: List<Observation>) : MetricDetailState
}

/**
 * One metric type's full history - pushed from a card in the Metrics grid.
 * Ported from Swift's MetricDetailView (itself ported from ui-platform's
 * mfe-devices DeviceHistoryContainer/DeviceHistoryCard): an expandable row
 * per record, collapsed showing date+time+zone | source and the value,
 * expanded revealing Category (joined CodeableConcept display names), Sync
 * Date (the record's `issued` field - distinct from its
 * `effectiveDateTime`), and "From <source>". No pagination here - a single
 * generously-sized page is fetched once, since getDeviceMetrics has no
 * paging-info in its response to build a real "Load More" against.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricDetailScreen(
    title: String,
    groupCode: String?,
    repository: HealthSyncRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf<MetricDetailState>(MetricDetailState.Loading) }
    var expandedIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(groupCode) {
        state = MetricDetailState.Loading
        val observations = guardedCall("MetricDetail", "load", null) {
            (repository.getDeviceMetrics(groupCode, pageSize = METRIC_HISTORY_PAGE_SIZE) as? BWellResult.ResourceCollection)?.data
        }
        state = if (observations.isNullOrEmpty()) MetricDetailState.Empty else MetricDetailState.Loaded(observations)
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        when (val current = state) {
            MetricDetailState.Loading -> Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            MetricDetailState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No history yet for this metric.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp),
                )
            }
            is MetricDetailState.Loaded -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(current.observations, key = { it.id }) { observation ->
                    MetricHistoryRow(
                        observation = observation,
                        isExpanded = expandedIds.contains(observation.id),
                        onToggle = {
                            expandedIds = if (observation.id in expandedIds) {
                                expandedIds - observation.id
                            } else {
                                expandedIds + observation.id
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricHistoryRow(observation: Observation, isExpanded: Boolean, onToggle: () -> Unit) {
    val sourceText = observation.meta?.security?.firstOrNull { it.system == OWNER_SECURITY_SYSTEM }?.code
    val dateText = observation.effectiveDateTime?.toDisplayDateTimeWithZone() ?: "Unknown date"
    val valueText = formattedQuantity(observation.value?.valueQuantity)
    val categoryText = observation.category
        ?.mapNotNull { it.text ?: it.coding?.firstOrNull()?.display }
        ?.takeIf { it.isNotEmpty() }
        ?.joinToString(" | ")
    val syncDateText = observation.issued?.toDisplayDateTimeWithZone()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, HealthCardBorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(sourceText?.let { "$dateText | $it" } ?: dateText, style = MaterialTheme.typography.bodyMedium)
                valueText?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            Icon(
                if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categoryText?.let { DetailField("Category", it) }
                syncDateText?.let { DetailField("Sync Date", it) }
                sourceText?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Filled.Link,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "From $it",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
