package com.studyroom.room.reservation.domain

import com.studyroom.room.shared.domain.DomainException

class InvalidTimeSlotException(
    reason: String,
) : DomainException("RESERVATION-400", reason)
