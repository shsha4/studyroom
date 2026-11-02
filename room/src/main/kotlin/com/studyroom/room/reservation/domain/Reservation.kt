package com.studyroom.room.reservation.domain

import java.time.Instant
import java.util.UUID

class Reservation(
    val id: UUID,
    val roomId: UUID,
    val memberId: UUID,
    val timeSlot: TimeSlot,
    status: ReservationStatus,
    cancelledAt: Instant?,
) {
    // private set = 읽기는 열려있지만 수정은 클래스 안에서만 허용
    var status: ReservationStatus = status
        private set

    var cancelledAt: Instant? = cancelledAt
        private set

    init {
        require((status == ReservationStatus.CANCELLED) == (cancelledAt != null)) {
            "취소된 예약만 취소 시각을 가진다"
        }
    }

    fun cancel(now: Instant): CancelResult =
        when (status) {
            ReservationStatus.CANCELLED -> {
                CancelResult.AlreadyCancelled
            }

            ReservationStatus.RESERVED -> {
                status = ReservationStatus.CANCELLED
                cancelledAt = now
                CancelResult.Cancelled(now)
            }
        }

    companion object {
        fun reserve(
            id: UUID,
            roomId: UUID,
            memberId: UUID,
            timeSlot: TimeSlot,
        ): Reservation = Reservation(id, roomId, memberId, timeSlot, ReservationStatus.RESERVED, null)
    }
}
