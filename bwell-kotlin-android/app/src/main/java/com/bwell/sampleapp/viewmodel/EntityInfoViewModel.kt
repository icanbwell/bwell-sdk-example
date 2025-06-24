package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import com.bwell.common.models.domain.common.Organization
import com.bwell.common.models.domain.search.HealthResource
import com.bwell.common.models.domain.search.Provider

class EntityInfoViewModel : ViewModel() {
    var provider: HealthResource? = null
    var organization: HealthResource? = null

    fun<T> getId(entity:T?): String {

        when (val nonNullEntity = requireNotNull(entity) { "Entity cannot be null in OrganizationInfoFragment" }) {
            is HealthResource -> {
                val endpoint = nonNullEntity.endpoint?.filter {
                        ep -> ep?.identifier?.any {
                        id -> id?.system?.contains("connectionhub/clientconnections") ?: false }
                    ?: return "" }
                    ?.first() ?: throw Exception("No clientConnections endpoint present on Organization entity.")

                return endpoint.name ?: throw Exception("No name present on Health Resource endpoint")
            }
        }

        // Should not get here
        throw IllegalStateException("Could not get entity id. Must be either Provider or Organization.")
    }

    fun<T> getName(entity: T?): String {
        when (val nonNullEntity =
            requireNotNull(entity) { "Entity cannot be null in OrganizationInfoFragment" }) {
            is HealthResource -> {
                return nonNullEntity.content.toString()
            }
        }

        throw IllegalStateException("Could not get entity name. Must be either Provider or Organization.")
    }
}