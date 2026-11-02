package com.studyroom.room.reservation.domain

import java.time.Instant

sealed interface CancelResult {
    data class Cancelled(
        val cancelledAt: Instant,
    ) : CancelResult

    data object AlreadyCancelled : CancelResult
}
