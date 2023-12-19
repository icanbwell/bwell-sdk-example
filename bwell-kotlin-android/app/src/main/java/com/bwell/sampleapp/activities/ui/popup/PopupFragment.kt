package com.bwell.sampleapp.activities.ui.popup

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.bwell.sampleapp.R


class PopupFragment : DialogFragment() {

    interface PopupListener {
        fun onSubmitButtonClicked(
            institute: String,
            provider: String,
            city: String,
            selectedState: String
        )
    }

    private var popupListener: PopupListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), theme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.popup_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var selectedState:String = "";
        val closeButton: FrameLayout = view.findViewById(R.id.frameLayoutCancel)
        val submitButton: FrameLayout = view.findViewById(R.id.frameLayoutSubmit)
        val instituteEditText: EditText = view.findViewById(R.id.instituteEditText)
        val providerEditText: EditText = view.findViewById(R.id.providerEditText)
        val cityEditText: EditText = view.findViewById(R.id.cityEditText)
        //create state Spinner
        val stateSpinner: Spinner = view.findViewById(R.id.stateSpinner)
        val stateAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.states, android.R.layout.simple_spinner_item)
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        stateSpinner.adapter = stateAdapter
        // Set the on item selected listener
        stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                selectedState = stateSpinner.selectedItem.toString()

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        closeButton.setOnClickListener {
            dismiss()
        }
        submitButton.setOnClickListener {
            dismiss()
            popupListener?.onSubmitButtonClicked(instituteEditText.text.toString(),providerEditText.text.toString(),cityEditText.text.toString(),selectedState)
        }
    }

    // Setter method for the listener
    fun setPopupListener(listener: PopupListener) {
        this.popupListener = listener
    }
}
