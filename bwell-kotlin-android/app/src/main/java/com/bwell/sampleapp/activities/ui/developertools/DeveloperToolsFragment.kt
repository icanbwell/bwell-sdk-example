package com.bwell.sampleapp.activities.ui.developertools

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import kotlinx.coroutines.launch

class DeveloperToolsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_developer_tools, container, false)
        val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository

        val deleteUserTextView: TextView = view.findViewById(R.id.delete_user)
        deleteUserTextView.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.delete_user_confirmation))
                .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    lifecycleScope.launch {
                        try {
                            repository?.deleteUser()?.collect {
                                Toast.makeText(requireContext(), getString(R.string.delete_user) + " initiated", Toast.LENGTH_SHORT).show()
                                // Optionally navigate away after deletion
                            }
                        } catch (ex: Exception) {
                            Log.e("DeveloperToolsFragment", "Failed to delete user", ex)
                            Toast.makeText(requireContext(), getString(R.string.delete_user) + " failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        val getConsentsTextView: TextView = view.findViewById(R.id.get_consents)
        getConsentsTextView.setOnClickListener {
            lifecycleScope.launch {
                try {
                    repository?.getConsents()?.collect { result ->
                        Toast.makeText(requireContext(), "Consents fetched", Toast.LENGTH_SHORT).show()
                        // TODO: Display consents in UI as needed
                    }
                } catch (ex: Exception) {
                    Log.e("DeveloperToolsFragment", "Failed to fetch consents", ex)
                    Toast.makeText(requireContext(), "Failed to fetch consents", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}
