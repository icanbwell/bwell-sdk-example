package com.bwell.sampleapp.activities.ui.healthsync

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Shared card shape for the Metrics grid - icon, title, big value + unit,
 * and an "Updated on <date>" footer. Ported from Swift's MetricCardView
 * (itself ported from ui-platform's mfe-devices DeviceVitalsCard),
 * simplified to this demo's needs. Uses Compose's standard ripple
 * (via [clickable]) for press feedback rather than porting iOS's
 * scale+dim animation verbatim - the platform-idiomatic touch feedback for
 * the same underlying intent (visible pressed state).
 */
@Composable
fun MetricCardView(
    title: String,
    value: String?,
    unit: String?,
    footer: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .shadow(elevation = 4.dp, shape = shape, clip = false)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE4E7EC), shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            HealthMetricIcon.iconFor(title),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
        )
        if (value != null) {
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (!unit.isNullOrEmpty()) {
                    Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (footer != null) {
            Divider()
            Text(footer, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
