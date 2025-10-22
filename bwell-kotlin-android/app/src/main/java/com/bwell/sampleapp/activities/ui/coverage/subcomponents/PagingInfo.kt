package com.bwell.sampleapp.activities.ui.coverage.subcomponents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.GsonBuilder

/**
 * Paging Information Composable
 */
@Composable
fun PagingInfo(
    currentPage: Int,
    pageSize: Int,
    totalItems: Int,
    totalPages: Int = 0,
    modifier: Modifier = Modifier
) {
    val startItem = if (totalItems > 0) (currentPage * pageSize) + 1 else 0
    val endItem = minOf((currentPage + 1) * pageSize, totalItems)
    var isExpanded by remember { mutableStateOf(false) }
    
    // Create paging data object for JSON display
    val pagingData = mapOf(
        "pageNumber" to currentPage,
        "pageSize" to pageSize,
        "totalItems" to totalItems,
        "totalPages" to totalPages
    )
    
    val gson = GsonBuilder().setPrettyPrinting().create()
    val jsonString = gson.toJson(pagingData)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with expand/collapse button
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current page info
                Column {
                    Text(
                        text = "Page ${currentPage + 1}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Showing $startItem-$endItem of $totalItems total",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                // Expand/Collapse button
                IconButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse paging details" else "Expand paging details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // JSON details (expandable)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Paging Details (JSON)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = jsonString,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}