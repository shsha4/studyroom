package com.studyroom.member.member

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class MemberService(
    private val members: InMemoryMemberStore,
) {
    fun getStatus(uid: UUID): MemberStatus = members.findStatus(uid) ?: throw MemberNotFoundException(uid)
}
