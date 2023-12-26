package com.bwell.sampleapp

import android.app.Application
import com.bwell.sampleapp.repository.ClinicsRepository
import com.bwell.sampleapp.repository.DataConnectionLabsRepository
import com.bwell.sampleapp.repository.DataConnectionsRepository
import com.bwell.sampleapp.repository.HealthSummaryRepository
import com.bwell.sampleapp.repository.HealthJourneyRepository
import com.bwell.sampleapp.repository.MedicineRepository
import com.bwell.sampleapp.repository.ProviderRepository
import com.bwell.sampleapp.repository.LabsRepository
import com.bwell.sampleapp.repository.Repository

class BWellSampleApplication : Application() {

     lateinit var bWellRepository: Repository
     lateinit var dataConnectionsRepository: DataConnectionsRepository
     lateinit var providerRepository: ProviderRepository
     lateinit var clinicsRepository: ClinicsRepository
     lateinit var dataConnectionLabsRepository: DataConnectionLabsRepository
    lateinit var labsRepository: LabsRepository
    lateinit var healthSummaryRepository: HealthSummaryRepository
     lateinit var medicineRepository: MedicineRepository
     lateinit var healthJourneyRepository: HealthJourneyRepository

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    private fun initialize() {
        bWellRepository = Repository( applicationContext)
        dataConnectionsRepository = DataConnectionsRepository( applicationContext)
        medicineRepository = MedicineRepository( applicationContext)
        providerRepository = ProviderRepository()
        clinicsRepository = ClinicsRepository( applicationContext)
        dataConnectionLabsRepository = DataConnectionLabsRepository( applicationContext)
        labsRepository = LabsRepository( applicationContext)
        healthSummaryRepository = HealthSummaryRepository( applicationContext)
        healthJourneyRepository = HealthJourneyRepository()

    }
}