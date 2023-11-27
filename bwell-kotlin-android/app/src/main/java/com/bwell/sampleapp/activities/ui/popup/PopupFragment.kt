package com.bwell.sampleapp.activities.ui.popup

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bwell.common.models.domain.data.Connection
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.R


class PopupFragment : DialogFragment() {

    interface PopupListener {
        fun onGetDataButtonClicked()
    }

    private var popupListener: PopupListener? = null
    private lateinit var popupView: View

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
        popupView = view;
        /*val getDataButton: Button = view.findViewById(R.id.get_data)
        // Set up the button click listener
        getDataButton.setOnClickListener {
            // Close the popup
            //dismiss()
            // Notify the listener (YourFragment) that the close button is clicked
            popupListener?.onGetDataButtonClicked()
        }*/
    }

    fun showConnectionsResult(result: BWellResult<Connection>) {
        val llLayout: LinearLayout = popupView.findViewById(R.id.ll_layout)

        when (result) {
            is BWellResult.ResourceCollection -> {
                for (connection in result.data.orEmpty()) {
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    // Create a vertical LinearLayout for each connection
                    val rowLayout = LinearLayout(requireContext())
                    rowLayout.layoutParams = layoutParams
                    rowLayout.orientation = LinearLayout.VERTICAL

                    // Iterate through each attribute and create a TextView for the key-value pair
                    val attributes = listOf(
                        "ID" to connection.id,
                        "Last Synced" to connection.lastSynced,
                        "Name" to connection.name,
                        "Status" to connection.status.toString(),
                        "Status Updated" to connection.statusUpdated,
                        "Sync Status" to connection.syncStatus.toString(),
                        "Type" to connection.type.toString(),
                        "URL" to connection.url
                    )

                    for ((key, value) in attributes) {
                        val attributeTextView = TextView(requireContext())
                        attributeTextView.text = Html.fromHtml("<b>$key:</b> $value")
                        attributeTextView.layoutParams = layoutParams
                        rowLayout.addView(attributeTextView)
                    }

                    // Add the rowLayout to llLayout
                    llLayout.addView(rowLayout)
                }
            }

            else -> {}
        }
    }


    // Setter method for the listener
    fun setPopupListener(listener: PopupListener) {
        this.popupListener = listener
    }
}
