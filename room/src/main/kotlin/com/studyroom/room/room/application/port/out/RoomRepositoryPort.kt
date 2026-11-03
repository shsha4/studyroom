package com.studyroom.room.room.application.port.out

import com.studyroom.room.room.application.command.RoomResult

interface RoomRepositoryPort {
    fun findAll(): List<RoomResult>
}
