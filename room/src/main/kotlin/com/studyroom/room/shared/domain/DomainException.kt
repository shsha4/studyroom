package com.studyroom.room.shared.domain

abstract class DomainException(
    val code: String,
    message: String,
) : RuntimeException(message) {
    init {
        require(CODE_FORMAT.matches(code)) {
            "오류 코드는 <도메인>-<HTTP 상태 세 자리> 형식이다(예 RESERVATION-409): $code"
        }
    }

    companion object {
        private val CODE_FORMAT = Regex("[A-Z][A-Z0-9_]*-[1-5][0-9]{2}")
    }
}
