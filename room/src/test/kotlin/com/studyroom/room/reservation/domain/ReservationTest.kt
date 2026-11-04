package com.studyroom.room.reservation.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ReservationTest {
    private val now = Instant.parse("2026-11-02T09:00:00Z")

    @Test
    fun `새 예약은 예약됨 상태로 시작한다`() {
        val reservation = reserve()

        assertThat(reservation.status).isEqualTo(ReservationStatus.RESERVED)
    }

    @Test
    fun `예약을 취소하면 취소 시각을 담은 결과를 돌려준다`() {
        val reservation = reserve()

        assertThat(reservation.cancel(now)).isEqualTo(CancelResult.Cancelled(now))
    }

    @Test
    fun `예약을 취소하면 취소됨 상태가 되고 취소 시각이 남는다`() {
        val reservation = reserve()

        reservation.cancel(now)

        assertThat(reservation)
            .extracting("status", "cancelledAt")
            .containsExactly(ReservationStatus.CANCELLED, now)
    }

    @Test
    fun `취소 시각 없이 취소됨 상태로는 만들 수 없다`() {
        assertThatThrownBy {
            Reservation(
                UUID.randomUUID(),
                1L,
                UUID.randomUUID(),
                slot(),
                ReservationStatus.CANCELLED,
                null,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `이미 취소된 예약을 다시 취소하면 처음 취소 시각을 지킨다`() {
        val reservation = reserve()
        reservation.cancel(now)

        val result = reservation.cancel(now.plusSeconds(60))

        assertThat(result).isEqualTo(CancelResult.AlreadyCancelled)
        assertThat(reservation.cancelledAt).isEqualTo(now)
    }

    private fun reserve(): Reservation = Reservation.reserve(UUID.randomUUID(), 1L, UUID.randomUUID(), slot())

    private fun slot(): TimeSlot =
        TimeSlot(Instant.parse("2026-11-03T10:00:00Z"), Instant.parse("2026-11-03T12:00:00Z"))
}
