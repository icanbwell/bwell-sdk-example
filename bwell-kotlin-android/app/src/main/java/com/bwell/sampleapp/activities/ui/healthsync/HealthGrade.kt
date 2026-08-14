package com.bwell.sampleapp.activities.ui.healthsync

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.South
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Shared border color for every white Health Sync card - dashboard rows, metric/body-system cards, and both detail screens. */
internal val HealthCardBorderColor = Color(0xFFE4E7EC)

/**
 * Ported from Swift's HealthGrade.swift - same A-F thresholds and color
 * intent as ui-platform's GRADE_THRESHOLDS/GRADE_COLOR_MAP, simplified to
 * Compose colors. Shared by the Body Score tab and its detail screen so
 * both grade a score identically.
 */
enum class HealthGrade(val label: String, val color: Color) {
    A("Excellent", Color(0xFF12B76A)),
    B("Good", Color(0xFF12B76A)),
    C("Fair", Color(0xFFEAAA08)),
    D("Low", Color(0xFFF79009)),
    F("Poor", Color(0xFFD92D20));

    companion object {
        // Matches ui-platform's GRADE_THRESHOLDS constant object verbatim.
        private const val THRESHOLD_A = 90.0
        private const val THRESHOLD_B = 75.0
        private const val THRESHOLD_C = 60.0
        private const val THRESHOLD_D = 45.0

        fun from(score: Double): HealthGrade = when {
            score >= THRESHOLD_A -> A
            score >= THRESHOLD_B -> B
            score >= THRESHOLD_C -> C
            score >= THRESHOLD_D -> D
            else -> F
        }
    }
}

/** "Excellent Performance" pill, colored by grade. */
@Composable
fun HealthGradeBadge(grade: HealthGrade) {
    Text(
        "${grade.label} Performance",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = grade.color,
        modifier = Modifier
            .background(grade.color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/**
 * "Poor - Fair - Excellent" gauge with a dot at the score's position -
 * simplified from ui-platform's HealthBar (drops the trend segment,
 * baseline comparison, and hover tooltips; keeps the core "where does this
 * score fall" visual).
 */
@Composable
fun HealthBarView(score: Double) {
    val position = (score.coerceIn(0.0, 100.0) / 100.0).toFloat()
    val grade = HealthGrade.from(score)
    val dotSize = 14.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Poor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Fair", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Excellent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(6.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(dotSize)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE4E7EC)),
            )
            val trackWidth = (maxWidth - dotSize).coerceAtLeast(0.dp)
            val dotOffset = (trackWidth * position).coerceIn(0.dp, trackWidth)
            Box(
                modifier = Modifier
                    .padding(start = dotOffset)
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(grade.color),
            )
        }
    }
}

/**
 * Simplified from ui-platform's TrendAlertCard - drops the minimize/dismiss
 * state and the "Ask Bailey" AI chat link (out of scope, no chat infra
 * here); keeps the trend direction + rate + comparison-date callout.
 */
@Composable
fun HealthTrendAlertCard(label: String, trendDirection: String?, trendRate: Double?, periodStart: Date?) {
    val isDeclining = trendDirection?.lowercase()?.let { it.contains("declin") || it.contains("down") } == true
    val isStable = trendDirection == null || trendDirection.lowercase().let { it.contains("stable") || it.contains("flat") }
    val tintColor = when {
        isStable -> Color(0xFF667085)
        isDeclining -> Color(0xFFD92D20)
        else -> Color(0xFF12B76A)
    }
    val titleText = when {
        isStable -> "Your $label trend is stable"
        isDeclining -> "Your $label trend is declining"
        else -> "Your $label trend is improving"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(tintColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .border(1.dp, tintColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = tintColor)
        Column {
            Text(titleText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (!isStable && trendRate != null && trendRate != 0.0) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        if (isDeclining) Icons.Filled.South else Icons.Filled.North,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        "%.0f".format(kotlin.math.abs(trendRate)),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = tintColor,
                    )
                    periodStart?.let {
                        Text(
                            "vs ${it.toDisplayDate()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = tintColor,
                        )
                    }
                }
            }
        }
    }
}

/**
 * "Score calculated on <date>" / "Based on data from <start> to <end>" -
 * shared by the Body Score tab and the body-system detail screen, which
 * both format the exact same three fields.
 */
@Composable
fun PeriodMetadataView(calculationDate: Date?, periodStart: Date?, periodEnd: Date?) {
    Column {
        calculationDate?.let {
            Text(
                "Score calculated on ${it.toDisplayDate()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (periodStart != null && periodEnd != null) {
            Text(
                "Based on data from ${periodStart.toDisplayDate()} to ${periodEnd.toDisplayDate()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** "August 13, 2026" - date only, for footers/metadata. */
fun Date.toDisplayDate(): String =
    SimpleDateFormat("MMMM d, yyyy", Locale.US).format(this)

/** "8/13/2026, 12:00 AM GMT+2" - date + time + zone, for history rows that need to disambiguate same-day records. */
fun Date.toDisplayDateTimeWithZone(): String =
    SimpleDateFormat("M/d/yyyy, h:mm a zzz", Locale.US).format(this)
