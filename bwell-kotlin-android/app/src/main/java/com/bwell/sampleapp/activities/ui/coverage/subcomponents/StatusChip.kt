package com.bwell.sampleapp.activities.ui.coverage.subcomponents

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Status Chip Composable
 */
@Composable
fun StatusChip(status: String) {
    val backgroundColor = when (status.lowercase()) {
        "active" -> Color(0xFF4CAF50)
        "cancelled", "inactive" -> Color(0xFFF44336)
        "draft" -> Color(0xFFFF9800)
        else -> Color.Gray
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = status.uppercase(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}