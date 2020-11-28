package org.birdview.utils

import org.junit.Test

import org.junit.Assert.*

class BVFiltersTest {
    private val ref = "https://some.atlassian.net/wiki/spaces/SOM/pages/7152030627/Design+Some+Feature"

    @Test
    fun shouldExtractSimpleLink() {
        val text = "  $ref"
        val ids = BVFilters.filterRefsFromText(text)
        assertEquals(ids.first().id, ref)
    }

    @Test
    fun shouldExtractSmartLink() {
        val text = "some text [$ref|$ref|smart-link]" +
                " with smartlink"
        val ids = BVFilters.filterRefsFromText(text)
        assertFalse(ids.isEmpty())
        assertEquals(ref, ids.first().id)
    }
}