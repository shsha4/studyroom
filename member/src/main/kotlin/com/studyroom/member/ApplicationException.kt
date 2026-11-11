package com.studyroom.member

abstract class ApplicationException(
    val code: String,
    message: String,
) : RuntimeException(message) {
    init {
        // gRPC 로 나갈 때도 이 code 를 ErrorInfo.reason 에 그대로 담는다.
        require(CODE_FORMAT.matches(code)) {
            "오류 코드는 <도메인>-<HTTP 상태 세 자리> 형식이다(예 MEMBER-404): $code"
        }
    }

    companion object {
        private val CODE_FORMAT = Regex("[A-Z][A-Z0-9_]*-[1-5][0-9]{2}")
    }
}
