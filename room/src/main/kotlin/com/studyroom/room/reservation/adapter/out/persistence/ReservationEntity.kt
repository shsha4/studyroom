package com.studyroom.room.reservation.adapter.out.persistence

import com.studyroom.room.reservation.domain.Reservation
import com.studyroom.room.reservation.domain.ReservationStatus
import com.studyroom.room.shared.adapter.out.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "reservation")
class ReservationEntity protected constructor(
    uuid: UUID,
    roomId: Long,
    memberUuid: UUID,
    startAt: Instant,
    endAt: Instant,
    reservationStatus: ReservationStatus,
    cancelledAt: Instant?,
    // created_at, updated_at 은 BaseEntity 와 Auditing 이 채운다.
) : BaseEntity() {
    // INSERT 전에는 id 가 없어서 Long?
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Column(nullable = false, updatable = false)
    var uuid: UUID = uuid
        protected set

    @Column(nullable = false, updatable = false)
    var roomId: Long = roomId
        protected set

    @Column(nullable = false, updatable = false)
    var memberUuid: UUID = memberUuid
        protected set

    @Column(nullable = false, updatable = false)
    var startAt: Instant = startAt
        protected set

    @Column(nullable = false, updatable = false)
    var endAt: Instant = endAt
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var reservationStatus: ReservationStatus = reservationStatus
        protected set

    // DDL 이 NULL 이고, @Column 기본값도 nullable = true 라서 생략
    var cancelledAt: Instant? = cancelledAt
        protected set

    fun changeStatus(
        reservationStatus: ReservationStatus,
        cancelledAt: Instant?,
    ) {
        this.reservationStatus = reservationStatus
        this.cancelledAt = cancelledAt
    }

    // 같은 행이면 같은 객체, 필드가 바뀌어도 id 가 같으면 같다.
    // is 검사라서 Hibernate 프록시도 같다고 본다.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReservationEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = ReservationEntity::class.hashCode()

    companion object {
        fun from(reservation: Reservation): ReservationEntity =
            ReservationEntity(
                uuid = reservation.id,
                roomId = reservation.roomId,
                memberUuid = reservation.memberId,
                startAt = reservation.timeSlot.startAt,
                endAt = reservation.timeSlot.endAt,
                reservationStatus = reservation.status,
                cancelledAt = reservation.cancelledAt,
            )
    }
}
