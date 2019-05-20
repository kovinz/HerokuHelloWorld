package org.telegram.seconds_bot

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.lang.Thread.sleep
import java.util.logging.Level
import java.util.logging.Logger


@RestController
class BotController {
    companion object {
        private val API_ENDPOINT = "https://api.telegram.org/bot"
        private val START_COMMAND = "/start"
        private val ECHO_COMMAND = "/echo"
        private val WAIT_COMMAND = "/wait"
    }

    private val logger: Logger = Logger.getLogger("[EchoBot]")
    @Value("\${token}")
    lateinit var token: String

    @PostMapping("/")
    fun onUpdate(@RequestBody update: Update) {
        logger.log(Level.INFO, "Got update: " + update)
        if (update.message != null) {
            val chatId = update.message.chat.id
            val text= update.message.text
            when {
                text?.startsWith(WAIT_COMMAND) == true -> onWaitCommand(chatId, text)
                text?.startsWith(START_COMMAND) == true -> onStartCommand(chatId)
                text?.startsWith(ECHO_COMMAND) == true -> onEchoCommand(chatId, text)
            }
        }
    }

    private fun onStartCommand(chatId: Long) = try {
        sendMessage(chatId, "Hello! I'm EchoBot.")
    } catch (e: UnirestException) {
        logger.log(Level.SEVERE, "Can not send START response!", e)
    }

    private fun onEchoCommand(chatId: Long, text: String) = try {
        val response = text.subSequence(ECHO_COMMAND.length, text.length).trim().toString()
        sendMessage(chatId, response)
    } catch (e: UnirestException) {
        logger.log(Level.SEVERE, "Can not send ECHO response!", e)
    }

    private fun onWaitCommand(chatId: Long, text: String) = try {
        val textWithoutCommand = text.subSequence(WAIT_COMMAND.length, text.length).trim().toString()
        if (textWithoutCommand.all { c: Char -> c.isDigit().or(c == ':') }.and(textWithoutCommand.isNotEmpty())){
            val listOfTimes: List<Long> = textWithoutCommand.split(":").map { s: String -> s.toLong() }
            var secondsToWait: Long = 0
            if (listOfTimes.size == 3) {
                val h = listOfTimes.get(0)
                val m = listOfTimes.get(1)
                val s = listOfTimes.get(2)
                if (h >= 0 && h < 24
                        && m >= 0 && m < 60
                        && s >= 0 && s < 60){
                    secondsToWait += listOfTimes.get(0) * 3600 * 1000
                    secondsToWait += listOfTimes.get(1) * 60 * 1000
                    secondsToWait += listOfTimes.get(2) * 1000
                    sleep(secondsToWait)
                    val response = "I've been waiting for " + secondsToWait / 1000 + " seconds!"
                    sendMessage(chatId, response)
                } else {
                    val response = "Wrong format of time. You should use hh:mm:ss or just quantity of seconds I should wait"
                    sendMessage(chatId, response)
                }
            } else if (listOfTimes.size == 2){
                val m = listOfTimes.get(0)
                val s = listOfTimes.get(1)
                if (m >= 0 && m < 60
                        && s >= 0 && s < 60){
                    secondsToWait += listOfTimes.get(0) * 60 * 1000
                    secondsToWait += listOfTimes.get(1) * 1000
                    sleep(secondsToWait)
                    val response = "I've been waiting for " + secondsToWait / 1000 + " seconds!"
                    sendMessage(chatId, response)
                } else {
                    val response = "Wrong format of time. You should use hh:mm:ss or just quantity of seconds I should wait"
                    sendMessage(chatId, response)
                }
            } else {
                secondsToWait +=listOfTimes.get(0) * 1000
                sleep(secondsToWait)
                val response = "I've been waiting for " + secondsToWait / 1000 + " seconds!"
                sendMessage(chatId, response)
            }
        } else {
            val response = "Wrong format of time. You should use hh:mm:ss or just quantity of seconds I should wait"
            sendMessage(chatId, response)
        }
    } catch (e: UnirestException) {
        logger.log(Level.SEVERE, "Can not wait!", e)
    }

    @Throws(UnirestException::class)
    private fun sendMessage(chatId: Long, text: String) {
        Unirest.post(API_ENDPOINT + token + "/sendMessage")
                .field("chat_id", chatId)
                .field("text", text)
                .asJson()
    }
}