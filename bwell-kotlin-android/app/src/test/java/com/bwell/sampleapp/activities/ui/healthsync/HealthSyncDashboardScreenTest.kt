package com.bwell.sampleapp.activities.ui.healthsync

import com.bwell.common.models.domain.common.Quantity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HealthSyncDashboardScreenTest {

    private fun quantity(value: Double?, unit: String? = null) =
        Quantity(value = value, unit = unit, comparator = null, system = null, code = null)

    @Test
    fun `null quantity is null`() {
        assertNull(formattedQuantity(null))
    }

    @Test
    fun `null value is null`() {
        assertNull(formattedQuantity(quantity(value = null, unit = "steps")))
    }

    @Test
    fun `whole number drops the decimal point`() {
        assertEquals("42 steps", formattedQuantity(quantity(value = 42.0, unit = "steps")))
    }

    @Test
    fun `fractional value keeps one decimal place`() {
        assertEquals("7.5 kg", formattedQuantity(quantity(value = 7.5, unit = "kg")))
    }

    @Test
    fun `missing unit is trimmed rather than left with a trailing space`() {
        assertEquals("10", formattedQuantity(quantity(value = 10.0, unit = null)))
    }
}
