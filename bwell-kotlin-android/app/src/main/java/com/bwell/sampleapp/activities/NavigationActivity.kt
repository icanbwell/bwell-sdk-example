package com.bwell.sampleapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Secure
import android.provider.Settings.Secure.getString
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bwell.device.requests.deviceToken.DevicePlatform
import com.bwell.device.requests.deviceToken.RegisterDeviceTokenRequest
import com.bwell.sampleapp.BWellSampleApplication
import com.bwell.sampleapp.R
import com.bwell.sampleapp.databinding.ActivityNavigationBinding
import com.bwell.sampleapp.repository.Repository
import com.bwell.sampleapp.utils.getEncryptedSharedPreferences
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch


class NavigationActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var deviceId:String
    private lateinit var repository:Repository
    private lateinit var navController: NavController

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = (this.application as? BWellSampleApplication)?.bWellRepository!!

        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarNavigation.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_navigation)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_login, R.id.nav_home, R.id.nav_data_connections, R.id.nav_health_summary, R.id.nav_health_journey, R.id.nav_insurance, R.id.nav_profile, R.id.nav_labs, R.id.nav_medicines
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
        askNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        handleDeeplink()
    }

    private fun handleDeeplink() {
        intent.extras?.getString("action")?.let { action ->
            if (action.startsWith("ActivityDefinition/")) {
                val bundle = Bundle().apply {
                    val taskId = action.replace("ActivityDefinition/", "")
                    putString("task_id", taskId)
                }
                navController.navigate(R.id.nav_health_journey, bundle)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_navigation)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        //unregisterDeviceToken(deviceId)
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

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
