package com.studyroom.room.reservation.application.service

import com.studyroom.room.shared.application.ApplicationException
import java.util.UUID

class ReservationOverlapException(
    roomId: UUID,
) : ApplicationException("RESERVATION-409", "이미 겹치는 예약이 있다 : $roomId")
