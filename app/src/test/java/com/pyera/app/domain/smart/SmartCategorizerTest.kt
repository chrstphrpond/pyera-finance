package com.pyera.app.domain.smart

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmartCategorizerTest {

    private val smartCategorizer = SmartCategorizer()

    @Test
    fun `predict returns Transport for uber`() {
        val result = smartCategorizer.predict("Uber ride to work")
        assertEquals("Transport", result)
    }

    @Test
    fun `predict returns Food for starbucks`() {
        val result = smartCategorizer.predict("Starbucks coffee")
        assertEquals("Food", result)
    }

    @Test
    fun `predict returns Entertainment for netflix`() {
        val result = smartCategorizer.predict("Netflix subscription")
        assertEquals("Entertainment", result)
    }

    @Test
    fun `predict returns null for unknown keyword`() {
        val result = smartCategorizer.predict("Random expense")
        assertNull(result)
    }

    @Test
    fun `predict is case insensitive`() {
        val result = smartCategorizer.predict("uBeR")
        assertEquals("Transport", result)
    }
}
