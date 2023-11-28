package com.bwell.sampleapp.activities.ui.popup

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.bwell.sampleapp.R


class PopupFragment : DialogFragment() {

    interface PopupListener {
        fun onCloseButtonClicked()
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
        val closeButton: Button = view.findViewById(R.id.closeButton)
        // Set up the button click listener
        closeButton.setOnClickListener {
            // Close the popup
            dismiss()
            // Notify the listener (YourFragment) that the close button is clicked
            popupListener?.onCloseButtonClicked()
        }
    }

    // Setter method for the listener
    fun setPopupListener(listener: PopupListener) {
        this.popupListener = listener
    }
}
