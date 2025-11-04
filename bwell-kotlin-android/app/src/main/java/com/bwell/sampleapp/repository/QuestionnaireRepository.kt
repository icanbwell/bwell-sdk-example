package com.bwell.sampleapp.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bwell.questionnaire.requests.questionnaireresponse.QuestionnaireResponseRequest
import com.bwell.common.models.responses.BWellResult
import com.bwell.sampleapp.R
import com.bwell.sampleapp.activities.ui.questionnaire.QuestionnaireItem
import com.bwell.sampleapp.singletons.BWellSdk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing questionnaire data
 */
class QuestionnaireRepository(private val applicationContext: Context) {

    private val TAG = QuestionnaireRepository::class.java.simpleName
    
    private val questionnaireLiveData = MutableLiveData<List<QuestionnaireItem>>()
    val questionnaireData: LiveData<List<QuestionnaireItem>>
        get() = questionnaireLiveData

    suspend fun getQuestionnaireList() {
        try {
            val questionnaireList = mutableListOf<QuestionnaireItem>()
            
            // Fetch QuestionnaireResponse count
            var questionnaireResponseCount: Int? = null
            try {
                val request = QuestionnaireResponseRequest.Builder()
                    .page(0)
                    .pageSize(20)
                    .build()
                    
                val result = withContext(Dispatchers.IO) {
                    BWellSdk.questionnaire.getQuestionnaireResponses(request)
                }
                
                if (result is BWellResult.ResourceCollection) {
                    questionnaireResponseCount = result.pagingInfo?.totalItems
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching questionnaire response count: ${e.message}")
            }
            
            questionnaireList.add(
                QuestionnaireItem(
                    R.drawable.baseline_person_24,
                    applicationContext.getString(R.string.questionnaire_response),
                    questionnaireResponseCount
                )
            )
            
            Log.d(TAG, "Added QuestionnaireResponse (count: $questionnaireResponseCount)")
            questionnaireLiveData.postValue(questionnaireList)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in getQuestionnaireList: ${e.message}")
        }
    }
}
