package com.studyroom.room.shared.adapter.`in`.web

import com.studyroom.room.shared.application.ApplicationException
import com.studyroom.room.shared.domain.DomainException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI

// 예외 → HTTP 응답 변환은 이 advice 한 곳에서만 한다. 컨트롤러는 try/catch 없이 정상 흐름만 쓴다.
// 근거: 컨벤션 01 「4.」 REST, spring/02 「17.」
// 컨트롤러가 아닌 HTTP 공용 기반이라 shared/adapter/in/web 에 둔다. 근거: 컨벤션 01 「2.」 기능 안의 계층
// 에러 본문은 RFC 9457 ProblemDetail + 확장 필드 code·traceId. 근거: 컨벤션 01 「4.」 REST, spring/02 「18.」
// application.yml 에 spring.mvc.problemdetails.enabled 를 켜지 않는다. 켜면 Boot 핸들러가 400·404·405 를
// 먼저 가져가서 code·traceId 없는 응답이 나간다. 근거: 컨벤션 01 「4.」 REST
// ResponseEntityExceptionHandler 를 상속하면 Spring MVC 자체 예외(헤더 누락, JSON 파싱 실패, 405 등)도
// ProblemDetail 로 받아 아래 createResponseEntity 에서 같은 확장 필드를 붙일 수 있다.
// 클래스 이름은 문서에 없음, 연습용 선택
@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {
    // 유스케이스 실패(ROOM-404, RESERVATION-409 …). 코드 끝 세 자리가 HTTP 상태다.
    // 호출자 잘못을 알리는 4xx 거부는 정상 결과라 스택 로그를 남기지 않는다. 근거: 컨벤션 01 「5.」
    @ExceptionHandler(ApplicationException::class)
    fun handleApplication(
        ex: ApplicationException,
        request: HttpServletRequest,
    ): ProblemDetail = problem(ex.code, ex.message, request.requestURI)

    // 도메인 규칙 위반(RESERVATION-400 잘못된 시간대 …). 처리 방식은 위와 같다.
    @ExceptionHandler(DomainException::class)
    fun handleDomain(
        ex: DomainException,
        request: HttpServletRequest,
    ): ProblemDetail = problem(ex.code, ex.message, request.requestURI)

    // 위에서 못 잡은 예외는 서버 버그다. 여기서 처리를 끝내므로 스택을 한 번 남긴다. 근거: 컨벤션 01 「5.」
    // 원래 메시지는 응답에 싣지 않는다. SQL·내부 클래스 이름 같은 값이 밖으로 나갈 수 있다.
    // 로그 메시지는 고정 문구. 근거: 컨벤션 04 「8.」 LOG_MESSAGE_CONSTANT
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        ex: Exception,
        request: HttpServletRequest,
    ): ProblemDetail {
        log.error("처리하지 못한 예외", ex)
        return problem(UNEXPECTED_CODE, "서버에서 요청을 처리하지 못했다", request.requestURI)
    }

    // ResponseEntityExceptionHandler 가 Spring MVC 예외로 만든 ProblemDetail 이 응답으로 나가기 직전에 지나는 곳.
    // 여기서 code·traceId 를 붙여 우리 예외와 같은 모양으로 맞춘다.
    // MVC 예외는 서비스 에러 코드가 없어서 COMMON-<상태>로 채운다. 접두어는 문서에 없음, 연습용 선택
    override fun createResponseEntity(
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        if (body is ProblemDetail) {
            // WebRequest 는 서블릿 요청을 감싼 것이라 꺼내서 경로를 읽는다.
            val path = (request as? ServletWebRequest)?.request?.requestURI
            body.addPlatformFields("$COMMON_PREFIX-${statusCode.value()}", path)
        }
        return super.createResponseEntity(body, headers, statusCode, request)
    }

    // code 형식은 예외 생성자에서 이미 검사했다(<도메인>-<1~5로 시작하는 세 자리>). 그래서 끝 세 자리를 바로 상태로 쓴다.
    private fun problem(
        code: String,
        detail: String?,
        path: String,
    ): ProblemDetail {
        val status = HttpStatusCode.valueOf(code.substringAfterLast('-').toInt())
        val problem = ProblemDetail.forStatusAndDetail(status, detail)
        problem.addPlatformFields(code, path)
        return problem
    }

    // traceId 는 Micrometer Tracing 이 MDC 에 넣는 값을 읽기만 한다. 손으로 MDC 에 넣지 않는다. 근거: 컨벤션 03 「5.」
    // 지금은 트레이싱을 안 들여서 null 로 나간다. 칸은 계약이라 먼저 둔다.
    // instance 는 표준 칸(요청 경로)이다. Spring 7 은 자동으로 채우지 않아서 직접 넣는다. 이미 있으면 두고 비었을 때만 채운다.
    private fun ProblemDetail.addPlatformFields(
        code: String,
        path: String?,
    ) {
        if (instance == null && path != null) {
            instance = URI.create(path)
        }
        setProperty("code", code)
        setProperty("traceId", MDC.get(TRACE_ID_KEY))
    }

    companion object {
        // 로거는 companion object 에 클래스를 명시해서 둔다. 근거: 컨벤션 04 「8.」 LOGGER_IS_STATIC
        private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)
        private const val TRACE_ID_KEY = "traceId"
        private const val COMMON_PREFIX = "COMMON"
        private const val UNEXPECTED_CODE = "$COMMON_PREFIX-500"
    }
}
