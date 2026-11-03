package com.studyroom.room.room.application.service

import com.studyroom.room.room.application.command.RoomResult
import com.studyroom.room.room.application.port.out.RoomRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoomQueryService(
    private val rooms: RoomRepositoryPort,
) {
    @Transactional(readOnly = true)
    fun findAll(): List<RoomResult> = rooms.findAll()
}
