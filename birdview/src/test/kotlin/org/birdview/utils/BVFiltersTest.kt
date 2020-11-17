package org.birdview.utils

import org.junit.Test

import org.junit.Assert.*

class BVFiltersTest {
    private val ref = "https://some.atlassian.net/wiki/spaces/SOM/pages/7152030627/Design+Some+Feature"

    @Test
    fun shouldExtractSimpleLink() {
        val text = "  $ref"
        val ids = BVFilters.filterIdsFromText(text)
        assertFalse(ids.isEmpty())
        assertEquals(ids.first(), ref)
    }

    @Test
    fun shouldExtractSmartLink() {
        val text = "some text [$ref|$ref|smart-link]" +
                " wit smartlink"
        val ids = BVFilters.filterIdsFromText(text)
        assertFalse(ids.isEmpty())
        assertEquals(ref, ids.first())
    }
}