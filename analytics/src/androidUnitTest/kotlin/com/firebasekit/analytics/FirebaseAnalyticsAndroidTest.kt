package com.firebasekit.analytics

import android.os.Bundle as AndroidBundle
import com.google.firebase.analytics.FirebaseAnalytics as AndroidFirebaseAnalytics
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FirebaseAnalyticsAndroidTest {

    private val mockInstance: AndroidFirebaseAnalytics = mockk(relaxUnitFun = true)

    private fun sut() = FirebaseAnalyticsAndroid(mockInstance)

    @Test
    fun logEvent_passesNullBundle_whenParametersAreEmpty() {
        sut().logEvent("app_open")

        verify(exactly = 1) { mockInstance.logEvent("app_open", null) }
    }

    @Test
    fun logEvent_translatesParameters_toBundleValues() {
        val bundleSlot = slot<AndroidBundle>()
        every { mockInstance.logEvent("purchase", capture(bundleSlot)) } just runs

        sut().logEvent(
            name = "purchase",
            parameters = Bundle().apply {
                put("item_id", "pizza")
                put("quantity", 2)
                put("price", 12.5)
                put("is_featured", true)
            }
        )

        verify(exactly = 1) { mockInstance.logEvent("purchase", any()) }
        assertEquals("pizza", bundleSlot.captured.getString("item_id"))
        assertEquals(2L, bundleSlot.captured.getLong("quantity"))
        assertEquals(12.5, bundleSlot.captured.getDouble("price"))
        assertEquals(1L, bundleSlot.captured.getLong("is_featured"))
    }

    @Test
    fun setAnalyticsCollectionEnabled_delegatesToNativeSdk() {
        sut().setAnalyticsCollectionEnabled(false)

        verify(exactly = 1) { mockInstance.setAnalyticsCollectionEnabled(false) }
    }

    @Test
    fun logEvent_serializesJsonObjects_asJsonStrings() {
        val bundleSlot = slot<AndroidBundle>()
        every { mockInstance.logEvent("profile_update", capture(bundleSlot)) } just runs

        sut().logEvent(
            name = "profile_update",
            parameters = Bundle().apply {
                put(
                    key = "profile",
                    value = buildJsonObject {
                        put("name", "Razo")
                        put("score", 99)
                    }
                )
            }
        )

        assertEquals("{\"name\":\"Razo\",\"score\":99}", bundleSlot.captured.getString("profile"))
    }

    @Test
    fun logEvent_serializesKotlinxSerializableValues_beforeSending() {
        val bundleSlot = slot<AndroidBundle>()
        every { mockInstance.logEvent("favorite_toppings", capture(bundleSlot)) } just runs

        sut().logEvent(
            name = "favorite_toppings",
            parameters = Bundle().apply {
                put(
                    key = "toppings",
                    value = listOf("pepperoni", "mushrooms"),
                    serializer = ListSerializer(String.serializer())
                )
            }
        )

        assertEquals("[\"pepperoni\",\"mushrooms\"]", bundleSlot.captured.getString("toppings"))
    }

    @Test
    fun setUserId_delegatesToNativeSdk() {
        sut().setUserId("user-42")

        verify(exactly = 1) { mockInstance.setUserId("user-42") }
    }

    @Test
    fun setUserProperty_delegatesToNativeSdk() {
        sut().setUserProperty("favorite_food", "pizza")

        verify(exactly = 1) { mockInstance.setUserProperty("favorite_food", "pizza") }
    }

    @Test
    fun resetAnalyticsData_delegatesToNativeSdk() {
        sut().resetAnalyticsData()

        verify(exactly = 1) { mockInstance.resetAnalyticsData() }
    }
}
