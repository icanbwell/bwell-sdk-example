package com.bwell.sampleapp.activities.ui.healthsync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.BodySystemComponent
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.BodySystemScore
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.HealthSyncRepository

private sealed interface BodySystemDetailState {
    data object Loading : BodySystemDetailState
    data object Empty : BodySystemDetailState
    data class Loaded(val bodySystem: BodySystemScore) : BodySystemDetailState
}

/**
 * One body system's full detail - pushed from a card in the Body Score
 * grid. Ported in spirit (not literally) from Swift's BodySystemDetailView
 * (itself ported from ui-platform's mfe-devices BodySystemDetailPage): score
 * + grade + trend, bio-age, recommendations, and contributing components.
 * Deliberately omits that page's "Ask Bailey" AI chat FAB and day-by-day
 * trend chart - both depend on infra this demo doesn't have; everything
 * shown here comes directly from BodySystemScore's own fields.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodySystemDetailScreen(
    bodySystemId: String,
    title: String,
    repository: HealthSyncRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf<BodySystemDetailState>(BodySystemDetailState.Loading) }

    LaunchedEffect(bodySystemId) {
        state = BodySystemDetailState.Loading
        val bodySystem = guardedCall("BodySystemDetail", "load", null) {
            (repository.getBodySystemScore(bodySystemId) as? BWellResult.SingleResource)?.data?.resource
        }
        state = if (bodySystem == null) BodySystemDetailState.Empty else BodySystemDetailState.Loaded(bodySystem)
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
            BodySystemDetailState.Loading -> Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            BodySystemDetailState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No data yet for this body system.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp),
                )
            }
            is BodySystemDetailState.Loaded -> Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                val bodySystem = current.bodySystem
                ScoreCard(bodySystem)
                if (bodySystem.trendDirection != null) {
                    HealthTrendAlertCard(
                        label = title.lowercase(),
                        trendDirection = bodySystem.trendDirection,
                        trendRate = bodySystem.trendRate,
                        periodStart = bodySystem.periodStart,
                    )
                }
                val recommendations = bodySystem.recommendations.orEmpty()
                if (recommendations.isNotEmpty()) RecommendationsSection(recommendations)
                val components = bodySystem.components.orEmpty()
                if (components.isNotEmpty()) ComponentsSection(components)
                PeriodMetadataView(
                    calculationDate = bodySystem.calculationDate,
                    periodStart = bodySystem.periodStart,
                    periodEnd = bodySystem.periodEnd,
                )
            }
        }
    }
}

@Composable
private fun ScoreCard(bodySystem: BodySystemScore) {
    val grade = bodySystem.score?.let { HealthGrade.from(it) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            bodySystem.score?.let { "%.0f".format(it) } ?: "—",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
        )
        if (grade != null) HealthGradeBadge(grade)
        bodySystem.score?.let {
            HealthBarView(it)
        }
        val bioAge = bodySystem.bioAge
        if (bioAge != null) {
            Divider()
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Biological Age", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.0f".format(bioAge), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                bodySystem.actualAge?.let { actualAge ->
                    val comparisonText = if (bioAge <= actualAge) {
                        "Younger than actual age (${actualAge.toInt()})"
                    } else {
                        "Older than actual age (${actualAge.toInt()})"
                    }
                    Text(comparisonText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RecommendationsSection(recommendations: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Recommendations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        recommendations.forEach { recommendation ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant),
                )
                Text(recommendation, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ComponentsSection(components: List<BodySystemComponent>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Contributing Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        components.forEach { component ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    component.title ?: component.category ?: "Metric",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                val componentValue = component.value
                val componentScore = component.score
                when {
                    componentValue != null -> Text(componentValue, style = MaterialTheme.typography.bodyMedium)
                    componentScore != null -> Text("%.0f".format(componentScore), style = MaterialTheme.typography.bodyMedium)
                }
                component.text?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
