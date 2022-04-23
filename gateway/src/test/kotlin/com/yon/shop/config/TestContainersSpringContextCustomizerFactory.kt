package com.yon.shop.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.ContextCustomizerFactory
import org.testcontainers.containers.KafkaContainer
import java.util.*

class TestContainersSpringContextCustomizerFactory : ContextCustomizerFactory {

    private val log = LoggerFactory.getLogger(TestContainersSpringContextCustomizerFactory::class.java)

    companion object {
        @JvmStatic
        private var kafkaBean: KafkaTestContainer? = null
    }

    override fun createContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer {
        return ContextCustomizer { context, _ ->
            val beanFactory = context.beanFactory
            var testValues = TestPropertyValues.empty()

            val kafkaAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedKafka::class.java)
            if (null != kafkaAnnotation) {
                log.debug("detected the EmbeddedKafka annotation on class {}", testClass.name)
                if (kafkaBean == null) {
                    log.info("Warming up the kafka broker")
                    kafkaBean = KafkaTestContainer()
                    beanFactory.initializeBean(kafkaBean, KafkaTestContainer::class.java.name.lowercase(Locale.getDefault()))
                    beanFactory.registerSingleton(KafkaTestContainer::class.java.name.lowercase(Locale.getDefault()), kafkaBean)
                    (beanFactory as (DefaultSingletonBeanRegistry)).registerDisposableBean(KafkaTestContainer::class.java.name.lowercase(Locale.getDefault()), kafkaBean)
                }
                kafkaBean?.let {
                    testValues = testValues.and("spring.cloud.stream.kafka.binder.brokers=" + it.getKafkaContainer().host + ':' + it.getKafkaContainer().getMappedPort(KafkaContainer.KAFKA_PORT))
                }
            }
            testValues.applyTo(context)
        }
    }
}
