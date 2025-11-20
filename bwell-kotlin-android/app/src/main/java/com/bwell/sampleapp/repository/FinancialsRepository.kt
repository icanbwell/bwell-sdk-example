package com.bwell.sampleapp.repository

import android.content.Context
import com.bwell.common.models.domain.financials.coverage.Coverage
import com.bwell.common.models.domain.financials.explanationOfBenefit.ExplanationOfBenefit
import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.common.models.responses.BWellResult
import com.bwell.financials.requests.coverage.CoverageRequest
import com.bwell.financials.requests.explanationofbenefit.ExplanationOfBenefitRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FinancialsRepository(private val applicationContext: Context) {

    fun getCoverages(coverageRequest: CoverageRequest?): Flow<BWellResult<Coverage>?> = flow {
        try {
            val coveragesResult = BWellSdk.financials.getCoverages(coverageRequest)
            emit(coveragesResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

    fun getEOB(eobRequest: ExplanationOfBenefitRequest?): Flow<BWellResult<ExplanationOfBenefit>?> = flow {
        try {
            val eobResult = BWellSdk.financials.getExplanationOfBenefits(eobRequest)
            emit(eobResult)
        } catch (e: Exception) {
            emit(null)
        }
    }

}