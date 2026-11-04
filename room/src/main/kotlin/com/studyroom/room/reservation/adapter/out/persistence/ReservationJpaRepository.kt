package com.studyroom.room.reservation.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReservationJpaRepository : JpaRepository<ReservationEntity, Long> {
    fun findByUuid(uuid: UUID): ReservationEntity?
}
