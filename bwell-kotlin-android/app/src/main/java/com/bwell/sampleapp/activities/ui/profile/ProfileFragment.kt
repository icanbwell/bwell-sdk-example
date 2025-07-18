package com.bwell.sampleapp.activities.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bwell.common.models.domain.user.Person
import com.bwell.common.models.domain.user.VerificationStatus
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.FragmentProfileBinding
import com.bwell.sampleapp.viewmodel.SharedViewModelFactory
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userData: Person
    private lateinit var verificationStaus: VerificationStatus
    private lateinit var verificationUrl: String;
    private lateinit var selectedSex:String;
    private lateinit var selectedState:String;


    @SuppressLint("SetTextI18n", "NewApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.bWellRepository

        // Initialize ViewModel
        profileViewModel = ViewModelProvider(this, SharedViewModelFactory(repository))[ProfileViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.userData.collect{
                userData = it!!
                 updateUI(userData)
            }

        }
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.verificationStatus.collect{
                verificationStaus = it!!
                updateIAL2UI(verificationStaus)
            }
        }

        profileViewModel.fetchData()
        profileViewModel.fetchVerificationStatus()
        val leftArrowImageView: ImageView = binding.root.findViewById(R.id.leftArrowImageView)
        leftArrowImageView.setOnClickListener {
            binding.includeViewProfile.viewProfileParent.visibility= View.VISIBLE;
            binding.includeEditProfile.editProfileParent.visibility= View.GONE;
        }

        val saveButton: FrameLayout = binding.root.findViewById(R.id.frameLayoutSave)
        saveButton.setOnClickListener {
            val updatedPerson = Person.Builder()
                .firstName(binding.includeEditProfile.firstNameEditText.text.toString())
                .lastName(binding.includeEditProfile.lastNameEditText.text.toString())
                .birthDate(binding.includeEditProfile.dateofbirthEditText.text.toString())
                .mobilePhone(binding.includeEditProfile.phonenumberEditText.text.toString())
                .addressStreet(binding.includeEditProfile.primaryAddressEditText.text.toString())
                .city(binding.includeEditProfile.cityEditText.text.toString())
                .postageOrZipCode(binding.includeEditProfile.zipcodeEditText.text.toString())
                .stateOrProvidence(selectedState)
                .gender(selectedSex)
                .email(userData.email)
                .build()

            profileViewModel.updatePersonData(updatedPerson)
            profileViewModel.createConsent()
        }

        val  getIAL2VerifiedButton : FrameLayout = binding.root.findViewById(R.id.frameLayoutIAL2Button)
        getIAL2VerifiedButton.setOnClickListener {
            onGetIAL2VerifiedClick(it)
        }


        val editButton: FrameLayout = binding.root.findViewById(R.id.frameLayoutEditProfile)
        editButton.setOnClickListener {
            binding.includeViewProfile.viewProfileParent.visibility= View.GONE;
            binding.includeEditProfile.editProfileParent.visibility= View.VISIBLE;
            binding.includeEditProfile.editProfileTitleTextView.text= resources.getString(R.string.edit_profile);
            userData.let {
                binding.includeEditProfile.firstNameEditText.setText(it.firstName)
                binding.includeEditProfile.lastNameEditText.setText(it.lastName)
                binding.includeEditProfile.dateofbirthEditText.setText(it.birthDate)
                binding.includeEditProfile.phonenumberEditText.setText(it.mobilePhone)
                binding.includeEditProfile.primaryAddressEditText.setText(it.addressStreet)
                binding.includeEditProfile.cityEditText.setText(it.city)
                binding.includeEditProfile.zipcodeEditText.setText(it.postageOrZipCode)
                val sexArray = resources.getStringArray(R.array.sex_assigned_at_birth_options)
                val sexIndex = sexArray.indexOf(it.gender)
                binding.includeEditProfile.sexAssignedAtBirthSpinner.setSelection(sexIndex)
                val stateArray = resources.getStringArray(R.array.states)
                val stateIndex = stateArray.indexOf(it.stateOrProvidence)
                if (stateIndex != -1) {
                    binding.includeEditProfile.stateSpinner.setSelection(stateIndex)
                }
            }

        }

        val spinner: Spinner = binding.includeEditProfile.sexAssignedAtBirthSpinner
        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.sex_assigned_at_birth_options, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                selectedSex = spinner.selectedItem.toString()

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //create state Spinner
        val stateSpinner: Spinner = binding.includeEditProfile.stateSpinner
        val stateAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.states, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        stateSpinner.adapter = stateAdapter
        // Set the on item selected listener
        stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                selectedState = spinner.selectedItem.toString()

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

    @SuppressLint("SetTextI18n", "NewApi")
    private fun updateUI(userData: Person?) {
        if (userData == null) {
            binding.includeViewProfile.viewProfileParent.visibility= View.GONE;
            binding.includeEditProfile.editProfileParent.visibility= View.VISIBLE;
            binding.includeEditProfile.editProfileTitleTextView.text= resources.getString(R.string.create_profile);
        }else{
            binding.includeViewProfile.viewProfileParent.visibility= View.VISIBLE;
            binding.includeEditProfile.editProfileParent.visibility= View.GONE;
            userData.let {
                binding.includeViewProfile.textViewName.text= "${it.firstName} ${it.lastName}"
                binding.includeViewProfile.textViewIntialLetter.text= "${it.firstName?.firstOrNull()} "
                binding.includeViewProfile.textViewEmailData.text= it.email
                binding.includeViewProfile.textViewSexData.text= it.gender
                binding.includeViewProfile.textViewPhoneNumberData.text= it.mobilePhone
                binding.includeViewProfile.textViewDateOfBirthData.text= it.birthDate
                binding.includeViewProfile.textViewAgeData.text= it.birthDate?.let { it1 ->
                    profileViewModel.calculateAge(
                        it1
                    )
                }
                binding.includeViewProfile.textViewAddressData.text= it.addressStreet
                selectedSex = it.gender.toString();
                selectedState = it.stateOrProvidence.toString();
            }
        }
    }

    private fun updateIAL2UI(verificationStatus: VerificationStatus?) {
        if (verificationStatus == null) {
            binding.includeViewProfile.textViewIAL2Heading.visibility = View.GONE
            binding.includeViewProfile.textViewVerificationStatusData.visibility = View.GONE
        } else {
            binding.includeViewProfile.textViewVerificationStatusData.text =
                verificationStatus.status.toString()
            binding.includeViewProfile.textViewIAL2Button.text = resources.getString(R.string.update_ial2_verified)
        }
    }

    private fun onGetIAL2VerifiedClick(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.verificationUrl.collect { url ->
                if (!url.isNullOrEmpty()) {
                    verificationUrl = url
                    Log.i("Opening URL: ", url)

                    // Open the URL in the default browser
                    val browserIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    startActivity(browserIntent)
                }
            }
        }
        profileViewModel.createVerificationUrl()
    }
}
