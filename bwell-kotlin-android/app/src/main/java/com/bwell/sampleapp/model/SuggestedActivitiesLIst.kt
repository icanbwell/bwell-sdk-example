package com.bwell.sampleapp.model

data class SuggestedActivitiesLIst(
    val suggestedActivitiesLIst :List<ActivityListItems>
)

data class ActivityListItems(val  headerText :String,
                             val subText: String,
                             val icon: Int,)
