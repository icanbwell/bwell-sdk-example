package com.bwell.sampleapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bwell.sampleapp.activities.NavigationActivity
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsCategoriesListAdapter
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateDataConnectionTest {

    @Test
    fun testCreateConnection() {
        // Launch Activity
        val activityScenario = ActivityScenario.launch(NavigationActivity::class.java)

        // Click Login Button on Login Screen
        Espresso.onView(ViewMatchers.withId(R.id.buttonLogin))
            .perform(ViewActions.click())

        // Click Get Started button on Home screen
        Espresso.onView(ViewMatchers.withId(R.id.btn_get_started))
            .perform(ViewActions.click())

        // Assert outcomes
        Espresso.onView(ViewMatchers.withId(R.id.header))
            .check(ViewAssertions.matches(ViewMatchers.withText("Connect your health records")))

        Espresso.onView(ViewMatchers.withId(R.id.btn_get_started))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.rv_suggested_data_connection_categories))
            .perform(RecyclerViewActions.actionOnItemAtPosition<DataConnectionsCategoriesListAdapter.ViewHolder>(2, ViewActions.click()))
    }
}
