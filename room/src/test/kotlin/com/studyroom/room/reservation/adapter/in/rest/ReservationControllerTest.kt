package com.studyroom.room.reservation.adapter.`in`.rest

import com.studyroom.room.reservation.application.command.ReservationResult
import com.studyroom.room.reservation.application.service.ReservationCancelService
import com.studyroom.room.reservation.application.service.ReservationCreateService
import com.studyroom.room.reservation.domain.ReservationStatus
import com.studyroom.room.reservation.domain.TimeSlot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.time.Instant
import java.util.UUID

// 어댑터 슬라이스 테스트. 컨트롤러와 MVC 설정만 띄우고 서비스·DB 는 띄우지 않는다. 근거: 컨벤션 03 「1.」
// 어댑터 자기 책임(요청 → 서비스 인자 변환, 상태 코드, 응답 JSON 모양)만 보고 비즈니스 결과는 보지 않는다. 근거: 컨벤션 03 「1.」
// MockMvcTester 는 AssertJ 로 검증하는 MockMvc. assertion 은 AssertJ 만 쓴다. 근거: 컨벤션 03 「2.」
// 스프링 테스트 주입은 생성자로 한다. 근거: 컨벤션 04 「10.」
@WebMvcTest(ReservationController::class)
class ReservationControllerTest(
    @Autowired private val mvc: MockMvcTester,
) {
    // @MockitoBean 은 필드에만 붙어서 lateinit 이 허용되는 경우다. 근거: 컨벤션 04 「10.」
    @MockitoBean
    lateinit var reservationCreateService: ReservationCreateService

    @MockitoBean
    lateinit var reservationCancelService: ReservationCancelService

    @Test
    fun `예약 요청을 서비스 인자로 바꿔 부르고 201 과 예약 응답을 돌려준다`() {
        whenever(reservationCreateService.reserve(ROOM_ID, MEMBER_ID, SLOT)).thenReturn(RESULT)

        val result =
            mvc
                .post()
                .uri("/v1/reservations")
                .header("X-Member-Id", MEMBER_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY)

        // stub 이 정확한 인자(ROOM_ID, MEMBER_ID, SLOT)에만 걸려 있어서, 변환이 틀리면 null 이 와서 이 검증이 깨진다.
        assertThat(result)
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .isLenientlyEqualTo(
                """
                {
                  "id": "$RESERVATION_ID",
                  "roomId": "$ROOM_ID",
                  "startAt": "2026-11-05T01:00:00Z",
                  "endAt": "2026-11-05T03:00:00Z",
                  "status": "RESERVED"
                }
                """.trimIndent(),
            )
    }

    @Test
    fun `회원 헤더가 없으면 400 으로 거절하고 서비스를 부르지 않는다`() {
        val result =
            mvc
                .post()
                .uri("/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY)

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST)
        verify(reservationCreateService, never()).reserve(any(), any(), any())
    }

    @Test
    fun `취소 요청은 경로의 예약 id 와 헤더의 회원 id 로 서비스를 부르고 204 를 돌려준다`() {
        val result =
            mvc
                .post()
                .uri("/v1/reservations/{id}/cancel", RESERVATION_ID)
                .header("X-Member-Id", MEMBER_ID.toString())

        assertThat(result).hasStatus(HttpStatus.NO_CONTENT)
        verify(reservationCancelService).cancel(RESERVATION_ID, MEMBER_ID)
    }

    // 이 파일에서만 쓰는 픽스처 값. 근거: 컨벤션 03 「2.」
    companion object {
        private val ROOM_ID: UUID = UUID.fromString("01a1a17e-bc80-7d74-b0dc-9e4f66d13344")
        private val MEMBER_ID: UUID = UUID.fromString("01a1a17e-bc80-7d74-b0dc-000000000001")
        private val RESERVATION_ID: UUID = UUID.fromString("01a1a17e-bc80-7d74-b0dc-000000000002")
        private val SLOT = TimeSlot(Instant.parse("2026-11-05T01:00:00Z"), Instant.parse("2026-11-05T03:00:00Z"))
        private val RESULT =
            ReservationResult(RESERVATION_ID, ROOM_ID, SLOT.startAt, SLOT.endAt, ReservationStatus.RESERVED)

        private val REQUEST_BODY =
            """
            {
              "roomId": "$ROOM_ID",
              "startAt": "2026-11-05T01:00:00Z",
              "endAt": "2026-11-05T03:00:00Z"
            }
            """.trimIndent()
    }
}
