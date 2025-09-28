package com

import com.bwell.common.models.domain.common.Coding
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.code.AllergyIntoleranceCriticalityCode
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.AllergyIntoleranceGroup
import com.bwell.common.models.domain.healthdata.healthsummary.allergyintolerance.enums.AllergyIntoleranceCriticality
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.HealthSummary
import com.bwell.common.models.domain.healthdata.healthsummary.healthsummary.enums.HealthSummaryCategory
import com.bwell.common.models.responses.BWellResult
import com.bwell.healthdata.healthsummary.requests.allergyintolerance.AllergyIntoleranceGroupsRequest
import com.bwell.sampleapp.singletons.BWellSdk
import com.bwell.sampleapp.utils.BWellSdkInitializer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SdkIntegrationTests {
    @Before
    fun setUp() = runBlocking {
        // Expected values for these tests are for PROA Test User Constance Renner in Sandbox
        val clientKey = System.getenv("BWELL_TEST_CLIENT_KEY")
        val jwt = System.getenv("BWELL_TEST_JWT")
        require(!clientKey.isNullOrBlank()) { "BWELL_TEST_CLIENT_KEY env variable must be set" }
        require(!jwt.isNullOrBlank()) { "BWELL_TEST_JWT env variable must be set" }
        BWellSdkInitializer.initialize(null, clientKey, jwt)
    }

    @Test
    fun testGetHealthSummary() = runBlocking {
        // Assumes user is already logged in and session is valid
        val result = BWellSdk.health?.getHealthSummary() as? BWellResult.ResourceCollection<HealthSummary>
        val expected = BWellResult.ResourceCollection(
            data = listOf(
                HealthSummary(total=7, category = HealthSummaryCategory.ALLERGY_INTOLERANCE),
                HealthSummary(total = 8, category = HealthSummaryCategory.ENCOUNTER),
                HealthSummary(total = 2, category = HealthSummaryCategory.CARE_PLAN),
                HealthSummary(total = 4, category = HealthSummaryCategory.CONDITION),
                HealthSummary(total = 1, category = HealthSummaryCategory.IMMUNIZATION),
                HealthSummary(total = 2, category = HealthSummaryCategory.PROCEDURE),
                HealthSummary(total = 24, category = HealthSummaryCategory.VITAL_SIGNS),
                HealthSummary(total = 9, category = HealthSummaryCategory.MEDICATIONS),
                HealthSummary(total = 12, category = HealthSummaryCategory.LABS)
            ),
            pagingInfo = null, // Set if needed
            error = null // or set expected error if needed
        )
        println("getHealthSummary result: $result")
        assertEquals("HealthSummary list size should match", expected.data?.size, result?.data?.size)
        expected.data?.forEachIndexed { i, expectedItem ->
            val actualItem = result?.data?.get(i)
            assertEquals("total should match for item $i", expectedItem.total, actualItem?.total)
            assertEquals("category should match for item $i", expectedItem.category, actualItem?.category)
        }
        assertEquals("error should match", expected.error, result?.error)
    }

    @Test
    fun testGetAllergyIntoleranceGroups() = runBlocking {
        val request = AllergyIntoleranceGroupsRequest.Builder().build()
        val result = BWellSdk.health?.getAllergyIntoleranceGroups(request) as? BWellResult.ResourceCollection<AllergyIntoleranceGroup>
        assertEquals("AllergyIntoleranceGroup list size should match", 7, result?.data?.size)
        val expectedFirstGroup = AllergyIntoleranceGroup(
            id = "70555147-ffa4-5994-be0e-d176ebe21f4a",
            name = "NSAIDS (NON-STEROIDAL ANTI-INFLAMMATORY DRUG)",
            coding = Coding(system = "http://snomed.info/sct", code = "16403005", display = "NSAIDS (NON-STEROIDAL ANTI-INFLAMMATORY DRUG)"),
            references = listOf("993ba1ff-eb7c-49d0-90be-2723237b388d"),
            criticality = AllergyIntoleranceCriticalityCode(
                code = AllergyIntoleranceCriticality.HIGH,
                display = null
            ),
            source = listOf("proa_demo"),
            sourceDisplay = listOf("BWell PROA Demo"),
            recordedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse("2022-11-28T08:05:58Z")
        )
        val actualFirstGroup = result?.data?.get(0)
        assertEquals("id should match for first group", expectedFirstGroup.id, actualFirstGroup?.id)
        assertEquals("name should match for first group", expectedFirstGroup.name, actualFirstGroup?.name)
        assertEquals("coding should match for first group", expectedFirstGroup.coding, actualFirstGroup?.coding)
        assertEquals("references should match for first group", expectedFirstGroup.references, actualFirstGroup?.references)
        assertEquals("criticality code should match for first group", expectedFirstGroup.criticality?.code, actualFirstGroup?.criticality?.code)
        assertEquals("criticality display should match for first group", expectedFirstGroup.criticality?.display, actualFirstGroup?.criticality?.display)
        assertEquals("source should match for first group", expectedFirstGroup.source, actualFirstGroup?.source)
        assertEquals("sourceDisplay should match for first group", expectedFirstGroup.sourceDisplay, actualFirstGroup?.sourceDisplay)
        assertEquals("recordedDate should match for first group", expectedFirstGroup.recordedDate, actualFirstGroup?.recordedDate)
        assertEquals("error should match", null, result?.error)
    }
    // Add more endpoint tests here
}
