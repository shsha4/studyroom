package com.studyroom.room.reservation.application.service

import com.studyroom.room.reservation.application.port.out.ReservationRepositoryPort
import com.studyroom.room.reservation.domain.CancelResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class ReservationCancelService(
    private val reservations: ReservationRepositoryPort,
    private val clock: Clock,
) {
    @Transactional
    fun cancel(
        reservationId: UUID,
        memberId: UUID,
    ): CancelResult {
        val reservation =
            reservations.findByIdAndMemberId(reservationId, memberId)
                ?: throw ReservationNotFoundException(reservationId)

        val result = reservation.cancel(clock.instant())

        when (result) {
            is CancelResult.Cancelled -> reservations.save(reservation)
            CancelResult.AlreadyCancelled -> Unit
        }

        return result
    }
}
