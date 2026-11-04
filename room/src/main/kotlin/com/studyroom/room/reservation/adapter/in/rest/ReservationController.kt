package com.studyroom.room.reservation.adapter.`in`.rest

import com.studyroom.room.reservation.adapter.`in`.rest.dto.CreateReservationRequest
import com.studyroom.room.reservation.adapter.`in`.rest.dto.ReservationResponse
import com.studyroom.room.reservation.application.service.ReservationCancelService
import com.studyroom.room.reservation.application.service.ReservationCreateService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

// 컨트롤러는 입력 변환 → 유스케이스 호출 → 응답 변환만 한다. 비즈니스 로직을 쓰지 않는다. 근거: 컨벤션 01 「4.」 공통
// 경로는 /v1 + 복수형 소문자 리소스. 근거: 컨벤션 01 「4.」 REST
// 부르는 곳이 이 컨트롤러 하나라 port/in 인터페이스 없이 서비스 클래스를 직접 주입한다. 근거: 컨벤션 01 「포트 규칙」
@RestController
@RequestMapping("/v1/reservations")
class ReservationController(
    private val reservationCreateService: ReservationCreateService,
    private val reservationCancelService: ReservationCancelService,
) {
    // 새 리소스를 만들었으니 201 Created.
    // 회원 id 는 임시로 X-Member-Id 헤더에서 받는다. 4단계에서 gateway 가 붙인 X-Passport 로 바꾼다.
    // 헤더를 읽는 건 어댑터 일이다. 서비스는 헤더를 모르고 UUID 만 받는다. 근거: 컨벤션 01 「4.」 공통
    // 헤더가 없거나 UUID 형식이 아니면 Spring 이 400 을 낸다.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestHeader(MEMBER_ID_HEADER) memberId: UUID,
        @RequestBody request: CreateReservationRequest,
    ): ReservationResponse =
        reservationCreateService.reserve(request.roomId, memberId, request.toTimeSlot()).toResponse()

    // 취소는 리소스 삭제가 아니라 상태 변경이라 DELETE 가 아니다. 커스텀 액션은 동사 하위 경로로 쓴다.
    // 근거: 컨벤션 01 「4.」 REST "POST /v1/lectures/{id}/publish"
    // 처음 취소(Cancelled)와 이미 취소됨(AlreadyCancelled) 둘 다 "취소된 상태"라서 같은 204 로 응답한다(멱등).
    // 클라이언트는 재시도해도 결과가 같다. 응답 모양은 문서에 없음, 연습용 선택
    // 남의 예약이면 서비스가 RESERVATION-404 를 던진다. 근거: 컨벤션 01 「4.」 REST "남의 리소스는 404"
    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancel(
        @RequestHeader(MEMBER_ID_HEADER) memberId: UUID,
        // @PathVariable 에 이름을 안 적어도 된다. javaParameters = true 라 파라미터 이름 id 로 {id} 를 찾는다.
        @PathVariable id: UUID,
    ) {
        reservationCancelService.cancel(id, memberId)
    }

    companion object {
        // 4단계에서 passport 로 바꾸면 지운다.
        const val MEMBER_ID_HEADER = "X-Member-Id"
    }
}
