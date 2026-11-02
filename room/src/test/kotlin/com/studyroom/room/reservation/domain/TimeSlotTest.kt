package com.studyroom.room.reservation.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

class TimeSlotTest {
    @Test
    fun `끝 시각이 시작 시각보다 늦지 않으면 거절한다`() {
        assertThatThrownBy { TimeSlot(at("10:00"), at("10:00")) }
            .isInstanceOf(InvalidTimeSlotException::class.java)
            .hasFieldOrPropertyWithValue("code", "RESERVATION-400")
    }

    @Test
    fun `정시가 아닌 시각은 거절한다`() {
        assertThatThrownBy { TimeSlot(at("10:30"), at("11:00")) }
            .isInstanceOf(InvalidTimeSlotException::class.java)
    }

    @Test
    fun `일부라도 겹치면 겹친다고 본다`() {
        val slot = TimeSlot(at("10:00"), at("12:00"))

        assertThat(slot.overlaps(TimeSlot(at("11:00"), at("13:00")))).isTrue()
    }

    @Test
    fun `끝과 시작이 맞닿은 시간대는 겹치지 않는다`() {
        val slot = TimeSlot(at("10:00"), at("12:00"))

        assertThat(slot.overlaps(TimeSlot(at("12:00"), at("13:00")))).isFalse()
    }

    private fun at(time: String): Instant = Instant.parse("2026-11-02T$time:00Z")
}
