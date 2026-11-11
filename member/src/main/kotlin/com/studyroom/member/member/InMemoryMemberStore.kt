package com.studyroom.member.member

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class InMemoryMemberStore {
    private val members: Map<UUID, MemberStatus> =
        mapOf(
            UUID.fromString("00000000-0000-7000-8000-000000000001") to MemberStatus.ACTIVE,
            UUID.fromString("00000000-0000-7000-8000-000000000002") to MemberStatus.SUSPENDED,
        )

    // 없는 회원이면 null
    fun findStatus(uid: UUID): MemberStatus? = members[uid]
}
