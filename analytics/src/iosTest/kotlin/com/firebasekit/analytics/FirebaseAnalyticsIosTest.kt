package com.firebasekit.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

class FirebaseAnalyticsIosTest {

    private class FakeAnalyticsBridge : AnalyticsBridge {
        var lastEventName: String? = null
        var lastParameters: Map<Any?, *>? = null
        var lastCollectionEnabled: Boolean? = null
        var lastUserId: String? = null
        var lastUserPropertyName: String? = null
        var lastUserPropertyValue: String? = null
        var resetCalls = 0

        override fun logEvent(name: String, parameters: Map<Any?, *>?) {
            lastEventName = name
            lastParameters = parameters
        }

        override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
            lastCollectionEnabled = enabled
        }

        override fun setUserId(userId: String?) {
            lastUserId = userId
        }

        override fun setUserProperty(name: String, value: String?) {
            lastUserPropertyName = name
            lastUserPropertyValue = value
        }

        override fun resetAnalyticsData() {
            resetCalls += 1
        }
    }

    private fun sut(bridge: FakeAnalyticsBridge = FakeAnalyticsBridge()) =
        FirebaseAnalyticsIos(bridge)

    @Test
    fun logEvent_passesNullParameters_whenBundleIsEmpty() {
        val bridge = FakeAnalyticsBridge()

        sut(bridge).logEvent("screen_view")
        assertNull(bridge.lastParameters)
    }

    @Test
    fun logEvent_convertsPrimitiveBundleValues() {
        val bridge = FakeAnalyticsBridge()

        sut(bridge).logEvent(
            name = "screen_view",
            parameters = Bundle().apply {
                put("screen_name", "home")
                put("unread_count", 5)
                put("conversion_rate", 0.75)
                put("is_premium", true)
            }
        )

        assertEquals("screen_view", bridge.lastEventName)
        assertEquals("home", bridge.lastParameters?.get("screen_name"))
        assertEquals(5L, bridge.lastParameters?.get("unread_count"))
        assertEquals(0.75, bridge.lastParameters?.get("conversion_rate"))
        assertEquals(1L, bridge.lastParameters?.get("is_premium"))
    }

    @Test
    fun logEvent_serializesStructuredValues_toJsonStrings() {
        val bridge = FakeAnalyticsBridge()

        sut(bridge).logEvent(
            name = "favorite_toppings",
            parameters = Bundle().apply {
                put(
                    key = "toppings",
                    value = listOf("pepperoni", "mushrooms"),
                    serializer = ListSerializer(String.serializer())
                )
            }
        )

        assertEquals("[\"pepperoni\",\"mushrooms\"]", bridge.lastParameters?.get("toppings"))
    }

    @Test
    fun setAnalyticsCollectionEnabled_delegatesToBridge() {
        val bridge = FakeAnalyticsBridge()

        sut(bridge).setAnalyticsCollectionEnabled(false)
        assertEquals(false, bridge.lastCollectionEnabled)
    }

    @Test
    fun setUserId_delegatesToBridge() {
        val bridge = FakeAnalyticsBridge()

        sut(bridge).setUserId("user-42")
        assertEquals("user-42", bridge.lastUserId)
    }

    @Test
    fun setUserProperty_delegatesToBridge() {
        val bridge = FakeAnalyticsBridge()

        sut(bridge).setUserProperty("favorite_food", "pizza")
        assertEquals("favorite_food", bridge.lastUserPropertyName)
        assertEquals("pizza", bridge.lastUserPropertyValue)
    }

    @Test
    fun resetAnalyticsData_delegatesToBridge() {
        val bridge = FakeAnalyticsBridge()

        sut(bridge).resetAnalyticsData()
        assertEquals(1, bridge.resetCalls)
    }
}
