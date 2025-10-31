package com.bwell.sampleapp.activities.ui.coverage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bwell.financials.requests.coverage.CoverageRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.databinding.FragmentCoverageBinding
import com.bwell.sampleapp.viewmodel.CoverageViewModel
import com.bwell.sampleapp.viewmodel.CoverageViewModelFactory
import com.bwell.sampleapp.activities.ui.coverage.subcomponents.CoverageCard
import com.bwell.sampleapp.activities.ui.coverage.subcomponents.CoverageSearchBar
import com.bwell.sampleapp.activities.ui.coverage.subcomponents.PagingInfo

/**
 * Coverage Fragment - Displays coverage information using Jetpack Compose
 */
class CoverageFragment : Fragment() {

    private var _binding: FragmentCoverageBinding? = null
    private val binding get() = _binding!!
    private lateinit var coverageViewModel: CoverageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoverageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize ViewModel
        val repository = (activity?.application as? BWellSampleApplication)?.financialsRepository
        val factory = CoverageViewModelFactory(repository)
        coverageViewModel = ViewModelProvider(this, factory)[CoverageViewModel::class.java]

        // Set up Compose UI
        binding.composeView.setContent {
            MaterialTheme {
                CoverageScreen(coverageViewModel)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Main Coverage Screen Composable
 */
@Composable
fun CoverageScreen(viewModel: CoverageViewModel) {
    val coverageResults by viewModel.coverageResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val pageSize by viewModel.pageSize.collectAsState()
    val totalItems by viewModel.totalItems.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val showJsonMode by viewModel.showJsonMode.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    // Launch coverage query when the composable is first created
    LaunchedEffect(Unit) {
        val coverageRequest = CoverageRequest.Builder()
            .page(0)
            .pageSize(20)
            .build()
        viewModel.getCoverage(coverageRequest)
    }

    // Update loading state based on results
    LaunchedEffect(coverageResults) {
        isLoading = coverageResults == null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with JSON Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Coverage",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // JSON Toggle Switch
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

            // Search Bar
            CoverageSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Get filtered results
                val filteredCoverage = viewModel.getFilteredCoverageList()

                if (filteredCoverage.isNullOrEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "No coverage found for \"$searchQuery\""
                            } else {
                                "No coverage information available"
                            },
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    // Display results
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredCoverage) { coverage ->
                            CoverageCard(coverage = coverage, showJsonMode = showJsonMode)
                        }
                    }

                    // Paging Information
                    PagingInfo(
                        currentPage = currentPage,
                        pageSize = pageSize,
                        totalItems = totalItems,
                        totalPages = totalPages,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

