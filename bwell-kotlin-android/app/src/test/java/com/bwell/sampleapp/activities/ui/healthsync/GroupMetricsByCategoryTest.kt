package com.bwell.sampleapp.activities.ui.healthsync

import com.bwell.common.models.domain.common.CodeableConcept
import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.healthdata.healthsummary.devicemetrics.DeviceMetricsGroup
import org.junit.Assert.assertEquals
import org.junit.Test

private const val DISPLAY_GROUP_SYSTEM = "https://www.icanbwell.com/display-group"

class GroupMetricsByCategoryTest {

    private fun group(name: String, code: String? = null, display: String? = null) = DeviceMetricsGroup(
        id = null,
        name = name,
        source = null,
        sourceDisplay = null,
        category = code?.let {
            listOf(CodeableConcept(coding = listOf(Coding(system = DISPLAY_GROUP_SYSTEM, code = it, display = display))))
        },
        coding = null,
        effectiveDateTime = null,
        value = null,
        interpretation = null,
        referenceRange = null,
        component = null,
        references = null,
    )

    @Test
    fun `groups by display-group coding code`() {
        val groups = listOf(
            group("Heart Rate", code = "cardiovascular", display = "Cardiovascular"),
            group("Steps", code = "activity", display = "Activity"),
            group("Blood Pressure", code = "cardiovascular", display = "Cardiovascular"),
        )

        val result = groupMetricsByCategory(groups)

        assertEquals(2, result.size)
        assertEquals("cardiovascular", result[0].id)
        assertEquals("Cardiovascular", result[0].label)
        assertEquals(2, result[0].items.size)
        assertEquals("activity", result[1].id)
        assertEquals(1, result[1].items.size)
    }

    @Test
    fun `groups preserve first-seen order`() {
        val groups = listOf(
            group("Steps", code = "activity", display = "Activity"),
            group("Heart Rate", code = "cardiovascular", display = "Cardiovascular"),
        )

        val result = groupMetricsByCategory(groups)

        assertEquals(listOf("activity", "cardiovascular"), result.map { it.id })
    }

    @Test
    fun `missing display-group coding falls into Other`() {
        val groups = listOf(group("Mystery Metric"))

        val result = groupMetricsByCategory(groups)

        assertEquals(1, result.size)
        assertEquals("other", result[0].id)
        assertEquals("Other", result[0].label)
    }

    @Test
    fun `coding with no display falls back to Other label`() {
        val groups = listOf(group("Steps", code = "activity", display = null))

        val result = groupMetricsByCategory(groups)

        assertEquals("Other", result[0].label)
    }

    @Test
    fun `empty input produces no groups`() {
        assertEquals(emptyList<MetricCategoryGroup>(), groupMetricsByCategory(emptyList()))
    }
}
