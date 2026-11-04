package com.studyroom.room.shared.adapter.`in`.web

import com.studyroom.room.shared.application.ApplicationException
import com.studyroom.room.shared.domain.DomainException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

// advice 만 따로 본다. 실제 컨트롤러 대신 예외만 던지는 테스트용 컨트롤러를 붙인다.
// 어댑터 자기 책임(예외 → 상태 코드·ProblemDetail 모양)만 본다. 근거: 컨벤션 03 「1.」
@WebMvcTest(ApiExceptionHandlerTest.ThrowingController::class)
@Import(ApiExceptionHandlerTest.ThrowingController::class)
class ApiExceptionHandlerTest(
    @Autowired private val mvc: MockMvcTester,
) {
    @Test
    fun `애플리케이션 예외는 코드 끝 세 자리를 상태로 쓰고 코드를 본문에 싣는다`() {
        assertThat(mvc.get().uri("/test/application"))
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson()
            .isLenientlyEqualTo(
                """{"status": 409, "detail": "테스트 충돌", "code": "TEST-409", "instance": "/test/application"}""",
            )
    }

    @Test
    fun `도메인 예외도 같은 방식으로 상태와 코드를 싣는다`() {
        assertThat(mvc.get().uri("/test/domain"))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .extractingPath("$.code")
            .isEqualTo("TEST-400")
    }

    // Spring MVC 자체 예외도 우리 예외와 같은 확장 필드를 가진다. problemdetails.enabled 를 켰다면 code 가 빠진다.
    @Test
    fun `필수 헤더가 없으면 400 과 COMMON-400 코드를 싣는다`() {
        assertThat(mvc.get().uri("/test/header"))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson()
            .extractingPath("$.code")
            .isEqualTo("COMMON-400")
    }

    @Test
    fun `예상하지 못한 예외는 500 으로 내고 원래 메시지를 싣지 않는다`() {
        assertThat(mvc.get().uri("/test/unexpected"))
            .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
            .bodyJson()
            .extractingPath("$.detail")
            .isEqualTo("서버에서 요청을 처리하지 못했다")
    }

    // 트레이싱을 안 들여서 값은 null 이지만 계약 칸은 있어야 한다.
    @Test
    fun `에러 본문에는 traceId 칸이 있다`() {
        assertThat(mvc.get().uri("/test/application"))
            .bodyJson()
            .hasPath("$.traceId")
    }

    @RestController
    class ThrowingController {
        @GetMapping("/test/application")
        fun application(): String = throw TestConflictException()

        @GetMapping("/test/domain")
        fun domain(): String = throw TestRuleException()

        @GetMapping("/test/header")
        fun header(
            @RequestHeader("X-Required") value: String,
        ): String = value

        @GetMapping("/test/unexpected")
        fun unexpected(): String = throw IllegalStateException("select * from secret_table")
    }

    class TestConflictException : ApplicationException("TEST-409", "테스트 충돌")

    class TestRuleException : DomainException("TEST-400", "테스트 규칙 위반")
}
