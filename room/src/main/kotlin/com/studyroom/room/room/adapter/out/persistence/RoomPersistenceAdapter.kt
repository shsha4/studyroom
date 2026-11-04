package com.studyroom.room.room.adapter.out.persistence

import com.studyroom.room.room.application.command.RoomResult
import com.studyroom.room.room.application.port.out.RoomRepositoryPort
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

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

    // 예약 전에 조회한 방 로우 락을 건다.
    // 락이 없으면 동시 요청에서 예약이 없는 방을 조회하여 동시성 이슈 발생 가능.
    // FOR UPDATE 는 행 하나를 잠글 때 사용해야 한다.
    // 조건 자체를 잠그면 갭 락이 걸린다.
    override fun lockIdByUuid(uuid: UUID): Long? =
        jdbcClient
            .sql("SELECT id FROM room WHERE uuid = :uuid FOR UPDATE")
            .param("uuid", BinaryUuid.toBytes(uuid))
            .query(Long::class.java)
            .optional()
            .orElse(null)
}
