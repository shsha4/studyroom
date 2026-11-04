package com.studyroom.room.reservation.application.service

import com.studyroom.room.shared.application.ApplicationException
import java.util.UUID

class ReservationNotFoundException(
    reservationId: UUID,
) : ApplicationException("RESERVATION-404", "예약을 찾을 수 없다: $reservationId")
