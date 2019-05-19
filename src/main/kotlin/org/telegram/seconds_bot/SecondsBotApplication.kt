package org.telegram.seconds_bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SecondsBotApplication

fun main(args: Array<String>) {
    runApplication<SecondsBotApplication>(*args)
}
