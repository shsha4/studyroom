package com.studyroom.room.reservation.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class UuidV7Test {
    @Test
    fun `버전 7 과 RFC 변형 비트를 가진다`() {
        val uuid = UuidV7.generate(Instant.parse("2026-11-03T13:00:00Z"))

        assertThat(uuid.version()).isEqualTo(7)
        assertThat(uuid.variant()).isEqualTo(2)
    }

    @Test
    fun `앞 48비트는 만든 시각의 밀리초다`() {
        val now = Instant.parse("2026-11-03T13:00:00.123Z")

        val uuid = UuidV7.generate(now)

        assertThat(uuid.mostSignificantBits ushr 16).isEqualTo(now.toEpochMilli())
    }

    @Test
    fun `늦게 만든 값이 바이트 순서로 뒤에 온다`() {
        val earlier = UuidV7.generate(Instant.parse("2026-11-03T13:00:00.000Z"))
        val later = UuidV7.generate(Instant.parse("2026-11-03T13:00:00.001Z"))

        assertThat(later.toString()).isGreaterThan(earlier.toString())
    }
}
