package com.studyroom.room.room.adapter.out.persistence

import java.nio.ByteBuffer
import java.util.UUID

object BinaryUuid {
    // DB의 16바이트를 UUID로 바꾼다
    // JPA는 이 변환을 알아서 해 주지만 JdbcClient는 SQL만 대신 실행해 줄 뿐이라 우리가 직접 바꿔야 한다.
    fun fromBytes(bytes: ByteArray): UUID {
        require(bytes.size == 16) { "BINARY(16) uuid 는 16바이트여야 한다: ${bytes.size}" }
        val buffer = ByteBuffer.wrap(bytes)
        return UUID(buffer.long, buffer.long)
    }
    // bytes:  [01 a1 a1 7e bc 80 7d 74][b0 dc 9e 4f 66 d1 33 44]
    //          └ 첫 번째 buffer.long ┘    └ 두 번째 buffer.long ┘
    //          = mostSigBits             = leastSigBits

    fun toBytes(uuid: UUID): ByteArray =
        ByteBuffer
            .allocate(16)
            .putLong(uuid.mostSignificantBits)
            .putLong(uuid.leastSignificantBits)
            .array()
}
