package org.birdview.storage

import org.assertj.core.api.Assertions.assertThat
import org.birdview.source.DocumentIdMapper
import org.birdview.source.SourceType
import org.junit.Test

class DocumentIdMapperTest {
    @Test
    fun testConfluenceMapping1() {
        val mappedId = DocumentIdMapper.map(SourceType.CONFLUENCE, "https://canvadev.atlassian.net/wiki/pages/viewpage.action?pageId=2369651567")
        assertThat(mappedId).isEqualTo("https://canvadev.atlassian.net/wiki:2369651567")
    }

    @Test
    fun testConfluenceMapping2() {
        val mappedId = DocumentIdMapper.map(SourceType.CONFLUENCE, "https://canvadev.atlassian.net/wiki/spaces/MAR/pages/2369651567/Switching+to+reserve+cache+cluster")
        assertThat(mappedId).isEqualTo("https://canvadev.atlassian.net/wiki:2369651567")
    }
}