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
import com.bwell.common.models.responses.Status
import kotlinx.coroutines.launch
import android.provider.Settings.Secure
import android.provider.Settings.Secure.getString
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.bwell.BWellSdk

class NavigationActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var deviceId:String

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        deviceId = getString(contentResolver, Secure.ANDROID_ID)
        registerDeviceToken(deviceId)
    }

    private fun registerDeviceToken(deviceToken: String) {
        lifecycleScope.launch {
            try {
                val outcome = BWellSdk.device?.registerDeviceToken(deviceToken)
                if (outcome != null) {
                    when (outcome.status) {
                        Status.SUCCESS -> {
                            Log.d("registerDeviceToken-","${outcome.status}")
                        }

                        else -> {
                            Log.d("registerDeviceToken Error-","${outcome.exception}")
                        }
                    }
                }
            } catch (_: Exception) {
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
            try {
                val outcome = BWellSdk.device?.deregisterDeviceToken(deviceToken)
                if (outcome != null) {
                    when (outcome.status) {
                        Status.SUCCESS -> {
                            Log.d("deregisterDeviceToken-","${outcome.status}")
                        }

                        else -> {
                            Log.d("deregisterDeviceToken Error-","${outcome.exception}")
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}
