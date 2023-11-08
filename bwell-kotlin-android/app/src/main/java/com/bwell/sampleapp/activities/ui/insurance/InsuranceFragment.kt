package com.bwell.sampleapp.activities.ui.insurance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentInsuranceViewBinding

class InsuranceFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentInsuranceViewBinding? = null

    private val binding get() = _binding!!
    private lateinit var insuranceViewModel: InsuranceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsuranceViewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        insuranceViewModel  = ViewModelProvider(this).get(InsuranceViewModel::class.java)
        //use the view model

        binding.frameLayoutConnectInsurance.setOnClickListener(this)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.frameLayoutConnectInsurance ->
            {
                findNavController().popBackStack(R.id.nav_insurance, true)
                findNavController().navigate(R.id.nav_data_connections)
            }
        }
    }
}