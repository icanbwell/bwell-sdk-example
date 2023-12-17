package com.bwell.sampleapp.activities.ui.labs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentLabsParentBinding
import com.bwell.sampleapp.model.LabsListItems
import com.bwell.sampleapp.viewmodel.SharedViewModelFactory

class LabsFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentLabsParentBinding? = null

    private val binding get() = _binding!!
//    private lateinit var labsViewModel: LabsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLabsParentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository
//
//        labsViewModel = ViewModelProvider(this, SharedViewModelFactory(repository))[LabsViewModel::class.java]
//
//        labsViewModel.labsData.observe(viewLifecycleOwner) {
//            setLabsAdapter(it.labsList)
//        }

        binding.includeLabsDetail.leftArrowImageView.setOnClickListener(this)
        return root
    }

    private fun setLabsAdapter(suggestedActivitiesLIst: List<LabsListItems>) {
        val adapter = LabsListAdapter(suggestedActivitiesLIst)
        adapter.onItemClicked = { selectedLabType ->
            binding.includelabsData.labsFragment.visibility = View.GONE;
            binding.includeLabsDetail.labDetailFragment.visibility = View.VISIBLE;
        }
        binding.includelabsData.rvLabs.layoutManager = LinearLayoutManager(requireContext())
        binding.includelabsData.rvLabs.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.leftArrowImageView -> {
                binding.includelabsData.labsFragment.visibility = View.VISIBLE;
                binding.includeLabsDetail.labDetailFragment.visibility = View.GONE;
            }
        }
    }
}