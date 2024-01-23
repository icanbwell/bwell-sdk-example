package com.bwell.sampleapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.bwell.sampleapp.activities.NavigationActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MyActivityTest {

    @Test
    fun testGetStartedButton() {
        // Launch Activity
//        val activity: NavigationActivity = Robolectric.buildActivity(NavigationActivity::class.java)
//            .create()
//            .resume()
//            .get()

        val activityScenario = ActivityScenario.launch(NavigationActivity::class.java)
        assertNotNull(activityScenario)

//        // Interact with UI
        Espresso.onView(ViewMatchers.withId(R.id.btn_get_started))
            .perform(ViewActions.click())

//        // Assert outcomes
        Espresso.onView(ViewMatchers.withId(R.id.header))
            .check(ViewAssertions.matches(ViewMatchers.withText("Connect your health records")))
    }
}
