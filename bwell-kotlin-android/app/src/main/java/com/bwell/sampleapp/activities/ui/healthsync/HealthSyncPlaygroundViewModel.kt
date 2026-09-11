package com.bwell.sampleapp.activities.ui.healthsync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bwell.common.models.domain.data.DeviceProvider
import com.bwell.common.models.domain.data.DeviceProviderList
import com.bwell.common.models.domain.healthdata.healthsummary.devicemetrics.DeviceMetricsGroup
import com.bwell.common.models.domain.healthdata.healthsummary.healthscore.HealthScoreResult
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.repository.HealthSyncRepository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Per-card result - each card runs and renders independently of the others. */
sealed interface PlaygroundCardState {
    data object Idle : PlaygroundCardState
    data object Loading : PlaygroundCardState
    data class Success(val raw: String) : PlaygroundCardState
    data class Error(val message: String) : PlaygroundCardState
}

/** A dropdown option carrying a raw code + its display label - ported from Swift's PlaygroundCodeOption. */
data class PlaygroundCodeOption(val code: String, val display: String?) {
    val label: String get() = if (display != null) "$code - $display" else code
}

data class BodySystemOption(val bodySystemId: String, val title: String?) {
    val label: String get() = title ?: bodySystemId
}

/**
 * Ported from Swift's HealthSyncPlaygroundViewModel. Drives every card in
 * [HealthSyncPlaygroundScreen]: dispatches a run, tracks per-card state, and
 * lazily loads the 3 dynamic dropdowns (device providers, device-metrics
 * codes, body systems) the same way Swift's `.task {}` modifiers do.
 */
class HealthSyncPlaygroundViewModel(private val repository: HealthSyncRepository) : ViewModel() {

    companion object {
        // Matches the internal healthsync-sample's convention for the
        // on-device source's connection id. Hardcoded here, same as Swift's
        // Playground hardcodes "apple_health" for setupMobileSync/
        // getDeviceUserStatus - it is not a user-editable field.
        const val PLAYGROUND_CONNECTION_ID = "health_connect"

        private const val TAG = "HealthSyncPlayground"
    }

    private val _results = MutableStateFlow<Map<HealthSyncPlaygroundEndpoint, PlaygroundCardState>>(emptyMap())
    val results: StateFlow<Map<HealthSyncPlaygroundEndpoint, PlaygroundCardState>> = _results.asStateFlow()

    private val _configured = MutableStateFlow(repository.isHealthSyncConfigured())
    val configured: StateFlow<Boolean> = _configured.asStateFlow()

    private val _deviceProviderOptions = MutableStateFlow<List<DeviceProvider>>(emptyList())
    val deviceProviderOptions: StateFlow<List<DeviceProvider>> = _deviceProviderOptions.asStateFlow()

    private val _selectedDeviceProvider = MutableStateFlow<DeviceProvider?>(null)
    val selectedDeviceProvider: StateFlow<DeviceProvider?> = _selectedDeviceProvider.asStateFlow()

    // Only used by deleteConnection - auto-prefilled from the selected
    // provider's slug, editable, distinct from PLAYGROUND_CONNECTION_ID.
    private val _deleteConnectionIdInput = MutableStateFlow("")
    val deleteConnectionIdInput: StateFlow<String> = _deleteConnectionIdInput.asStateFlow()

    private val _deviceMetricsCodeOptions = MutableStateFlow<List<PlaygroundCodeOption>>(emptyList())
    val deviceMetricsCodeOptions: StateFlow<List<PlaygroundCodeOption>> = _deviceMetricsCodeOptions.asStateFlow()

    private val _selectedDeviceMetricsCode = MutableStateFlow<String?>(null)
    val selectedDeviceMetricsCode: StateFlow<String?> = _selectedDeviceMetricsCode.asStateFlow()

    private val _bodySystemOptions = MutableStateFlow<List<BodySystemOption>>(emptyList())
    val bodySystemOptions: StateFlow<List<BodySystemOption>> = _bodySystemOptions.asStateFlow()

    private val _selectedBodySystemId = MutableStateFlow<String?>(null)
    val selectedBodySystemId: StateFlow<String?> = _selectedBodySystemId.asStateFlow()

    private var attached = false
    private val playgroundJson = GsonBuilder().setPrettyPrinting().create()

    /**
     * One-time setup, idempotent - safe to call from every screen that hosts
     * the Playground (direct or Dashboard tab). Callers must also call
     * [refreshDeviceDataOptions] right after - it is not included here (see
     * its doc).
     */
    fun attach() {
        if (attached) return
        attached = true
        _configured.value = repository.isHealthSyncConfigured()
        loadDeviceProviderOptions()
    }

    /**
     * Not gated like [attach] - call every time the Playground screen
     * (re-)enters composition, e.g. on each Dashboard tab switch back to
     * Playground. GET_DEVICE_METRICS/GET_BODY_SYSTEM_SCORE's own copy
     * promises their dropdowns populate "each time this card appears," and
     * a sync triggered from the Dashboard's "Sync with b.well" button runs
     * through [HealthSyncDashboardViewModel] entirely - this ViewModel has
     * no other way to learn that data now exists.
     */
    fun refreshDeviceDataOptions() {
        loadDeviceMetricsCodeOptions()
        loadBodySystemOptions()
    }

    /** For GET_OAUTH_URL/GET_DEVICE_PROVIDER_STATUS - selection only, never touches deleteConnectionIdInput. */
    fun onSelectDeviceProviderForLookup(provider: DeviceProvider) {
        _selectedDeviceProvider.value = provider
    }

    /**
     * For DELETE_CONNECTION only - prefills the id field from the selected
     * provider's slug. Kept separate from [onSelectDeviceProviderForLookup]:
     * all 3 provider dropdowns previously shared one callback that always
     * overwrote deleteConnectionIdInput, silently discarding a manually
     * edited value whenever a provider was (re-)selected on either of the
     * two unrelated lookup cards.
     */
    fun onSelectDeviceProviderForDelete(provider: DeviceProvider) {
        _selectedDeviceProvider.value = provider
        _deleteConnectionIdInput.value = provider.slug
    }

    fun onDeleteConnectionIdInputChanged(value: String) {
        _deleteConnectionIdInput.value = value
    }

    fun onSelectDeviceMetricsCode(code: String?) {
        _selectedDeviceMetricsCode.value = code
    }

    fun onSelectBodySystem(bodySystemId: String) {
        _selectedBodySystemId.value = bodySystemId
    }

    private fun loadDeviceProviderOptions() {
        viewModelScope.launch {
            // Reachable before login - see guardedCall's doc. The provider
            // picker just stays empty and its dependent cards stay disabled
            // (see isRunDisabled) on failure.
            val result = guardedCall<BWellResult<DeviceProviderList>?>(TAG, "loadDeviceProviderOptions", null) {
                repository.getDeviceProviders()
            } ?: return@launch
            if (result is BWellResult.SingleResource && result.success()) {
                val providers = result.data?.providers.orEmpty()
                _deviceProviderOptions.value = providers
                if (_selectedDeviceProvider.value == null) {
                    providers.firstOrNull()?.let(::onSelectDeviceProviderForDelete)
                }
            }
        }
    }

    /** Dedupes getDeviceMetricsGroups() results by coding.code, sorted - mirrors Swift's loadDeviceMetricsCodeOptions. */
    private fun loadDeviceMetricsCodeOptions() {
        viewModelScope.launch {
            val result = guardedCall<BWellResult<DeviceMetricsGroup>?>(TAG, "loadDeviceMetricsCodeOptions", null) {
                repository.getDeviceMetricsGroups()
            } ?: return@launch
            if (result is BWellResult.ResourceCollection && result.success()) {
                val options = result.data.orEmpty()
                    .mapNotNull { it.coding?.code?.let { code -> PlaygroundCodeOption(code, it.coding?.display) } }
                    .distinctBy { it.code }
                    .sortedBy { it.code }
                _deviceMetricsCodeOptions.value = options
                if (_selectedDeviceMetricsCode.value != null && options.none { it.code == _selectedDeviceMetricsCode.value }) {
                    _selectedDeviceMetricsCode.value = null
                }
            }
        }
    }

    /** Sourced from getHealthScore()'s contributing body systems - mirrors Swift's loadBodySystemOptions. */
    private fun loadBodySystemOptions() {
        viewModelScope.launch {
            val result = guardedCall<BWellResult<HealthScoreResult>?>(TAG, "loadBodySystemOptions", null) {
                repository.getHealthScore()
            } ?: return@launch
            if (result is BWellResult.SingleResource && result.success()) {
                val options = result.data?.resource?.bodySystems.orEmpty()
                    .mapNotNull { it.bodySystemId?.let { id -> BodySystemOption(id, it.title) } }
                _bodySystemOptions.value = options
                if (_selectedBodySystemId.value == null) {
                    options.firstOrNull()?.let { _selectedBodySystemId.value = it.bodySystemId }
                }
            }
        }
    }

    fun run(endpoint: HealthSyncPlaygroundEndpoint) {
        val blockedReason = HealthSyncGating.blockedReason(endpoint, _configured.value)
        if (blockedReason != null) {
            _results.value = _results.value + (endpoint to PlaygroundCardState.Error(blockedReason))
            return
        }
        _results.value = _results.value + (endpoint to PlaygroundCardState.Loading)
        viewModelScope.launch {
            val cardState = try {
                runEndpoint(endpoint)
            } catch (e: Exception) {
                PlaygroundCardState.Error(e.message ?: "Unexpected exception")
            }
            _results.value = _results.value + (endpoint to cardState)

            if (endpoint == HealthSyncPlaygroundEndpoint.SYNC && cardState is PlaygroundCardState.Success) {
                refreshDeviceDataOptions()
            }
        }
    }

    private suspend fun runEndpoint(endpoint: HealthSyncPlaygroundEndpoint): PlaygroundCardState = when (endpoint) {
        HealthSyncPlaygroundEndpoint.CONNECT ->
            repository.connect().toCardState { "connect() completed successfully." }
        HealthSyncPlaygroundEndpoint.DISCONNECT ->
            repository.disconnect().toCardState { "disconnect() completed successfully." }
        HealthSyncPlaygroundEndpoint.REQUEST_PERMISSIONS ->
            repository.requestPermissions().toCardState { "requestPermissions() completed successfully." }
        HealthSyncPlaygroundEndpoint.SYNC ->
            repository.sync().toCardState()
        HealthSyncPlaygroundEndpoint.GET_CURRENT_USER -> {
            val result = repository.currentUser()
            val user = (result as? BWellResult.SingleResource)?.data
            when {
                !result.success() -> PlaygroundCardState.Error(result.error?.message() ?: "Unknown error")
                user == null -> PlaygroundCardState.Success("No active session.")
                else -> PlaygroundCardState.Success(
                    "userId: ${user.userId}\norganizationId: ${user.organizationId}",
                )
            }
        }
        HealthSyncPlaygroundEndpoint.SETUP_MOBILE_SYNC ->
            repository.setupMobileSync(PLAYGROUND_CONNECTION_ID).toCardState()
        HealthSyncPlaygroundEndpoint.GET_DEVICE_USER_STATUS ->
            repository.getDeviceUserStatus(PLAYGROUND_CONNECTION_ID).toCardState()
        HealthSyncPlaygroundEndpoint.GET_OAUTH_URL ->
            requireSelectedProvider()?.let { repository.getOauthUrl(it.slug).toCardState() }
                ?: PlaygroundCardState.Error("Select a provider first")
        HealthSyncPlaygroundEndpoint.GET_DEVICE_PROVIDER_STATUS ->
            requireSelectedProvider()?.let { repository.getDeviceProviderStatus(it.slug).toCardState() }
                ?: PlaygroundCardState.Error("Select a provider first")
        HealthSyncPlaygroundEndpoint.GET_DEVICE_PROVIDERS ->
            repository.getDeviceProviders().toCardState()
        HealthSyncPlaygroundEndpoint.DELETE_CONNECTION -> {
            val connectionId = _deleteConnectionIdInput.value.trim()
            if (connectionId.isEmpty()) {
                PlaygroundCardState.Error("Enter a connection id first")
            } else {
                val outcome = repository.deleteConnection(connectionId)
                if (outcome.success()) {
                    PlaygroundCardState.Success("deleteConnection() completed successfully.")
                } else {
                    PlaygroundCardState.Error(outcome.message())
                }
            }
        }
        HealthSyncPlaygroundEndpoint.GET_DEVICE_METRICS ->
            repository.getDeviceMetrics(_selectedDeviceMetricsCode.value).toCardState()
        HealthSyncPlaygroundEndpoint.GET_DEVICE_METRICS_GROUPS ->
            repository.getDeviceMetricsGroups().toCardState()
        HealthSyncPlaygroundEndpoint.GET_HEALTH_SCORE ->
            repository.getHealthScore().toCardState()
        HealthSyncPlaygroundEndpoint.GET_BODY_SYSTEM_SCORE ->
            _selectedBodySystemId.value?.let { repository.getBodySystemScore(it).toCardState() }
                ?: PlaygroundCardState.Error("Select a body system first")
    }

    private fun requireSelectedProvider(): DeviceProvider? = _selectedDeviceProvider.value

    private fun BWellResult<*>.toCardState(onSuccess: (() -> String)? = null): PlaygroundCardState = when {
        !success() -> PlaygroundCardState.Error(error?.message() ?: "Unknown error")
        onSuccess != null -> PlaygroundCardState.Success(onSuccess())
        this is BWellResult.SingleResource<*> -> PlaygroundCardState.Success(playgroundJson.toJson(data))
        this is BWellResult.ResourceCollection<*> ->
            PlaygroundCardState.Success(playgroundJson.toJson(mapOf("pagingInfo" to pagingInfo, "data" to data)))
        this is BWellResult.SearchResults<*> ->
            PlaygroundCardState.Success(playgroundJson.toJson(mapOf("pagingInfo" to pagingInfo, "data" to data)))
        else -> PlaygroundCardState.Success(playgroundJson.toJson(this))
    }
}
