package com.studyroom.member.member

import com.studyroom.contracts.member.v1.GetMemberStatusRequest
import com.studyroom.contracts.member.v1.GetMemberStatusResponse
import io.grpc.Status
import java.util.UUID
import com.studyroom.contracts.member.v1.MemberStatus as ProtoMemberStatus

fun GetMemberStatusRequest.toUid(): UUID =
    try {
        UUID.fromString(uid)
    } catch (e: IllegalArgumentException) {
        throw Status.INVALID_ARGUMENT
            .withDescription("uid 는 UUID 형식이어야 한다.")
            .withCause(e)
            .asRuntimeException()
    }

fun MemberStatus.toResponse(): GetMemberStatusResponse =
    GetMemberStatusResponse.newBuilder().setStatus(toProto()).build()

private fun MemberStatus.toProto(): ProtoMemberStatus =
    when (this) {
        MemberStatus.ACTIVE -> ProtoMemberStatus.MEMBER_STATUS_ACTIVE
        MemberStatus.SUSPENDED -> ProtoMemberStatus.MEMBER_STATUS_SUSPENDED
    }
