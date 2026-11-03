package com.studyroom.room.room.adapter.`in`.rest.dto

import java.util.UUID

data class RoomListResponse(
    val rooms: List<Item>,
) {
    // 중첩 클래스
    data class Item(
        val id: UUID,
        val name: String,
        val capacity: Int,
    )
    // RoomResult 를 그대로 내보내지 않고 HTTP용 모양을 따로 둔다
    // RoomResult 를 응답 그대로 쓰면, 나중에 서비스 쪽에 필드 하나만 더해도 그 필드가 API 응답에 그대로 나간다.
}
