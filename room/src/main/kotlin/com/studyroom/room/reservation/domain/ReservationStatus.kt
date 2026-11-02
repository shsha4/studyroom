package com.studyroom.room.reservation.domain

// 예약 상태는 종류만 다르고 상태별로 들고 있는 데이터는 없기 때문에 ENUM
enum class ReservationStatus {
    RESERVED,
    CANCELLED,
}
