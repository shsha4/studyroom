package com.studyroom.room.reservation.adapter.`in`.rest.dto

import java.time.Instant
import java.util.UUID

// 이름은 <행위><도메인>Request, 위치는 adapter/in/rest/dto. 근거: 컨벤션 02 「2.」, 컨벤션 01 「4.」 REST
// 세 필드 모두 널 불가에 기본값이 없다. JSON 에서 빠지면 Jackson 역직렬화가 실패해 400 이 된다.
// 그래서 @NotNull 같은 검증 애너테이션을 따로 달지 않는다. 근거: 컨벤션 04 「9.」
// "끝이 시작보다 늦다", "정시다" 검사는 여기서 하지 않는다. 도메인 TimeSlot 이 한다.
// 같은 검증을 두 곳에 쓰지 않는다. 근거: 컨벤션 01 「4.」 공통
data class CreateReservationRequest(
    // 방은 외부 공개 uuid 로 받는다. 내부 Long id 는 room 기능이 잠그면서 풀어 준다.
    val roomId: UUID,
    // ISO-8601 문자열("2026-11-05T01:00:00Z")로 받는다.
    val startAt: Instant,
    val endAt: Instant,
)
