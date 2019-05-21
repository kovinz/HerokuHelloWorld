package org.telegram.seconds_bot

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.beans.factory.annotation.Autowired



@RunWith(SpringRunner::class)
@SpringBootTest
class CountSecondsTests {

    @Autowired
    private val botController: BotController = BotController()

    @Test
    fun correctSeconds() {
        val listOfTimes: List<Long> = "60".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(60);
    }

    @Test
    fun correctManySeconds() {
        val listOfTimes: List<Long> = "9000".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(9000);
    }

    @Test
    fun wrongSeconds() {
        val listOfTimes: List<Long> = "00:60".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(-1);
    }

    @Test
    fun correctMinutes() {
        val listOfTimes: List<Long> = "02:00".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(120);
    }

    @Test
    fun wrongMinutes() {
        val listOfTimes: List<Long> = "60:00".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(-1);
    }

    @Test
    fun correctHours() {
        val listOfTimes: List<Long> = "01:00:00".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(3600);
    }

    @Test
    fun wrongHours() {
        val listOfTimes: List<Long> = "24:00:00".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(-1);
    }

    @Test
    fun allCorrectTimes() {
        val listOfTimes: List<Long> = "05:10:50".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(18650);
    }

    @Test
    fun allWrongTimes() {
        val listOfTimes: List<Long> = "50:100:74".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(-1);
    }

    @Test
    fun tooManyTimes() {
        val listOfTimes: List<Long> = "00:12:30:30".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(-1);
    }

    @Test
    fun allTimesZeros() {
        val listOfTimes: List<Long> = "00:00:00".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(0);
    }

    @Test
    fun zeroSeconds() {
        val listOfTimes: List<Long> = "0".split(":").map { s: String -> s.toLong() }
        assertThat(botController.countSeconds(listOfTimes))
                .isEqualTo(0);
    }
}
