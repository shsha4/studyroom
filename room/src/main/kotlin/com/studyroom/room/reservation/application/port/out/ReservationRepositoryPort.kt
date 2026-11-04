package com.studyroom.room.reservation.application.port.out

import com.studyroom.room.reservation.domain.Reservation
import com.studyroom.room.reservation.domain.TimeSlot
import java.util.UUID

interface ReservationRepositoryPort {
    // 이 방에 겹치는 예약이 있나 취소된 예약은 빼는 것까지 이 메서드 책임
    // 반드시 LockRoomUseCase.lock() 다음에 부른다.
    fun existsOverlapping(
        roomId: Long,
        timeSlot: TimeSlot,
    ): Boolean

    // 내 예약만 찾는다. 남의 예약이면 없는 것과 똑같다.
    // id 로 찾고 회원을 따로 비교해 403 을 주면 "그 id 의 예약이 있다"는 사실이 새어 나간다.
    fun findByIdAndMemberId(
        id: UUID,
        memberId: UUID,
    ): Reservation?

    // 새 예약이면 INSERT, 있던 예약이면 상태만 바꾼다.
    // id(uuid)를 앱이 만들어서 DB 가 채워 줄 값이 없어 반환값이 없다.
    fun save(reservation: Reservation)
}
