package com.bwell.sampleapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bwell.sampleapp.activities.NavigationActivity

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
@RunWith(AndroidJUnit4::class)
class MyActivityTest {

    @Test
    fun testGetStartedButton() {
        // Launch Activity
        val activityScenario = ActivityScenario.launch(NavigationActivity::class.java)

//        // Interact with UI
        Espresso.onView(ViewMatchers.withId(R.id.btn_get_started))
            .perform(ViewActions.click())

//        // Assert outcomes
        Espresso.onView(ViewMatchers.withId(R.id.header))
            .check(ViewAssertions.matches(ViewMatchers.withText("Connect your health records")))
    }
}
