package com.bwell.sampleapp.activities.ui.questionnaire

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentQuestionnaireBinding
import com.bwell.sampleapp.viewmodel.QuestionnaireViewModel
import com.bwell.sampleapp.viewmodel.QuestionnaireViewModelFactory

/**
 * Fragment for displaying Questionnaire responses
 */
class QuestionnaireFragment : Fragment() {

    private var _binding: FragmentQuestionnaireBinding? = null
    private val binding get() = _binding!!
    private lateinit var questionnaireViewModel: QuestionnaireViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionnaireBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        val repository = (activity?.application as? BWellSampleApplication)?.questionnaireRepository
        questionnaireViewModel = ViewModelProvider(
            this,
            QuestionnaireViewModelFactory(repository)
        )[QuestionnaireViewModel::class.java]
        
        questionnaireViewModel.questionnaireData.observe(viewLifecycleOwner) {
            setQuestionnaireAdapter(it)
        }

        return root
    }

    private fun setQuestionnaireAdapter(items: List<QuestionnaireItem>) {
        val adapter = QuestionnaireListAdapter(items)
        binding.rvQuestionnaire.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuestionnaire.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
