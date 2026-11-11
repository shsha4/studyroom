package com.studyroom.member.member

import com.google.rpc.ErrorInfo
import com.studyroom.contracts.member.v1.GetMemberStatusRequest
import com.studyroom.contracts.member.v1.MemberServiceGrpc
import com.studyroom.member.grpc.GrpcErrorHandler
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.grpc.client.GrpcChannelFactory
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID
import java.util.concurrent.TimeUnit
import com.studyroom.contracts.member.v1.MemberStatus as ProtoMemberStatus

// gRPC 어댑터 슬라이스 테스트. proto 요청 → MemberGrpcService → 서비스 호출 → proto 응답·Status 만 본다.
// 서비스는 @MockitoBean 대역이라 "없는 회원이면 404" 같은 업무 판단은 MemberServiceTest 가 맡고 여기선 다시 보지 않는다.
// 근거: spring/06 「1.」 gRPC Adapter 행, 「25. gRPC도 자기 Adapter만 테스트한다」
// @AutoConfigureTestGrpcTransport 가 서버·클라이언트를 in-process 채널로 바꾼다. 9090 포트를 열지 않아 테스트끼리 포트가 안 부딪힌다.
@SpringBootTest
@AutoConfigureTestGrpcTransport
class MemberGrpcServiceTest(
    // 테스트도 생성자로 주입받는다. lateinit 은 @MockitoBean 필드에만 쓴다. 근거: spring/06 「17.」
    @Autowired channels: GrpcChannelFactory,
) {
    @MockitoBean
    lateinit var memberService: MemberService

    // 테스트 클라이언트도 Deadline 을 건다. 서버가 응답을 안 닫는 버그가 있으면 테스트가 멈추지 않고 DEADLINE_EXCEEDED 로 실패한다.
    // 근거: 컨벤션 01 「4.」 gRPC "클라이언트 호출은 deadline 을 항상 설정한다"
    private val stub =
        MemberServiceGrpc
            .newBlockingStub(channels.createChannel("member"))
            .withDeadlineAfter(5, TimeUnit.SECONDS)

    @Test
    fun `있는 회원이면 상태를 proto enum 으로 바꿔 돌려준다`() {
        whenever(memberService.getStatus(UID)).thenReturn(MemberStatus.SUSPENDED)

        val response = stub.getMemberStatus(request(UID.toString()))

        // 우리 enum → proto enum 매핑(MemberGrpcMapping.toProto)까지 지났는지 본다.
        assertThat(response.status).isEqualTo(ProtoMemberStatus.MEMBER_STATUS_SUSPENDED)
    }

    @Test
    fun `없는 회원이면 NOT_FOUND 와 ErrorInfo reason MEMBER-404 로 답한다`() {
        whenever(memberService.getStatus(UID)).thenThrow(MemberNotFoundException(UID))

        val e =
            catchThrowableOfType(StatusRuntimeException::class.java) { stub.getMemberStatus(request(UID.toString())) }

        // code 끝 404 → NOT_FOUND 는 GrpcErrorHandler 가 고른다.
        assertThat(e.status.code).isEqualTo(Status.Code.NOT_FOUND)
        // room 은 Status 코드가 아니라 이 reason 으로 무슨 실패인지 가른다. trailer 의 rpc Status 에서 꺼낸다.
        assertThat(errorInfo(e).reason).isEqualTo("MEMBER-404")
    }

    @Test
    fun `uid 가 UUID 형식이 아니면 서비스를 부르지 않고 INVALID_ARGUMENT 로 답한다`() {
        val e = catchThrowableOfType(StatusRuntimeException::class.java) { stub.getMemberStatus(request("not-a-uuid")) }

        assertThat(e.status.code).isEqualTo(Status.Code.INVALID_ARGUMENT)
        // 형식 검사는 어댑터에서 끝난다. 서비스까지 내려가지 않았어야 한다. 근거: spring/04 「6.」
        verify(memberService, never()).getStatus(any())
    }

    // proto3 string 은 안 채우면 "" 다. 빈 uid 도 형식 오류로 걸러야 한다.
    @Test
    fun `uid 를 안 보내면 INVALID_ARGUMENT 로 답한다`() {
        val e =
            catchThrowableOfType(StatusRuntimeException::class.java) {
                stub.getMemberStatus(GetMemberStatusRequest.getDefaultInstance())
            }

        assertThat(e.status.code).isEqualTo(Status.Code.INVALID_ARGUMENT)
    }

    @Test
    fun `예상 못 한 예외는 INTERNAL 로 답하고 원래 메시지를 싣지 않는다`() {
        whenever(memberService.getStatus(UID)).thenThrow(IllegalStateException("내부 값 노출 금지"))

        val e =
            catchThrowableOfType(StatusRuntimeException::class.java) { stub.getMemberStatus(request(UID.toString())) }

        // 처리 안 된 예외의 Spring gRPC 기본값은 UNKNOWN 이다. INTERNAL 이면 GrpcErrorHandler 의 else 가지를 탄 것이다.
        assertThat(e.status.code).isEqualTo(Status.Code.INTERNAL)
        assertThat(e.status.description).doesNotContain("내부 값 노출 금지")
    }

    private fun request(uid: String): GetMemberStatusRequest = GetMemberStatusRequest.newBuilder().setUid(uid).build()

    // trailer 에 실린 google.rpc.Status 를 꺼내 details 의 ErrorInfo 를 푼다.
    private fun errorInfo(e: StatusRuntimeException): ErrorInfo =
        checkNotNull(StatusProto.fromThrowable(e)) { "trailer 에 rpc Status 가 없다" }
            .detailsList
            .single { it.`is`(ErrorInfo::class.java) }
            .unpack(ErrorInfo::class.java)

    // 앱 전체를 스캔하지 않고 gRPC 어댑터와 예외 핸들러만 올린다. 저장소(InMemoryMemberStore)는 컨텍스트에 없다.
    // 중첩 설정 클래스가 있으면 @SpringBootTest 가 MemberApplication 대신 이걸 쓴다.
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(MemberGrpcService::class, GrpcErrorHandler::class)
    class GrpcSliceConfiguration

    companion object {
        private val UID: UUID = UUID.fromString("00000000-0000-7000-8000-000000000001")
    }
}
