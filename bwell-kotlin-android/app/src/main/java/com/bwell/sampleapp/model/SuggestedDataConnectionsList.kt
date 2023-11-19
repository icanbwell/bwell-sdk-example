package com.bwell.sampleapp.model

data class SuggestedDataConnectionsList(
    val suggestedDataConnectionsList :List<DataConnectionListItems>
)

data class DataConnectionListItems(val connectionName: String,
                                   val connectionLogo: Int,val status: String,val statusChangeLogo: Int)

data class SuggestedDataConnectionsCategoriesList(
    val suggestedDataConnectionsCategoriesList :List<DataConnectionCategoriesListItems>
)

data class DataConnectionCategoriesListItems(val connectionCategoryName: String,
                                             val connectionLogo: Int,val description: String)

data class DataConnectionsClinicsList(
    val dataConnectionsClinicsList :List<DataConnectionsClinicsListItems>
)

data class DataConnectionsClinicsListItems(val clinicLogo: Int,val clinicName: String)
