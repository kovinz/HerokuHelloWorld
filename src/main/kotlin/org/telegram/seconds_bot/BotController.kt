package org.telegram.seconds_bot

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.schedule


@RestController
class BotController {
    companion object {
        private val API_ENDPOINT = "https://api.telegram.org/bot"
        private val START_COMMAND = "/start"
        private val WAIT_COMMAND = "/wait"
    }

    private val logger: Logger = Logger.getLogger("[EchoBot]")
    @Value("\${token}")
    lateinit var token: String


    /**
     * checks if user's message had any known command and calls appropriate functions
     */
    @PostMapping("/")
    fun onUpdate(@RequestBody update: Update) {
        logger.log(Level.INFO, "Got update: " + update)
        if (update.message != null) {
            val chatId = update.message.chat.id
            val text = update.message.text
            when {
                text?.startsWith(WAIT_COMMAND) == true -> onWaitCommand(chatId, text)
                text?.startsWith(START_COMMAND) == true -> onStartCommand(chatId)
            }
        }
    }


    /**
     * sends hello message on /start
     */
    private fun onStartCommand(chatId: Long) = try {
        sendMessage(chatId, "Hello! I'm Seconds Waiting Bot.")
    } catch (e: UnirestException) {
        logger.log(Level.SEVERE, "Can not send START response!", e)
    }


    /**
     * chatId - id of chat
     * text - text for bot to write to chat
     *
     * checks whether text has only digits and ':'
     * chooses appropriate answer to user's request
     * if format of request is correct then sends approval of request, waits required amount of seconds
     * and sends message with confirmation of waiting for required time
     * if format of request isn't corrent then sends denial of request
     */
    private fun onWaitCommand(chatId: Long, text: String) = try {
        val textWithoutCommand = text.subSequence(WAIT_COMMAND.length, text.length).trim().toString()

        if (textWithoutCommand.isNotEmpty().and(textWithoutCommand.all { c: Char -> c.isDigit().or(c == ':') })) {
            val listOfTimes: List<Long> = textWithoutCommand.split(":").map { s: String -> s.toLong() }

            val seconds: Long = countSeconds(listOfTimes)
            if (seconds == (-1).toLong()) {
                denyWaiting(chatId)
            } else {
                acceptWaiting(chatId, seconds)
                finishWaiting(chatId, seconds)
            }
        } else {
            denyWaiting(chatId)
        }
    } catch (e: UnirestException) {
        logger.log(Level.SEVERE, "Can not wait!", e)
    }


    /**
     * chatId - id of chat
     * text - text for bot to write to chat
     *
     * bot sends message with text to chat of chatId
     */
    @Throws(UnirestException::class)
    private fun sendMessage(chatId: Long, text: String) {
        Unirest.post(API_ENDPOINT + token + "/sendMessage")
                .field("chat_id", chatId)
                .field("text", text)
                .asJson()
    }

    /**
     * listOfTimes: list with hours minutes and seconds (some of it may be absent)
     *
     * check if time is sent in the correct format
     * (no more than 3 elements, hours in range 0..23, minutes and seconds in range 0..59,
     * if there is only one element is list then it counts as seconds and should be in range 0..oo)
     * and counts amount of seconds
     *
     * returns amount of seconds or -1 if format is not correct
     */
    private fun countSeconds(listOfTimes: List<Long>): Long {
        var secondsToWait: Long = 0
        var h: Long = 0
        var m: Long = 0
        var s: Long = 0
        var wrongFormat = false

        if (listOfTimes.size == 3) {
            if (listOfTimes[0] in 0..23
                    && listOfTimes[1] in 0..59
                    && listOfTimes[2] in 0..59) {
                h = listOfTimes[0]
                m = listOfTimes[1]
                s = listOfTimes[2]
            } else {
                wrongFormat = true
            }
        } else if (listOfTimes.size == 2) {
            if (listOfTimes[0] in 0..59
                    && listOfTimes[1] in 0..59) {
                m = listOfTimes[0]
                s = listOfTimes[1]
            } else {
                wrongFormat = true
            }
        } else if (listOfTimes.size == 1) {
            if (listOfTimes[0] >= 0) {
                s = listOfTimes[0]
            } else {
                wrongFormat = true
            }
        } else {
            wrongFormat = true
        }

        if (wrongFormat) {
            return -1
        }

        secondsToWait += h * 3600 * 1000
        secondsToWait += m * 60 * 1000
        secondsToWait += s * 1000
        return secondsToWait / 1000
    }


    /**
     * bot sends message with acceptance of waiting
     */
    private fun acceptWaiting(chatId: Long, secondsToWait: Long) {
        val response = "I will wait for " + secondsToWait + " seconds!"
        sendMessage(chatId, response)
    }


    /**
     * bot sends message of finishing of waiting
     */
    private fun finishWaiting(chatId: Long, secondsToWait: Long) {
        Timer("timer", false).schedule(secondsToWait * 1000) {
            val response = "I've been waiting for " + secondsToWait + " seconds!"
            sendMessage(chatId, response)
        }
    }


    /**
     * bot sends message with denial of waiting
     */
    private fun denyWaiting(chatId: Long) {
        val response = "Wrong format of time. You should use hh:mm:ss (hh in range 0..23, mm in range 0..59, ss in range 0..59)" +
                " or just quantity of seconds I should wait in range 0..oo"
        sendMessage(chatId, response)
    }
}