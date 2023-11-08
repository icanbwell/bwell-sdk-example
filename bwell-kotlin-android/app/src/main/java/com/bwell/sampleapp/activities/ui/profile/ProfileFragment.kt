package com.bwell.sampleapp.activities.ui.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentProfileBinding
import com.bwell.sampleapp.model.UserData

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userData: UserData
    private var selected_sex = "Male"
    private var selected_state = "Alabama"


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // Initialize ViewModel
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        // Observe changes in userData LiveData
        profileViewModel.userData.observe(viewLifecycleOwner) { userData ->
            if (userData != null) {
                this.userData = userData
            };
            // Update UI with the new userData
            updateUI(userData)
        }

        // Trigger data fetching
        profileViewModel.fetchData()

        // Find the save button by ID
        val saveButton: FrameLayout = binding.root.findViewById(R.id.frameLayoutSave)
        // Set a click listener for the save button
        saveButton.setOnClickListener {
            // Create a UserData object with the necessary data ( need to get this data from  UI components)
            val userData = UserData(
                binding.includeEditProfile.firstNameEditText.text.toString(),
                binding.includeEditProfile.lastNameEditText.text.toString(),
                binding.includeEditProfile.dateofbirthEditText.text.toString(),
                selected_sex,
                "30",
                binding.includeEditProfile.phonenumberEditText.text.toString(),
                binding.includeEditProfile.primaryAddressEditText.text.toString(),
                binding.includeEditProfile.cityEditText.text.toString(),
                selected_state,
                binding.includeEditProfile.zipcodeEditText.text.toString(),
                "john.doe@example.com"
            )
            // Call the saveData method from the ViewModel and pass the user data
            profileViewModel.saveData(userData)
        }

        // Find the save button by ID
        val editButton: FrameLayout = binding.root.findViewById(R.id.frameLayoutEditProfile)
        // Set a click listener for the save button
        editButton.setOnClickListener {
            binding.includeViewProfile.viewProfileParent.visibility= View.GONE;
            binding.includeEditProfile.editProfileParent.visibility= View.VISIBLE;
            binding.includeEditProfile.editProfileTitleTextView.text= resources.getString(R.string.edit_profile);
            userData.let {
                binding.includeEditProfile.firstNameEditText.setText(it.firstName)
                binding.includeEditProfile.lastNameEditText.setText(it.lastName)
                binding.includeEditProfile.dateofbirthEditText.setText(it.dateOfBirth)
                binding.includeEditProfile.phonenumberEditText.setText(it.mobileNumber)
                binding.includeEditProfile.primaryAddressEditText.setText(it.primaryAddress)
                binding.includeEditProfile.cityEditText.setText(it.city)
                binding.includeEditProfile.zipcodeEditText.setText(it.zipcode)
            }

        }

        //create sexAssignedAtBirthSpinner
        val spinner: Spinner = binding.includeEditProfile.sexAssignedAtBirthSpinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.sex_assigned_at_birth_options, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        spinner.adapter = adapter
        // Set the on item selected listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                // Get the selected item from the spinner
                selected_sex = spinner.selectedItem.toString()

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //create stateSpinner
        val stateSpinner: Spinner = binding.includeEditProfile.stateSpinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        val stateAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.states, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        stateSpinner.adapter = stateAdapter
        // Set the on item selected listener
        stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                // Get the selected item from the spinner
                selected_state = spinner.selectedItem.toString()

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(userData: UserData?) {
        // Update UI components with userData
        if (userData == null) {
            binding.includeViewProfile.viewProfileParent.visibility= View.GONE;
            binding.includeEditProfile.editProfileParent.visibility= View.VISIBLE;
            binding.includeEditProfile.editProfileTitleTextView.text= resources.getString(R.string.create_profile);
        }else{
            binding.includeViewProfile.viewProfileParent.visibility= View.VISIBLE;
            binding.includeEditProfile.editProfileParent.visibility= View.GONE;
            userData.let {
                binding.includeViewProfile.textViewName.text= "${it.firstName} ${it.lastName}"
                binding.includeViewProfile.textViewIntialLetter.text= "${it.firstName.firstOrNull()} "
                binding.includeViewProfile.textViewEmailData.text= it.email
                binding.includeViewProfile.textViewSexData.text= it.sexAssignedAtBirth
                binding.includeViewProfile.textViewPhoneNumberData.text= it.mobileNumber
                binding.includeViewProfile.textViewDateOfBirthData.text= it.dateOfBirth
                binding.includeViewProfile.textViewAgeData.text= it.age
                binding.includeViewProfile.textViewAddressData.text= it.primaryAddress
            }
        }
    }
}
