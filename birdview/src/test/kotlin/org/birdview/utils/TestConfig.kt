package org.birdview.utils

import org.birdview.user.BVLoggedUserSettingsProvider
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@Import(LazyBeanFactoryPostProcessor::class)
@TestConfiguration
open class TestConfig {
    @Bean
    open fun loggedUserSettingsProvider(): BVLoggedUserSettingsProvider =
        mock(BVLoggedUserSettingsProvider::class.java).also {
            `when`(it.getTimezoneId()).thenReturn("UTC");
        }
}
