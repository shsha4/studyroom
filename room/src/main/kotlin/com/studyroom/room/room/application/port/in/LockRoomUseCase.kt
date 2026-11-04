package com.studyroom.room.room.application.port.`in`

import java.util.UUID

interface LockRoomUseCase {
    fun lock(roomId: UUID): Long
}
