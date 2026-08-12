package com.bwell.sampleapp.activities.ui.healthsync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Ported from Swift's HealthSyncPlaygroundView: one group per
 * [HealthSyncPlaygroundGroup], the Read Data note banner, and a card per
 * endpoint with whatever input the endpoint needs.
 */
@Composable
fun HealthSyncPlaygroundScreen(viewModel: HealthSyncPlaygroundViewModel, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { viewModel.attach() }

    val results by viewModel.results.collectAsStateWithLifecycle()
    val configured by viewModel.configured.collectAsStateWithLifecycle()
    val deviceProviderOptions by viewModel.deviceProviderOptions.collectAsStateWithLifecycle()
    val selectedDeviceProvider by viewModel.selectedDeviceProvider.collectAsStateWithLifecycle()
    val deleteConnectionIdInput by viewModel.deleteConnectionIdInput.collectAsStateWithLifecycle()
    val deviceMetricsCodeOptions by viewModel.deviceMetricsCodeOptions.collectAsStateWithLifecycle()
    val selectedDeviceMetricsCode by viewModel.selectedDeviceMetricsCode.collectAsStateWithLifecycle()
    val bodySystemOptions by viewModel.bodySystemOptions.collectAsStateWithLifecycle()
    val selectedBodySystemId by viewModel.selectedBodySystemId.collectAsStateWithLifecycle()

    fun isRunDisabled(endpoint: HealthSyncPlaygroundEndpoint): Boolean = when (endpoint) {
        HealthSyncPlaygroundEndpoint.GET_OAUTH_URL,
        HealthSyncPlaygroundEndpoint.GET_DEVICE_PROVIDER_STATUS,
        -> selectedDeviceProvider == null
        HealthSyncPlaygroundEndpoint.GET_DEVICE_METRICS -> deviceMetricsCodeOptions.isEmpty()
        HealthSyncPlaygroundEndpoint.GET_BODY_SYSTEM_SCORE -> bodySystemOptions.isEmpty()
        else -> false
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column {
                Text(
                    "API Playground",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Test bench — run any endpoint independently, raw response shown per card.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        for (group in HealthSyncPlaygroundGroup.entries) {
            val endpoints = HealthSyncPlaygroundEndpoint.entries.filter { it.group == group }
            item {
                Text(
                    group.title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (group == HealthSyncPlaygroundGroup.READ_DATA) {
                item {
                    PlaygroundNoteBanner(
                        "Run connect, requestPermissions, and sync first (Mobile Sync group above) — " +
                            "Read Data reflects whatever was last synced. Data appears within 5 min.",
                    )
                }
            }
            items(endpoints) { endpoint ->
                PlaygroundCard(
                    endpoint = endpoint,
                    state = results[endpoint] ?: PlaygroundCardState.Idle,
                    isBlocked = HealthSyncGating.isBlocked(endpoint, configured),
                    blockedReason = HealthSyncGating.blockedReason(endpoint, configured),
                    runDisabled = isRunDisabled(endpoint),
                    onRun = { viewModel.run(endpoint) },
                ) {
                    EndpointInputs(
                        endpoint = endpoint,
                        deviceProviderOptions = deviceProviderOptions,
                        selectedDeviceProvider = selectedDeviceProvider,
                        onSelectDeviceProviderForLookup = viewModel::onSelectDeviceProviderForLookup,
                        onSelectDeviceProviderForDelete = viewModel::onSelectDeviceProviderForDelete,
                        deleteConnectionIdInput = deleteConnectionIdInput,
                        onDeleteConnectionIdInputChanged = viewModel::onDeleteConnectionIdInputChanged,
                        deviceMetricsCodeOptions = deviceMetricsCodeOptions,
                        selectedDeviceMetricsCode = selectedDeviceMetricsCode,
                        onSelectDeviceMetricsCode = viewModel::onSelectDeviceMetricsCode,
                        bodySystemOptions = bodySystemOptions,
                        selectedBodySystemId = selectedBodySystemId,
                        onSelectBodySystem = viewModel::onSelectBodySystem,
                    )
                }
            }
        }
    }
}

@Composable
private fun EndpointInputs(
    endpoint: HealthSyncPlaygroundEndpoint,
    deviceProviderOptions: List<com.bwell.common.models.domain.data.DeviceProvider>,
    selectedDeviceProvider: com.bwell.common.models.domain.data.DeviceProvider?,
    onSelectDeviceProviderForLookup: (com.bwell.common.models.domain.data.DeviceProvider) -> Unit,
    onSelectDeviceProviderForDelete: (com.bwell.common.models.domain.data.DeviceProvider) -> Unit,
    deleteConnectionIdInput: String,
    onDeleteConnectionIdInputChanged: (String) -> Unit,
    deviceMetricsCodeOptions: List<PlaygroundCodeOption>,
    selectedDeviceMetricsCode: String?,
    onSelectDeviceMetricsCode: (String?) -> Unit,
    bodySystemOptions: List<BodySystemOption>,
    selectedBodySystemId: String?,
    onSelectBodySystem: (String) -> Unit,
) {
    when (endpoint) {
        HealthSyncPlaygroundEndpoint.GET_OAUTH_URL,
        HealthSyncPlaygroundEndpoint.GET_DEVICE_PROVIDER_STATUS,
        -> {
            LabeledDropdown(
                label = "Provider",
                options = deviceProviderOptions,
                selectedLabel = selectedDeviceProvider?.name,
                displayText = { it.name },
                onSelect = onSelectDeviceProviderForLookup,
            )
        }
        HealthSyncPlaygroundEndpoint.DELETE_CONNECTION -> {
            LabeledDropdown(
                label = "Provider",
                options = deviceProviderOptions,
                selectedLabel = selectedDeviceProvider?.name,
                displayText = { it.name },
                onSelect = onSelectDeviceProviderForDelete,
            )
            OutlinedTextField(
                value = deleteConnectionIdInput,
                onValueChange = onDeleteConnectionIdInputChanged,
                label = { Text("connectionId") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
        HealthSyncPlaygroundEndpoint.GET_DEVICE_METRICS -> {
            LabeledDropdown(
                label = "Code",
                options = deviceMetricsCodeOptions,
                selectedLabel = deviceMetricsCodeOptions.firstOrNull { it.code == selectedDeviceMetricsCode }?.label,
                displayText = { it.label },
                onSelect = { onSelectDeviceMetricsCode(it.code) },
                emptyHintText = "No codes available yet — sync some data first (Mobile Sync group above)",
            )
        }
        HealthSyncPlaygroundEndpoint.GET_BODY_SYSTEM_SCORE -> {
            LabeledDropdown(
                label = "Body system",
                options = bodySystemOptions,
                selectedLabel = bodySystemOptions.firstOrNull { it.bodySystemId == selectedBodySystemId }?.label,
                displayText = { it.label },
                onSelect = { onSelectBodySystem(it.bodySystemId) },
                emptyHintText = "No body systems available yet — a health score must be computed first",
            )
        }
        else -> Unit
    }
}

/** A read-only dropdown backed by a live options list - ported from healthsync-sample's SmartDropdown. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> LabeledDropdown(
    label: String,
    options: List<T>,
    selectedLabel: String?,
    displayText: (T) -> String,
    onSelect: (T) -> Unit,
    emptyHintText: String = "Loading options…",
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.padding(top = 8.dp),
    ) {
        OutlinedTextField(
            value = selectedLabel ?: if (options.isEmpty()) emptyHintText else "Select…",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayText(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
