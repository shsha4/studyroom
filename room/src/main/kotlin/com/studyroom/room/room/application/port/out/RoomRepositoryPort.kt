package com.studyroom.room.room.application.port.out

import com.studyroom.room.room.application.command.RoomResult
import java.util.UUID

interface RoomRepositoryPort {
    fun findAll(): List<RoomResult>

    fun lockIdByUuid(uuid: UUID): Long?
}
