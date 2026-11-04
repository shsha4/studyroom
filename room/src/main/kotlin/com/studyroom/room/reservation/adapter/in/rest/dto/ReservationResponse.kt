package com.studyroom.room.reservation.adapter.`in`.rest.dto

import com.studyroom.room.reservation.domain.ReservationStatus
import java.time.Instant
import java.util.UUID

// 이름은 <도메인>Response. 근거: 컨벤션 02 「2.」
// ReservationResult 를 그대로 응답하지 않고 HTTP 용 모양을 따로 둔다. 서비스 결과에 필드가 늘어도 API 가 따라 바뀌지 않는다.
// 도메인 모델(Reservation)과 엔티티는 응답으로 내보내지 않는다. 근거: 컨벤션 01 「4.」 REST NO_DOMAIN_IN_REST_RESPONSE
data class ReservationResponse(
    val id: UUID,
    // 방 uuid. 내부 Long id 는 밖으로 내보내지 않는다.
    val roomId: UUID,
    val startAt: Instant,
    val endAt: Instant,
    // enum 은 이름("RESERVED")으로 직렬화된다.
    val status: ReservationStatus,
)
