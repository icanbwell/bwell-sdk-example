package com.bwell.sampleapp.model

data class LabsList(
    val labsList :List<LabsListItems>
)

data class LabsListItems(val labs_type: String,val labs_date: String,val labs_status: String)
