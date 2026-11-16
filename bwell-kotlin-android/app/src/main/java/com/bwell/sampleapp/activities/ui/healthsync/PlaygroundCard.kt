package com.bwell.sampleapp.activities.ui.healthsync

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * One endpoint's card - ported from Swift's PlaygroundCard.swift: title +
 * optional info button/sheet, Run button (or "Blocked" when [isBlocked],
 * disabled either way), copy-to-clipboard on a successful result, and
 * [content] as a slot for endpoint-specific inputs (provider/code/body
 * system pickers).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaygroundCard(
    endpoint: HealthSyncPlaygroundEndpoint,
    state: PlaygroundCardState,
    isBlocked: Boolean,
    blockedReason: String?,
    runDisabled: Boolean,
    onRun: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    var showInfoSheet by remember { mutableStateOf(false) }
    val hasInfo = endpoint.documentationUrl != null || endpoint.parameterInfo != null

    Card(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    endpoint.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (hasInfo) {
                    IconButton(onClick = { showInfoSheet = true }) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "More info about ${endpoint.title}",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                if (state is PlaygroundCardState.Success) {
                    CopyButton(text = state.raw)
                }
                when (state) {
                    is PlaygroundCardState.Loading ->
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else ->
                        Button(onClick = onRun, enabled = !isBlocked && !runDisabled) {
                            Text(if (isBlocked) "Blocked" else "Run")
                        }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                endpoint.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (blockedReason != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    blockedReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            content()

            when (state) {
                is PlaygroundCardState.Success -> {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = Color.White,
                        contentColor = Color.Black,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            state.raw,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(8.dp),
                        )
                    }
                }
                is PlaygroundCardState.Error -> {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                else -> Unit
            }
        }
    }

    if (showInfoSheet) {
        val sheetState = rememberModalBottomSheetState()
        val context = LocalContext.current
        ModalBottomSheet(onDismissRequest = { showInfoSheet = false }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(endpoint.title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text(
                    endpoint.documentationSummary ?: endpoint.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                )
                endpoint.parameterInfo?.let { info ->
                    Spacer(Modifier.height(16.dp))
                    Text("Parameters", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(info, style = MaterialTheme.typography.bodySmall)
                }
                endpoint.documentationUrl?.let { url ->
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("More info on the doc page")
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/** Swaps to a checkmark for 1.2s after copying - mirrors Swift's PlaygroundCard copy affordance. */
@Composable
private fun CopyButton(text: String) {
    val clipboard = LocalClipboardManager.current
    var justCopied by remember { mutableStateOf(false) }

    LaunchedEffect(justCopied) {
        if (justCopied) {
            delay(1200)
            justCopied = false
        }
    }

    IconButton(onClick = {
        clipboard.setText(AnnotatedString(text))
        justCopied = true
    }) {
        Icon(
            if (justCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
            contentDescription = "Copy output",
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

/**
 * Bordered container for a group of Playground cards - ported from Swift's
 * `PlaygroundGroupBoxStyle` (a visible rounded outline + label, distinct from
 * a plain unbounded section of cards).
 */
@Composable
fun PlaygroundGroupBox(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/** Info banner for the Read Data group - ported from Swift's PlaygroundNoteBanner. */
@Composable
fun PlaygroundNoteBanner(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(top = 2.dp),
            )
            Spacer(Modifier.padding(horizontal = 4.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
