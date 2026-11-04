package com.studyroom.room.reservation.adapter.out.persistence

import com.studyroom.room.reservation.application.port.out.ReservationRepositoryPort
import com.studyroom.room.reservation.domain.Reservation
import com.studyroom.room.reservation.domain.ReservationStatus
import com.studyroom.room.reservation.domain.TimeSlot
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ReservationPersistenceAdapter(
    private val repository: ReservationJpaRepository,
) : ReservationRepositoryPort {
    override fun existsOverlapping(
        roomId: Long,
        timeSlot: TimeSlot,
    ): Boolean = repository.existsOverlapping(roomId, ReservationStatus.RESERVED, timeSlot.startAt, timeSlot.endAt)

    override fun findByIdAndMemberId(
        id: UUID,
        memberId: UUID,
    ): Reservation? = repository.findByUuidAndMemberUuid(id, memberId)?.toDomain()

    override fun save(reservation: Reservation) {
        val entity = repository.findByUuid(reservation.id)
        if (entity == null) {
            repository.save(ReservationEntity.from(reservation))
        } else {
            entity.changeStatus(reservation.status, reservation.cancelledAt)
        }
    }
}
