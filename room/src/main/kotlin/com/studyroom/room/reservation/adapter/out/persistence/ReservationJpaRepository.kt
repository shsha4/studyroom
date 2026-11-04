package com.studyroom.room.reservation.adapter.out.persistence

import com.studyroom.room.reservation.domain.ReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface ReservationJpaRepository : JpaRepository<ReservationEntity, Long> {
    fun findByUuid(uuid: UUID): ReservationEntity?

    // 메서드 이름 쿼리로 쓰면 existsByRoomIdAndReservationStatusAndStartAtLessThanAndEndAtGreaterThan(.., endAt, startAt)
    // JPQL 로 작성
    @Query(
        """
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM ReservationEntity r
        WHERE r.roomId = :roomId
            AND r.reservationStatus = :reservationStatus
            AND r.startAt < :endAt
            AND r.endAt > :startAt
        """,
    )
    fun existsOverlapping(
        roomId: Long,
        reservationStatus: ReservationStatus,
        startAt: Instant,
        endAt: Instant,
    ): Boolean

    fun findByUuidAndMemberUuid(
        uuid: UUID,
        memberUuid: UUID,
    ): ReservationEntity?
}
