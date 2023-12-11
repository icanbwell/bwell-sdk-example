package com.bwell.sampleapp

import android.app.Application
import com.bwell.sampleapp.repository.DataConnectionsRepository
import com.bwell.sampleapp.repository.HealthSummaryRepository
import com.bwell.sampleapp.repository.MedicineRepository
import com.bwell.sampleapp.repository.Repository

class BWellSampleApplication : Application() {

     lateinit var bWellRepository: Repository
     lateinit var dataConnectionsRepository: DataConnectionsRepository
    lateinit var medicineRepository: MedicineRepository
    lateinit var healthSummaryRepository: HealthSummaryRepository

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    private fun initialize() {
        bWellRepository = Repository( applicationContext)
        dataConnectionsRepository = DataConnectionsRepository( applicationContext)
        medicineRepository = MedicineRepository( applicationContext)
        healthSummaryRepository = HealthSummaryRepository( applicationContext)

    }
}