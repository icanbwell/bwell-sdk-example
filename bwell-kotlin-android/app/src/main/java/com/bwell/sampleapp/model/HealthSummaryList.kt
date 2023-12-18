package com.bwell.sampleapp.model

import com.bwell.common.models.domain.common.Period

data class HealthSummaryList(
    val healthSummaryList :List<HealthSummaryListItems>
)

data class HealthSummaryListItems(val healthSummaryTypeLogo: Int, val healthSummaryDetailsLogo: Int, val category: String?,
                                  val date: Period)
