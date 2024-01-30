package com.bwell.sampleapp.viewmodel

import androidx.lifecycle.ViewModel
import com.bwell.common.models.domain.common.Organization
import com.bwell.common.models.domain.search.Provider

class EntityInfoViewModel : ViewModel() {
    var provider: Provider? = null
    var organization: Organization? = null
}