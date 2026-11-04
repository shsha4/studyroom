package com.studyroom.room.reservation.adapter.out.persistence

import com.studyroom.room.reservation.domain.Reservation
import com.studyroom.room.reservation.domain.TimeSlot

fun ReservationEntity.toDomain(): Reservation =
    Reservation(
        id = uuid,
        roomId = roomId,
        memberId = memberUuid,
        timeSlot = TimeSlot(startAt, endAt),
        status = reservationStatus,
        cancelledAt = cancelledAt,
    )
