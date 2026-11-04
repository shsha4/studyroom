package com.studyroom.room.reservation.application.service

import com.studyroom.room.reservation.domain.UuidV7
import org.springframework.stereotype.Component
import java.time.Clock
import java.util.UUID

@Component
class ReservationIdGenerator(
    private val clock: Clock,
) {
    fun generate(): UUID = UuidV7.generate(clock.instant())
}
