package com.studyroom.room.room.adapter.`in`.rest

import com.studyroom.room.room.adapter.`in`.rest.dto.RoomListResponse
import com.studyroom.room.room.application.command.RoomResult

// 확장 함수
fun List<RoomResult>.toResponse(): RoomListResponse = RoomListResponse(rooms = map { it.toItem() })

// 확장 함수 : 기존 타입에 함수를 붙인 것 처럼 사용한다.
private fun RoomResult.toItem(): RoomListResponse.Item =
    RoomListResponse.Item(
        id = id,
        name = name,
        capacity = capacity,
    )
