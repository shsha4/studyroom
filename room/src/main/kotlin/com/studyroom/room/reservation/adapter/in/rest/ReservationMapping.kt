package com.studyroom.room.reservation.adapter.`in`.rest

import com.studyroom.room.reservation.adapter.`in`.rest.dto.CreateReservationRequest
import com.studyroom.room.reservation.adapter.`in`.rest.dto.ReservationResponse
import com.studyroom.room.reservation.application.command.ReservationResult
import com.studyroom.room.reservation.domain.TimeSlot

// REST 어댑터의 매핑. 어댑터가 자기 프로토콜 타입(Request/Response)과 애플리케이션 타입 사이를 바꾼다.
// 근거: 컨벤션 01 「매핑 소유권」, 컨벤션 04 「6.」 <대상>Mapping.kt
// persistence 쪽에도 같은 이름 파일이 있지만 패키지가 달라서 부딪히지 않는다.

// 요청 → 도메인 값 객체. 여기서 TimeSlot 을 만들 때 init 검증이 돈다. 틀리면 InvalidTimeSlotException(RESERVATION-400).
fun CreateReservationRequest.toTimeSlot(): TimeSlot = TimeSlot(startAt, endAt)

fun ReservationResult.toResponse(): ReservationResponse =
    ReservationResponse(
        id = id,
        roomId = roomId,
        startAt = startAt,
        endAt = endAt,
        status = status,
    )
