package com.bwell.sampleapp.activities

import android.annotation.SuppressLint
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.ActivityNavigationBinding
import kotlinx.coroutines.launch
import android.provider.Settings.Secure
import android.provider.Settings.Secure.getString
import androidx.lifecycle.lifecycleScope
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.repository.Repository
import com.bwell.device.requests.deviceToken.DevicePlatform
import com.bwell.device.requests.deviceToken.RegisterDeviceTokenRequest

class NavigationActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var deviceId:String
    private lateinit var repository:Repository

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = (this.application as? BWellSampleApplication)?.bWellRepository!!

        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarNavigation.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_navigation)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_data_connections, R.id.nav_health_summary, R.id.nav_health_journey, R.id.nav_insurance, R.id.nav_profile, R.id.nav_labs, R.id.nav_medicines
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.itemIconTintList = null

        var tempDeviceId = getString(contentResolver, Secure.ANDROID_ID)

        if (tempDeviceId == null) {
            tempDeviceId = "foo"
        }
        deviceId = tempDeviceId
        val registerDeviceTokenRequest: RegisterDeviceTokenRequest = RegisterDeviceTokenRequest.Builder()
            .deviceToken(deviceId)
            .applicationName("Samsung Health PHR")
            .platform(DevicePlatform.ANDROID)
            .build()
        registerDeviceToken(registerDeviceTokenRequest)
    }

    private fun registerDeviceToken(registerDeviceTokenRequest: RegisterDeviceTokenRequest) {
        lifecycleScope.launch {
            val registerOutcome = repository.registerDeviceToken(registerDeviceTokenRequest)
            registerOutcome.collect { outcome ->
                outcome?.let {
                    if (outcome.success()) {
                        //device registered successfully
                    } else {
                        //device not registered
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_navigation)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        unregisterDeviceToken(deviceId)
        super.onDestroy()
    }

    private fun unregisterDeviceToken(deviceToken: String) {
        lifecycleScope.launch {
            val unregisterOutcome = repository.unregisterDeviceToken(deviceToken)
            unregisterOutcome.collect { outcome ->
                outcome?.let {
                    if (outcome.success()) {
                        //device registered successfully
                    } else {
                        //device not registered
                    }
                }
            }
        }
    }
}
