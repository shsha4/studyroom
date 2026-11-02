package com.studyroom.room.reservation.domain

import java.time.Instant
import java.time.temporal.ChronoUnit

// 값 객체는 data class + init
// 프로퍼티는 전부 val
// 값 객체를 쓰는 이유
// startAt, endAt 을 Instant 두 개로 따로 들고 다니지 않고 TimeSlot 하나로 묶은 이유 3가지
// 1. 잘못된 시간대가 아예 만들어지지 않는다.
// 2. 시작과 끝을 바꿔 넣는 실수를 막는다.
// 3. 시간대에 대한 판단이 한곳에 모인다.
data class TimeSlot(
    val startAt: Instant,
    val endAt: Instant,
) {
    init {
        if (!startAt.isBefore(endAt)) {
            throw InvalidTimeSlotException("끝 시각은 시작 시각보다 늦어야 한다")
        }

        if (!isOnTheHour(startAt) || !isOnTheHour(endAt)) {
            throw InvalidTimeSlotException("예약은 정시에 시작하고 정시에 끝나야 한다")
        }
    }

    // 두 시간대가 조금이라도 겹치면 true
    fun overlaps(other: TimeSlot): Boolean = startAt < other.endAt && other.startAt < endAt

    // 시각이 정시인지 확인
    // 시 단위로 잘라 낸 값이 원래 값과 같으면 정시
    private fun isOnTheHour(at: Instant): Boolean = at.truncatedTo(ChronoUnit.HOURS) == at
}
