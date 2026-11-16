package com.bwell.sampleapp.activities.ui.healthsync

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A small, hand-picked icon lookup for metric/body-system card icons -
 * ported from Swift's HealthMetricIcon.swift. Matches on keywords in the
 * metric's display name (this demo has no LOINC-code-to-icon remote config
 * like ui-platform's real one) - approximate, not a LOINC registry port.
 * Anything unmatched falls back to a generic trending-chart icon, same
 * shape as the real app's own fallback behavior.
 */
object HealthMetricIcon {
    private val genericFallback = Icons.Filled.ShowChart

    /** Checked in order - more specific keywords before generic ones they'd otherwise also match. */
    private val keywordIcons: List<Pair<String, ImageVector>> = listOf(
        "blood pressure" to Icons.Filled.MonitorHeart,
        "heart rate" to Icons.Filled.Favorite,
        "heart" to Icons.Filled.Favorite,
        "step" to Icons.Filled.DirectionsWalk,
        "distance" to Icons.Filled.DirectionsWalk,
        "exercise" to Icons.Filled.DirectionsRun,
        "activity" to Icons.Filled.LocalFireDepartment,
        "calorie" to Icons.Filled.LocalFireDepartment,
        "energy" to Icons.Filled.LocalFireDepartment,
        "sleep" to Icons.Filled.Bedtime,
        "weight" to Icons.Filled.MonitorWeight,
        "body mass" to Icons.Filled.MonitorWeight,
        "oxygen" to Icons.Filled.Air,
        "respiratory" to Icons.Filled.Air,
        "temperature" to Icons.Filled.Thermostat,
        "glucose" to Icons.Filled.Bloodtype,
        "blood" to Icons.Filled.Bloodtype,
    )

    fun iconFor(title: String?): ImageVector {
        if (title.isNullOrEmpty()) return genericFallback
        val lowered = title.lowercase()
        return keywordIcons.firstOrNull { (keyword, _) -> lowered.contains(keyword) }?.second ?: genericFallback
    }
}
