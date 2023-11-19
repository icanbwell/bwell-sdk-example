package com.bwell.sampleapp.model

data class HealthJourneyList(
    val healthJourneyList :List<HealthJourneyListItems>
)

data class HealthJourneyListItems(val type: String,
                                  val typeLogo: Int,val description: String,val moreLogo: Int)
