package com.bwell.sampleapp.activities.ui.healthsync

import org.junit.Assert.assertEquals
import org.junit.Test

class HealthGradeTest {

    @Test
    fun `scores at or above 90 are A`() {
        assertEquals(HealthGrade.A, HealthGrade.from(90.0))
        assertEquals(HealthGrade.A, HealthGrade.from(100.0))
    }

    @Test
    fun `scores at or above 75 but below 90 are B`() {
        assertEquals(HealthGrade.B, HealthGrade.from(75.0))
        assertEquals(HealthGrade.B, HealthGrade.from(89.9))
    }

    @Test
    fun `scores at or above 60 but below 75 are C`() {
        assertEquals(HealthGrade.C, HealthGrade.from(60.0))
        assertEquals(HealthGrade.C, HealthGrade.from(74.9))
    }

    @Test
    fun `scores at or above 45 but below 60 are D`() {
        assertEquals(HealthGrade.D, HealthGrade.from(45.0))
        assertEquals(HealthGrade.D, HealthGrade.from(59.9))
    }

    @Test
    fun `scores below 45 are F`() {
        assertEquals(HealthGrade.F, HealthGrade.from(44.9))
        assertEquals(HealthGrade.F, HealthGrade.from(0.0))
    }
}
