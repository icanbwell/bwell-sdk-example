package com.bwell.sampleapp.repository

import android.content.Context
import com.bwell.common.models.domain.financials.coverage.Coverage
import com.bwell.common.models.domain.financials.explanationOfBenefit.ExplanationOfBenefit
import com.bwell.common.models.responses.BWellResult
import com.bwell.financials.requests.explanationofbenefit.ExplanationOfBenefitRequest
import com.bwell.sampleapp.singletons.BWellSdk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Wraps the b.well SDK financials calls used by the insurance demo:
 *  - getCoverages: the user's insurance plans (used to populate the filter).
 *  - getExplanationOfBenefits: claims, optionally filtered to a single plan via
 *    the coverage reference filter.
 */
class InsuranceRepository(private val applicationContext: Context) {

    suspend fun getCoverages(): Flow<BWellResult<Coverage>?> = flow {
        try {
            emit(BWellSdk.financials.getCoverages(null))
        } catch (e: Exception) {
            emit(null)
        }
    }

    /** @param coverageId bare Coverage id to filter by, or null for all plans. */
    suspend fun getClaims(coverageId: String?): Flow<BWellResult<ExplanationOfBenefit>?> = flow {
        try {
            val request = ExplanationOfBenefitRequest.Builder()
                .apply { if (!coverageId.isNullOrBlank()) coverage(coverageId) }
                .build()
            emit(BWellSdk.financials.getExplanationOfBenefits(request))
        } catch (e: Exception) {
            emit(null)
        }
    }
}
