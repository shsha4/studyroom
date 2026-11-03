package com.studyroom.room.reservation.domain

import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

/**
 * UUID 는 128비트(16바이트) 숫자를 16진수 32글자로 적은 것이다. 하이픈은 읽기 좋으라고 넣은 구분자일 뿐이다.
 * 그래서 DB에는 CHAR(36) 문자열이 아니라 BINARY(16) 16바이트로 넣는다
 *
 * v4 : v7
 * v4 - 비트 6개를 빼고 나머지 122비트가 전부 난수 (순서가 없음)
 * v7 - 앞 48비트는 만든 시각(밀리초), 나머지는 난수 (순서가 있음)
 *
 * MySQL 인덱스는 B+ Tree 구조에서 인덱스 값 순서대로 정렬된다.
 * v4 - 새 값이 트리의 아무 데나 끼어든다. 이미 꽉 찬 페이지 한가운데에 넣어야 해서 페이지 분할이 자주 일어나고, 메모리에 없는
 *      예전 페이지를 디스크에서 다시 읽어 와야 한다.
 *
 * v7 - 새 값이 항상 지금까지 중 가장 크다. 그래서 트리 오른쪽 끝에만 붙는다, AUTO_INCREMENT id가 PK에 붙는 방식과 같다.
 */
object UuidV7 {
// object 클래스 선언과 인스턴스 하나를 한 번에 만든다.
// 싱글톤으로 인스턴스를 생성해준다.
    private val random = SecureRandom()

    fun generate(now: Instant): UUID {
        // 코틀린은 자바처럼 << , |, & 같은 비트 연산자 기호가 없다. 대신 중위 함수 shl(왼쪽 시프트), ushr(부호 없는 오른쪽 시프트), or, and 를 쓴다.
        val randA = random.nextInt(1 shl 12).toLong()
        val randB = random.nextLong() and 0x3FFF_FFFF_FFFF_FFFFL
        val mostSigBits = (now.toEpochMilli() shl 16) or (0x7L shl 12) or randA
        val leastSigBits = randB or Long.MIN_VALUE
        return UUID(mostSigBits, leastSigBits)
    }
}
