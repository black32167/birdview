package org.birdview.utils

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory

class LazyBeanFactoryPostProcessor: BeanFactoryPostProcessor {
    val log = LoggerFactory.getLogger(LazyBeanFactoryPostProcessor::class.java)
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        for (beanName in beanFactory.beanDefinitionNames) {
            if(beanFactory.isSingleton(beanName)) {
                val beanDefinition = beanFactory.getBeanDefinition(beanName)
                log.info("Making bean lazy: {}", beanName)
                beanDefinition.isLazyInit = true
            }
        }
    }
}
