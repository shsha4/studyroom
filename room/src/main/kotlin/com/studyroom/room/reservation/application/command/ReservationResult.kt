package com.studyroom.room.reservation.application.command

import com.studyroom.room.reservation.domain.ReservationStatus
import java.time.Instant
import java.util.UUID

data class ReservationResult(
    val id: UUID,
    val roomId: UUID,
    val startAt: Instant,
    val endAt: Instant,
    val status: ReservationStatus,
)
