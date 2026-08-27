package com.bwell.sampleapp.activities.ui.insurance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.domain.financials.coverage.Coverage
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentInsuranceViewBinding
import com.bwell.sampleapp.viewmodel.InsuranceViewModelFactory
import kotlinx.coroutines.launch

class InsuranceFragment : Fragment() {

    private var _binding: FragmentInsuranceViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var insuranceViewModel: InsuranceViewModel
    private val claimsAdapter = ClaimsListAdapter(emptyList())

    // (label, coverageId) for each spinner entry; id is null for "All plans".
    private var planOptions: List<Pair<String, String?>> = listOf("All plans" to null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsuranceViewBinding.inflate(inflater, container, false)

        val repository = (activity?.application as? BWellSampleApplication)?.insuranceRepository
        insuranceViewModel =
            ViewModelProvider(this, InsuranceViewModelFactory(repository))[InsuranceViewModel::class.java]

        binding.rvClaims.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClaims.adapter = claimsAdapter

        binding.frameLayoutConnectInsurance.setOnClickListener {
            // Route into the data-connections flow to connect a payer (same as ui-platform).
            findNavController().popBackStack(R.id.nav_insurance, true)
            findNavController().navigate(R.id.nav_data_connections)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()

        // Coverage decides which state to show; claims load once the spinner is built.
        insuranceViewModel.loadCoverages()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            insuranceViewModel.coverages.collect { result ->
                // Anything that isn't a non-empty coverage collection (loading, error,
                // or genuinely no coverage) falls back to the connect prompt, so the
                // screen always has a recovery path instead of going blank.
                val coverages = (result as? BWellResult.ResourceCollection)?.data.orEmpty()
                if (coverages.isEmpty()) showConnectState() else showDataState(coverages)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            insuranceViewModel.claims.collect { result ->
                if (result !is BWellResult.ResourceCollection) return@collect
                val claims = result.data.orEmpty()
                claimsAdapter.updateList(claims)
                val label = planOptions.getOrNull(binding.spinnerPlans.selectedItemPosition)?.first ?: "All plans"
                binding.textClaimsSummary.text = "${claims.size} claims for $label"
            }
        }
    }

    /** No coverage → show the connect-insurance prompt. */
    private fun showConnectState() {
        binding.dataContainer.visibility = View.GONE
        binding.connectContainer.visibility = View.VISIBLE
    }

    /** Has coverage → show the plan filter + claims. */
    private fun showDataState(coverages: List<Coverage>) {
        binding.connectContainer.visibility = View.GONE
        binding.dataContainer.visibility = View.VISIBLE
        buildPlanSpinner(coverages)
    }

    private fun buildPlanSpinner(coverages: List<Coverage>) {
        planOptions = buildList {
            add("All plans" to null)
            coverages.forEach { add(planLabel(it) to it.id) }
        }

        binding.spinnerPlans.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            planOptions.map { it.first }
        )

        // Selecting an entry (including the initial "All plans") drives the claims query.
        binding.spinnerPlans.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                insuranceViewModel.loadClaims(planOptions[position].second)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /** Human-readable plan label: plan name, falling back to type / insurer / id. */
    private fun planLabel(coverage: Coverage): String {
        val planName = coverage.`class`
            ?.firstOrNull { it.type?.coding?.any { c -> c.code == "plan" } == true }?.name
        return planName
            ?: coverage.type?.text
            ?: coverage.payor?.firstOrNull()?.display
            ?: coverage.id
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
