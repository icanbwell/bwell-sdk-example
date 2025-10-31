package com.bwell.sampleapp.activities.ui.eob

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bwell.sampleapp.activities.ui.eob.subcomponents.EOBCard
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bwell.financials.requests.explanationofbenefit.ExplanationOfBenefitRequest

import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.databinding.FragmentEobBinding
import com.bwell.sampleapp.viewmodel.EOBViewModel
import com.bwell.sampleapp.viewmodel.EOBViewModelFactory

class EOBFragment : Fragment() {
    private var _binding: FragmentEobBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EOBViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEobBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val repository = (activity?.application as? BWellSampleApplication)?.financialsRepository
        val factory = EOBViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EOBViewModel::class.java]

        binding.composeView.setContent {
            MaterialTheme {
                EOBScreen(viewModel)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@Composable
fun EOBScreen(viewModel: EOBViewModel) {
    val eobResults by viewModel.eobResults.collectAsState()
    val showJsonMode by viewModel.showJsonMode.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    // Fetch EOBs on first composition
    LaunchedEffect(Unit) {
        val eobRequest = ExplanationOfBenefitRequest.Builder().build()
        viewModel.getEOB(eobRequest)
    }
    LaunchedEffect(eobResults) {
        isLoading = eobResults == null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val eobs = when (val result = eobResults) {
            is com.bwell.common.models.responses.BWellResult.SingleResource -> result.data?.let { listOf(it) } ?: emptyList()
            is com.bwell.common.models.responses.BWellResult.ResourceCollection -> result.data ?: emptyList()
            is com.bwell.common.models.responses.BWellResult.SearchResults -> result.data ?: emptyList()
            else -> emptyList()
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header as first item
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Explanation of Benefit",
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "JSON",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = showJsonMode,
                                onCheckedChange = { viewModel.toggleJsonMode() }
                            )
                        }
                    }
                }

                // Empty state
                if (eobs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No EOB data available",
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    // EOB items
                    items(eobs) { eob ->
                        EOBCard(eob = eob, showJsonMode = showJsonMode)
                    }
                }
            }
        }
    }
}
