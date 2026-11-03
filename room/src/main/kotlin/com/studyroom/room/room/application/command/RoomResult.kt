package com.studyroom.room.room.application.command

import java.util.UUID

// 데이터 클래스는 equals, hashCode, toString, copy 를 자동으로 만들어 준다.
// id 는 Long 이 아니라 UUID 다
// AUTO_INCREMENT 값인 id 는 DB 안에서만 쓰는 번호다. 외부 공개는 UUID
data class RoomResult(
    val id: UUID,
    val name: String,
    val capacity: Int,
)
