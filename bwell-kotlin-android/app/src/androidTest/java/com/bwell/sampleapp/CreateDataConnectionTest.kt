package com.bwell.sampleapp

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bwell.sampleapp.activities.NavigationActivity
import com.bwell.sampleapp.activities.ui.data_connections.DataConnectionsCategoriesListAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class CreateDataConnectionTest {

    private fun waitForView(viewMatcher: Matcher<View>, timeout: Long): ViewInteraction {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + timeout

        do {
            try {
                Espresso.onView(viewMatcher).check(ViewAssertions.matches(isDisplayed()))
                return Espresso.onView(viewMatcher) // If view is found return it
            } catch (e: Exception) {
                Thread.sleep(50)
            }
        } while (System.currentTimeMillis() < endTime)

        throw TimeoutException("View with matcher $viewMatcher not found after $timeout milliseconds")
    }

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
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<DataConnectionsCategoriesListAdapter.ViewHolder>(
                    2,
                    ViewActions.click()
                )
            )

        waitForView(withId(R.id.searchText), 5000) // waits up to 5 seconds

        Espresso.onView(ViewMatchers.withId(R.id.searchText))
            .perform(ViewActions.typeText("bwell"))

        Thread.sleep(2000)

        waitForView(ViewMatchers.withId(R.id.rv_clinics), 5000)

        Espresso.onView(ViewMatchers.withId(R.id.itemText))
            .check(ViewAssertions.matches(withText("BWell PROA Demo")))

        // Delay to keep the app open for a while after test completion
        runBlocking {
            delay(10000) // Delay for 10 seconds
        }
    }
}
