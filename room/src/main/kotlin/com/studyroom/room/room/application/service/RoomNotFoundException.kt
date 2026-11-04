package com.studyroom.room.room.application.service

import com.studyroom.room.shared.application.ApplicationException
import java.util.UUID

class RoomNotFoundException(
    roomId: UUID,
) : ApplicationException("ROOM-404", "방을 찾을 수 없다: $roomId")
