package com.studyroom.member.member

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

// Spring 없이 저장소만 mock 한다. @SpringBootTest 면 gRPC 서버까지 떠서 느리고 실패 원인이 흐려진다.
// 근거: spring/06 「4. Application은 Outbound Port만 Mock한다」「5. 왜 @SpringBootTest를 사용하지 않는가」
// 진짜 InMemoryMemberStore 를 쓰면 고정 uid 에 테스트가 묶여서 저장소를 DB 로 바꿀 때 테스트도 같이 깨진다.
class MemberServiceTest {
    // @Component 라 plugin.spring 이 클래스를 열어 둬서 인터페이스 없이도 mock 할 수 있다.
    private val members: InMemoryMemberStore = mock()
    private val service = MemberService(members)

    @Test
    fun `있는 회원이면 상태를 돌려준다`() {
        // ACTIVE 가 아니라 SUSPENDED 로 둔다. 저장소 값을 그대로 넘기는지 보려면 기본값처럼 보이지 않는 값이 낫다.
        whenever(members.findStatus(UID)).thenReturn(MemberStatus.SUSPENDED)

        val status = service.getStatus(UID)

        assertThat(status).isEqualTo(MemberStatus.SUSPENDED)
    }

    // stub 하지 않으면 mock 은 null 을 돌려준다. 저장소에 없는 회원과 같다.
    @Test
    fun `없는 회원이면 MEMBER-404 로 거절한다`() {
        assertThatThrownBy { service.getStatus(UID) }
            .isInstanceOf(MemberNotFoundException::class.java)
            // 3-3 인터셉터가 이 code 로 NOT_FOUND 를 고른다. 코드가 바뀌면 여기서 먼저 잡힌다.
            .hasFieldOrPropertyWithValue("code", "MEMBER-404")
    }

    companion object {
        // 테스트 이름은 백틱 한국어 문장, 고정값은 companion 상수로 둔다. 근거: 컨벤션 04 「10.」, spring/06 「9.」
        private val UID: UUID = UUID.fromString("00000000-0000-7000-8000-000000000001")
    }
}
