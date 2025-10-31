package com.bwell.sampleapp.activities.ui.coverage.subcomponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.bwell.common.models.domain.financials.coverage.Coverage
import com.google.gson.GsonBuilder

/**
 * Detailed Coverage Information Composable
 */
@Composable
fun CoverageDetails(coverage: Coverage) {
    Column {
        // Basic Information
        DetailItem("ID", coverage.id)
        DetailItem("Status", coverage.status.toString() )
        DetailItem("Subscriber ID", coverage.subscriberId ?: "N/A")
        DetailItem("Resource Type", coverage.resourceType ?: "N/A")
        
        // Type Information (if simple)
        coverage.type?.text?.let { typeText ->
            DetailItem("Type", typeText)
        }
        
        // Nested Objects as JSON
        coverage.type?.let { type ->
            if (type.text.isNullOrEmpty() && !type.coding.isNullOrEmpty()) {
                JsonDetailSection("Type", type)
            }
        }
        
        coverage.beneficiary?.let { beneficiary ->
            JsonDetailSection("Beneficiary", beneficiary)
        }
        
        coverage.policyHolder?.let { policyHolder ->
            JsonDetailSection("Policy Holder", policyHolder)
        }
        
        coverage.relationship?.let { relationship ->
            JsonDetailSection("Relationship", relationship)
        }
        
        coverage.period?.let { period ->
            JsonDetailSection("Coverage Period", period)
        }
        
        if (!coverage.payor.isNullOrEmpty()) {
            JsonDetailSection("Payors", coverage.payor)
        }
        
        if (!coverage.`class`.isNullOrEmpty()) {
            JsonDetailSection("Coverage Classes", coverage.`class`)
        }
        
        if (!coverage.identifier.isNullOrEmpty()) {
            JsonDetailSection("Identifiers", coverage.identifier)
        }
        
        coverage.meta?.let { meta ->
            JsonDetailSection("Meta", meta)
        }
    }
}

/**
 * Helper composable for displaying nested objects as formatted JSON
 */
@Composable
fun JsonDetailSection(title: String, data: Any?) {
    if (data == null) return
    
    var isExpanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        // Header with expand/collapse
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // JSON Content (when expanded)
        if (isExpanded) {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = try {
                gson.toJson(data)
            } catch (e: Exception) {
                "Error serializing data: ${e.message}"
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Text(
                    text = jsonString,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState())
                )
            }
        }
    }
}

/**
 * Helper composable for individual detail items
 */
@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}