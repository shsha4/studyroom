package com.studyroom.room.reservation.application.service

import com.studyroom.room.reservation.application.port.out.ReservationRepositoryPort
import com.studyroom.room.reservation.domain.CancelResult
import com.studyroom.room.reservation.domain.Reservation
import com.studyroom.room.reservation.domain.ReservationStatus
import com.studyroom.room.reservation.domain.TimeSlot
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

// Spring 없이 저장소 포트만 mock 한다. 도메인 Reservation 은 진짜를 쓴다. 근거: 컨벤션 03 「1.」
class ReservationCancelServiceTest {
    private val reservations: ReservationRepositoryPort = mock()

    // 기대값에 취소 시각이 들어가서 시계를 고정한다. 시스템 시계면 실행할 때마다 값이 달라 비교를 못 한다.
    // 근거: 컨벤션 03 「2.」
    private val service = ReservationCancelService(reservations, Clock.fixed(NOW, ZoneOffset.UTC))

    @Test
    fun `내 예약을 취소하면 취소 시각을 담아 저장한다`() {
        whenever(reservations.findByIdAndMemberId(RESERVATION_ID, MEMBER_ID)).thenReturn(reserved())

        service.cancel(RESERVATION_ID, MEMBER_ID)

        // save() 에 넘어간 예약을 붙잡아 상태와 취소 시각을 본다.
        val saved = argumentCaptor<Reservation>()
        verify(reservations).save(saved.capture())
        assertThat(saved.firstValue)
            .extracting("status", "cancelledAt")
            .containsExactly(ReservationStatus.CANCELLED, NOW)
    }

    @Test
    fun `이미 취소된 예약을 다시 취소하면 저장하지 않고 이미 취소됨을 돌려준다`() {
        whenever(reservations.findByIdAndMemberId(RESERVATION_ID, MEMBER_ID)).thenReturn(cancelled())

        val result = service.cancel(RESERVATION_ID, MEMBER_ID)

        // 에러가 아니라 AlreadyCancelled 결과. 두 번 취소해도 안전하다(멱등).
        assertThat(result).isEqualTo(CancelResult.AlreadyCancelled)
        // when 의 AlreadyCancelled -> Unit 가지를 탔는지: save 가 한 번도 안 불렸어야 한다.
        verify(reservations, never()).save(any())
    }

    // stub 하지 않으면 mock 은 null 을 돌려준다. 없는 예약과 남의 예약은 포트에서 똑같이 null 이다.
    @Test
    fun `없거나 남의 예약이면 RESERVATION-404 로 거절한다`() {
        assertThatThrownBy { service.cancel(RESERVATION_ID, MEMBER_ID) }
            .isInstanceOf(ReservationNotFoundException::class.java)
            .extracting("code")
            .isEqualTo("RESERVATION-404")
    }

    private fun reserved(): Reservation = Reservation.reserve(RESERVATION_ID, 1L, MEMBER_ID, SLOT)

    // 취소된 상태는 생성자로 바로 만든다. cancel() 을 불러 만들면 준비 단계에서 도메인 로직을 한 번 더 타게 된다.
    private fun cancelled(): Reservation =
        Reservation(RESERVATION_ID, 1L, MEMBER_ID, SLOT, ReservationStatus.CANCELLED, NOW.minusSeconds(3600))

    // 이 파일에서만 쓰는 픽스처 값. 여러 테스트가 같이 쓰게 되면 Object Mother 로 옮긴다. 근거: 컨벤션 03 「2.」
    companion object {
        private val NOW: Instant = Instant.parse("2026-11-05T00:30:00Z")
        private val RESERVATION_ID: UUID = UUID.fromString("01a1a17e-bc80-7d74-b0dc-000000000002")
        private val MEMBER_ID: UUID = UUID.fromString("01a1a17e-bc80-7d74-b0dc-000000000001")
        private val SLOT = TimeSlot(Instant.parse("2026-11-05T01:00:00Z"), Instant.parse("2026-11-05T03:00:00Z"))
    }
}
