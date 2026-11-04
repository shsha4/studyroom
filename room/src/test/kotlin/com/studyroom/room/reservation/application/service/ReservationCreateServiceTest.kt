package com.studyroom.room.reservation.application.service

import com.studyroom.room.reservation.application.command.ReservationResult
import com.studyroom.room.reservation.application.port.out.ReservationRepositoryPort
import com.studyroom.room.reservation.domain.Reservation
import com.studyroom.room.reservation.domain.ReservationStatus
import com.studyroom.room.reservation.domain.TimeSlot
import com.studyroom.room.room.application.port.`in`.LockRoomUseCase
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

// Spring 없이 생성자에 mock 을 직접 넣는다.
// 도메인은 mock 하지 않고 진짜를 쓴다.
class ReservationCreateServiceTest {
    // JUnit 5 는 테스트 메서드마다 이 클래스를 새로 만든다.
    private val lockRoom: LockRoomUseCase = mock()
    private val reservations: ReservationRepositoryPort = mock()
    private val idGenerator: ReservationIdGenerator = mock()
    private val service = ReservationCreateService(lockRoom, reservations, idGenerator)

    @BeforeEach
    fun setUp() {
        whenever(lockRoom.lock(ROOM_UUID)).thenReturn(ROOM_ID)

        whenever(idGenerator.generate()).thenReturn(RESERVATION_ID)
    }

    @Test
    fun `겹치는 예약이 없으면 잠근 방 id 로 새 예약을 저장한다`() {
        service.reserve(ROOM_UUID, MEMBER_ID, SLOT)

        // save() 에 실제로 넘어간 인자를 꺼내 본다.
        val saved = argumentCaptor<Reservation>()
        verify(reservations).save(saved.capture())

        assertThat(saved.firstValue)
            .extracting("id", "roomId", "memberId", "timeSlot", "status")
            .containsExactly(RESERVATION_ID, ROOM_ID, MEMBER_ID, SLOT, ReservationStatus.RESERVED)
    }

    @Test
    fun `예약 결과에는 내부 방 id 가 아니라 요청한 방 uuid 를 담는다`() {
        val result = service.reserve(ROOM_UUID, MEMBER_ID, SLOT)

        assertThat(result).isEqualTo(
            ReservationResult(RESERVATION_ID, ROOM_UUID, SLOT.startAt, SLOT.endAt, ReservationStatus.RESERVED),
        )
    }

    @Test
    fun `겹치는 예약이 있으면 RESERVATION-409 로 거절하고 저장하지 않는다`() {
        whenever(reservations.existsOverlapping(ROOM_ID, SLOT)).thenReturn(true)

        assertThatThrownBy { service.reserve(ROOM_UUID, MEMBER_ID, SLOT) }
            .isInstanceOf(ReservationOverlapException::class.java)
            .extracting("code")
            .isEqualTo("RESERVATION-409")

        verify(reservations, never()).save(any())
    }

    @Test
    fun `방을 먼저 잠그고 그다음에 겹침을 조회한다`() {
        service.reserve(ROOM_UUID, MEMBER_ID, SLOT)

        val order = inOrder(lockRoom, reservations)
        order.verify(lockRoom).lock(ROOM_UUID)
        order.verify(reservations).existsOverlapping(ROOM_ID, SLOT)
    }

    companion object {
        private val ROOM_UUID: UUID = UUID.fromString("01a1a17e-bc80-7d74-b0dc-9e4f66d13344")
        private const val ROOM_ID = 1L
        private val MEMBER_ID: UUID = UUID.fromString("01a1a17e-bc80-7d74-b0dc-000000000001")
        private val RESERVATION_ID: UUID = UUID.fromString("01a1a17e-bc80-7d74-b0dc-000000000002")
        private val SLOT = TimeSlot(Instant.parse("2026-11-05T01:00:00Z"), Instant.parse("2026-11-05T03:00:00Z"))
    }
}
