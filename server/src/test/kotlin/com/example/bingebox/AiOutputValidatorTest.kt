package com.example.bingebox

import com.example.bingebox.groq.AiOutputValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AiOutputValidatorTest {
    @Test fun `accepts a bounded structured text response`() {
        assertEquals("Try Arrival.", AiOutputValidator().textFrom("{\"text\":\"Try Arrival.\"}"))
    }

    @Test fun `rejects unstructured or empty provider output`() {
        assertNull(AiOutputValidator().textFrom("Try Arrival."))
        assertNull(AiOutputValidator().textFrom("{\"text\":\"\"}"))
    }
}