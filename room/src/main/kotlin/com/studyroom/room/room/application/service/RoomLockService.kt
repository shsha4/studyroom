package com.studyroom.room.room.application.service

import com.studyroom.room.room.application.port.`in`.LockRoomUseCase
import com.studyroom.room.room.application.port.out.RoomRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RoomLockService(
    private val rooms: RoomRepositoryPort,
) : LockRoomUseCase {
    // propagation (트랜잭션 전파 속성)
    // REQUIRED (default) : 부모 트랜잭션이 있음 합류, 없음 새로 열고, 메서드 끝날때 커밋
    // MANDATORY : 부모 트랜잭션이 있음 합류, 없음 IllegalTransactionStateException 예외 발생
    // REQUIRED 면 부르는 쪽이 @Transactional 을 빠뜨렸을 때 잠그자마자 커밋돼 잠금이 바로 풀리게 된다.
    @Transactional(propagation = Propagation.MANDATORY)
    override fun lock(roomId: UUID): Long = rooms.lockIdByUuid(roomId) ?: throw RoomNotFoundException(roomId)
}
