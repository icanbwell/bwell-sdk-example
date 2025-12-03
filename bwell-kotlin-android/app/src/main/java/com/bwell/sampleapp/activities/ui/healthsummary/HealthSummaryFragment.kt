package com.bwell.sampleapp.activities.ui.healthsummary

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntolerance
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceGroup
import com.bwell.common.models.domain.healthdata.healthsummary.careplan.CarePlanGroup
import com.bwell.common.models.domain.healthdata.healthsummary.condition.ConditionGroup
import com.bwell.common.models.domain.healthdata.healthsummary.documentreference.DocumentReference
import com.bwell.common.models.domain.healthdata.healthsummary.encounter.EncounterGroup
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.domain.healthdata.healthsummary.immunization.ImmunizationGroup
import com.bwell.common.models.domain.healthdata.healthsummary.procedure.ProcedureGroup
import com.bwell.common.models.domain.healthdata.healthsummary.vitalsign.VitalSignGroup
import com.bwell.common.models.domain.healthdata.medication.MedicationGroup
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.allergyintolerance.AllergyIntoleranceGroupsRequest
import com.bwell.healthdata.healthsummary.requests.careplan.CarePlanGroupsRequest
import com.bwell.healthdata.healthsummary.requests.condition.ConditionGroupsRequest
import com.bwell.healthdata.healthsummary.requests.documentReference.DocumentReferencesRequest
import com.bwell.healthdata.healthsummary.requests.encounter.EncounterGroupsRequest
import com.bwell.healthdata.healthsummary.requests.immunization.ImmunizationGroupsRequest
import com.bwell.healthdata.healthsummary.requests.procedure.ProcedureGroupsRequest
import com.bwell.healthdata.healthsummary.requests.vitalsign.VitalSignGroupsRequest
import com.bwell.healthdata.requests.binary.BinaryRequest
import com.bwell.provider.requests.organization.OrganizationRequest
import com.bwell.provider.requests.practitioner.PractitionerRequest
import com.bwell.provider.requests.practitionerrole.PractitionerRoleRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.medicines.MedicineDetailFragment
import com.bwell.sampleapp.databinding.FragmentHealthSummaryParentBinding
import com.bwell.sampleapp.model.HealthSummaryListItems
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModel
import com.bwell.sampleapp.viewmodel.HealthSummaryViewModelFactory
import kotlinx.coroutines.launch
import java.io.File

class HealthSummaryFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentHealthSummaryParentBinding? = null

    private val binding get() = _binding!!
    private lateinit var healthSummaryViewModel: HealthSummaryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthSummaryParentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val repository = (activity?.application as? BWellSampleApplication)?.healthSummaryRepository
        val providerResourcesRepository = (activity?.application as? BWellSampleApplication)?.providerResourcesRepository
        healthSummaryViewModel = ViewModelProvider(this, HealthSummaryViewModelFactory(repository, providerResourcesRepository))[HealthSummaryViewModel::class.java]
        healthSummaryViewModel.healthSummaryData.observe(viewLifecycleOwner) {
            setHealthSummaryAdapter(it.healthSummaryList)
        }
        binding.healthSummaryCategoriesDataView.leftArrowImageView.setOnClickListener(this)


        /**
         * Calling the getDocumentReference and getBinary
         */
        val documentReferenceRequest = DocumentReferencesRequest.Builder()
            .ids(listOf("55b12ebe-ce57-494c-831b-ff9814c410d2"))
            // plain
//            .ids(listOf("24f870b6-ff2c-4294-b7fc-6a097f8d7c6f"))
            .build()
        // Replace the id from what is received from the document reference
        val binaryRequest = BinaryRequest.Builder()
            .ids(listOf("4QPCDMNPf6PMktViyhqiCQ==|lhZknievmAGndyGp7qBG37PYq6Y9xWh9m2+dkYG3fYnApTzBouvTJp+lkovgfdrAvoyCUKvhLbnfGA=="))
            .build()
        healthSummaryViewModel.getDocumentReferences(documentReferenceRequest)
        healthSummaryViewModel.getBinary(binaryRequest)


        /**
         * Calling the getPractitioners
         */
        val practitionerRequest = PractitionerRequest.Builder()
            .id("00000590-d9c2-551f-9c5a-f3abb73aad4c")
            .build()
        healthSummaryViewModel.getPractitioners(practitionerRequest)

        val organizationRequest = OrganizationRequest.Builder().id("00000107-21f8-57a3-b59e-96f2e1e01775").build()
        healthSummaryViewModel.getOrganizations(organizationRequest)

        val practitionerRoleRequest = PractitionerRoleRequest.Builder()
            .id("00000590-d9c2-551f-9c5a-f3abb73aad4c")
            .build()
        healthSummaryViewModel.getPractitionerRoles(practitionerRoleRequest)

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                healthSummaryViewModel.documentReferenceRequestResults.collect { result ->
                    when(result) {
                        is BWellResult.ResourceCollection -> {
                            Log.i("DocumentReference", result.toString())
                            result.data?.forEach { r->
                                val attachment = r.content?.firstOrNull()?.attachment
                                val contentType = attachment?.contentType
                                val data = attachment?.data
                                val title = attachment?.title
                                if (data != null && contentType != null && title != null) {
                                    downloadBase64File(data, contentType, title)
                                }
                            }

                        }

                        else -> {
                            Log.i("DocumentReference", "DocumentReference didn't return BwellResult.ResourceCollection")
                        }
                    }


                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                healthSummaryViewModel.binaryResults.collect { result ->
                    when(result) {
                        is BWellResult.ResourceCollection -> {
                            Log.i("getBinary", result.toString())
                        }

                        else -> {
                            Log.i("getBinary", "getBinary didn't return BwellResult.ResourceCollection")
                        }
                    }


                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                healthSummaryViewModel.practitionersResults.collect { result ->
                    when(result) {
                        is BWellResult.ResourceCollection -> {
                            Log.i("getPractitioners", result.toString())
                        }
                        else -> {
                            Log.w("getPractitioners", "Unexpected result type: ${result?.javaClass?.simpleName}")
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                healthSummaryViewModel.organizationsResults.collect { result ->
                    when(result) {
                        is BWellResult.ResourceCollection -> {
                            Log.i("getOrganizations", result.toString())
                        }
                        else -> {
                            Log.w("getOrganizations", "Unexpected result type: ${result?.javaClass?.simpleName}")
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                healthSummaryViewModel.practitionerRoleResults.collect { result ->
                    when(result) {
                        is BWellResult.ResourceCollection -> {
                            Log.i("getPractitionerRoles", result.toString())
                        } else -> {
                            Log.w("getPractitionerRoles", "Unexpected result type: ${result?.javaClass?.simpleName}")
                        }
                    }
                }
            }
        }


        return root
    }

    private fun setHealthSummaryAdapter(suggestedActivitiesLIst: List<HealthSummaryListItems>) {
        val adapter = HealthSummaryListAdapter(suggestedActivitiesLIst)
        adapter.onItemClicked = { selectedList ->
            binding.healthSummaryCategoriesView.healthSummaryCategoriesView.visibility = View.GONE
            binding.healthSummaryCategoriesDataView.healthSummaryCategoriesDataView.visibility = View.VISIBLE

            val  healthSummaryRequest: Any? = when (selectedList.category) {
                HealthSummaryCategory.CARE_PLAN -> {
                    CarePlanGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.IMMUNIZATION -> {
                    ImmunizationGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.PROCEDURE -> {
                    ProcedureGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.VITAL_SIGNS -> {
                    VitalSignGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.ENCOUNTER -> {
                    EncounterGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.ALLERGY_INTOLERANCE -> {
                    AllergyIntoleranceGroupsRequest.Builder().build()
                }
                HealthSummaryCategory.CONDITION -> {
                    ConditionGroupsRequest.Builder().build()
                }
                else -> {
                    null
                }
            }

            if(healthSummaryRequest != null) {
                binding.healthSummaryCategoriesDataView.titleTextView.text = selectedList.categoryFriendlyName + " (" + selectedList.count + ")"
                healthSummaryViewModel.getHealthSummaryGroupData(
                    healthSummaryRequest,
                    selectedList.category
                )

                viewLifecycleOwner.lifecycleScope.launch {
                    healthSummaryViewModel.healthSummaryGroupResults.collect { result ->
                        if (result != null) {
                            setDataAdapter(result)
                        }
                    }
                }
            }
        }

        binding.healthSummaryCategoriesView.rvHealthSummary.layoutManager = LinearLayoutManager(requireContext())
        binding.healthSummaryCategoriesView.rvHealthSummary.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun <T> setDataAdapter(result: BWellResult<T>) {
        when (result) {
            is BWellResult.ResourceCollection -> {
                val dataList = result.data
                val adapter = HealthSummaryCategoriesDataAdapter(dataList)
                binding.healthSummaryCategoriesDataView.rvHealthSummaryCategories.layoutManager = LinearLayoutManager(requireContext())
                binding.healthSummaryCategoriesDataView.rvHealthSummaryCategories.adapter = adapter
                adapter.onItemClicked= { selectedResource ->
                    var fragment: Fragment? = null
                    when (selectedResource) {
                        is AllergyIntoleranceGroup -> {
                            fragment = AllergyIntoleranceDetailFragment()
                        }
                        is CarePlanGroup -> {
                            fragment = CarePlanDetailFragment()
                        }
                        is ConditionGroup -> {
                            fragment = ConditionDetailFragment()
                        }
                        is EncounterGroup -> {
                            fragment = EncounterDetailFragment()
                        }
                        is ImmunizationGroup -> {
                            fragment = ImmunizationDetailFragment()
                        }
                        is ProcedureGroup -> {
                            fragment = ProcedureDetailFragment()
                        }
                        is VitalSignGroup -> {
                            fragment = VitalSignDetailFragment()
                        }
                    }
                    if (fragment != null) {
                        val bundle = Bundle()
                        bundle.putString("id", adapter.getId(selectedResource))
                        bundle.putString("groupCode", adapter.getGroupCodeCode(selectedResource))
                        bundle.putString("groupSystem", adapter.getGroupCodeSystem(selectedResource))
                        bundle.putString("name", adapter.getName(selectedResource))
                        bundle.putString("from", adapter.getSource(selectedResource))
                        binding.healthSummaryCategoriesDataView.healthSummaryCategoriesDataView.visibility = View.GONE
                        fragment?.arguments = bundle
                        val transaction = childFragmentManager.beginTransaction()
                        binding.containerLayout.visibility = View.VISIBLE;
                        transaction.replace(R.id.container_layout, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }
            }
            else -> {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.leftArrowImageView -> {
                binding.healthSummaryCategoriesView.healthSummaryCategoriesView.visibility = View.VISIBLE
                binding.healthSummaryCategoriesDataView.healthSummaryCategoriesDataView.visibility = View.GONE
            }
        }
    }

    fun showHealthSummaryList() {
        binding.healthSummaryCategoriesView.healthSummaryCategoriesView.visibility = View.VISIBLE
    }

    /**
     * Downloads a base64 encoded file to the device's Downloads folder
     * @param base64String The base64 encoded string
     * @param contentType The MIME type (e.g., "image/jpg", "application/pdf")
     * @param titleOrTimestamp The file name prefix (without extension)
     *
     * Usage example:
     * downloadBase64File("SGVsbG8gV29ybGQ=", "image/jpg", "my_image_${System.currentTimeMillis()}")
     */
    fun downloadBase64File(base64String: String, contentType: String, titleOrTimestamp: String) {
        try {
            // Decode base64 string
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

            // Normalize content type
            val mimeType = normalizeContentType(contentType)

            // Get file extension from MIME type
            val extension = getExtensionFromMimeType(mimeType)

            // Create filename
            val fileName = "$titleOrTimestamp.$extension"

            // Save file using MediaStore
            saveWithMediaStore(fileName, mimeType, decodedBytes)

            Toast.makeText(requireContext(), "File saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("DownloadError", "Error downloading file: ${e.message}", e)
            Toast.makeText(requireContext(), "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Normalizes content type to standard MIME types
     */
    private fun normalizeContentType(contentType: String): String {
        return when (contentType.lowercase().trim()) {
            "image/jpg" -> "image/jpeg"
            "image/jpe" -> "image/jpeg"
            "application/x-pdf" -> "application/pdf"
            else -> contentType.lowercase().trim()
        }
    }

    /**
     * Gets file extension from MIME type
     */
    private fun getExtensionFromMimeType(mimeType: String): String {
        return when (mimeType.lowercase()) {
            "image/jpeg" -> "jpg"
            "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/bmp" -> "bmp"
            "image/webp" -> "webp"
            "application/pdf" -> "pdf"
            "text/plain" -> "txt"
            "text/html" -> "html"
            "application/json" -> "json"
            "application/xml" -> "xml"
            "text/xml" -> "xml"
            "application/zip" -> "zip"
            "application/msword" -> "doc"
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            "application/vnd.ms-excel" -> "xls"
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
            "image/tiff" -> "tiff"
            else -> {
                // Try to extract extension from MIME type (e.g., "image/jpeg" -> "jpeg")
                val parts = mimeType.split("/")
                if (parts.size == 2) parts[1] else "bin"
            }
        }
    }

    /**
     * Saves file to Downloads folder using MediaStore API (Android Q+)
     */
    private fun saveWithMediaStore(fileName: String, mimeType: String, data: ByteArray) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(data)
                outputStream.flush()
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            Log.i("FileSaved", "File saved successfully to Downloads: $fileName")
        } else {
            throw Exception("Failed to create file in MediaStore")
        }
    }
}