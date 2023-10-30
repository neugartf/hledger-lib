package com.neugartf.hledger

import com.goncalossilva.resources.Resource
import kotlin.test.Test
import kotlin.test.assertEquals

class HledgerParserTest {

    @Test
    fun testParser() {
        // ASSIGN
        val content = Resource("src/commonTest/resources/test.journal").readText()

        // ACT
        val result = HledgerParser.parse(content)

        // ASSERT
        assertEquals(2, result.size)
        assertEquals(5, result[0].postings.size)
        assertEquals(2, result[1].postings.size)
    }
}