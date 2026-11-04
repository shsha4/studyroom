package com.studyroom.room.reservation.application.service

import com.studyroom.room.reservation.application.command.ReservationResult
import com.studyroom.room.reservation.application.port.out.ReservationRepositoryPort
import com.studyroom.room.reservation.domain.Reservation
import com.studyroom.room.reservation.domain.TimeSlot
import com.studyroom.room.room.application.port.`in`.LockRoomUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ReservationCreateService(
    // 다른 기능(room)은 port/in 계약으로만 부른다. (유즈케이스)
    private val lockRoom: LockRoomUseCase,
    private val reservations: ReservationRepositoryPort,
    private val idGenerator: ReservationIdGenerator,
) {
    @Transactional
    fun reserve(
        roomId: UUID,
        memberId: UUID,
        timeSlot: TimeSlot,
    ): ReservationResult {
        val internalRoomId = lockRoom.lock(roomId)

        if (reservations.existsOverlapping(internalRoomId, timeSlot)) {
            throw ReservationOverlapException(roomId)
        }

        val reservation = Reservation.reserve(idGenerator.generate(), internalRoomId, memberId, timeSlot)
        reservations.save(reservation)

        return ReservationResult(
            id = reservation.id,
            roomId = roomId,
            startAt = reservation.timeSlot.startAt,
            endAt = reservation.timeSlot.endAt,
            status = reservation.status,
        )
    }
}
