package com.studyroom.room.shared.application

abstract class ApplicationException(
    val code: String,
    message: String,
) : RuntimeException(message) {
    // 객체를 만들때 마다 실행된다 생성자 느낌
    init {
        // 조건이 거짓이면 그 메시지로 IllegalArgumentException 을 던진다. 메시지 람다는 실패할 때만 실행
        require(CODE_FORMAT.matches(code)) {
            "오류 코드는 <도메인>-<HTTP 상태 코드> 형식이다(예 ROOM-404): $code"
        }
    }

    companion object {
        private val CODE_FORMAT = Regex("[A-Z][A-Z0-9_]*-[1-5][0-9]{2}")
    }
}
// 도메인 규칙 위반이 아닌 실패를 담는 예외 케이스
// DomainException - 도메인이 규칙을 어겼다고 판단 ex) TimeSlot: 종료 시각이 시작 시각보다 빠르다.
// ApplicationException - 유스케이스 흐름에서 생긴 실패 ex) 예약하려는 방이 없다 (ROOM-404)
