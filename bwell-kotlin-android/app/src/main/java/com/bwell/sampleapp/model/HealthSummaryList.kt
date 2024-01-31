package com.bwell.sampleapp.model

import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory

data class HealthSummaryList(
    val healthSummaryList :List<HealthSummaryListItems>
)

data class HealthSummaryListItems(
    val healthSummaryTypeLogo: Int,
    val healthSummaryDetailsLogo: Int,
    val category: HealthSummaryCategory,
    val categoryFriendlyName: String?,
    val count: Int?)
