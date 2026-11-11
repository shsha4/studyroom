package com.studyroom.member.member

import com.studyroom.contracts.member.v1.GetMemberStatusRequest
import com.studyroom.contracts.member.v1.GetMemberStatusResponse
import com.studyroom.contracts.member.v1.MemberServiceGrpc
import io.grpc.stub.StreamObserver
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class MemberGrpcService(
    private val memberService: MemberService,
) : MemberServiceGrpc.MemberServiceImplBase() {
    // try/catch 를 쓰지 않는다. MemberNotFoundException 같은 예외는 던진다.
    override fun getMemberStatus(
        request: GetMemberStatusRequest,
        responseObserver: StreamObserver<GetMemberStatusResponse>,
    ) {
        val status = memberService.getStatus(request.toUid())

        responseObserver.onNext(status.toResponse())
        responseObserver.onCompleted()
    }
}
