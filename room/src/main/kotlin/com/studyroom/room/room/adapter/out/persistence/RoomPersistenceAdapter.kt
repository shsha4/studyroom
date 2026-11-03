package com.studyroom.room.room.adapter.out.persistence

import com.studyroom.room.room.application.command.RoomResult
import com.studyroom.room.room.application.port.out.RoomRepositoryPort
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

@Repository
class RoomPersistenceAdapter(
    private val jdbcClient: JdbcClient,
) : RoomRepositoryPort {
    override fun findAll(): List<RoomResult> =
        jdbcClient
            // 실행할 SQL
            .sql("SELECT uuid, room_name, capacity FROM room ORDER BY id")
            // 결과 한 줄 (rs) 를 RoomResult 하나로 바꾸는 방법. _ 는 몇 번째 줄인지인데 안 써서 버린다
            .query { rs, _ ->
                val uuid: ByteArray = rs.getBytes("uuid")
                val roomName: String = rs.getString("room_name")
                RoomResult(
                    id = BinaryUuid.fromBytes(uuid),
                    name = roomName,
                    capacity = rs.getInt("capacity"),
                )
            }.list()
}
