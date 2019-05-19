package org.telegram.seconds_bot

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@SpringBootApplication
class SecondsBotApplication

@Configuration
class ObjectMapperConfiguration {
    @Bean
    @Primary
    fun objectMapper() = ObjectMapper().apply {
        registerModule(KotlinModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}

fun main(args: Array<String>) {
    runApplication<SecondsBotApplication>(*args)
}
