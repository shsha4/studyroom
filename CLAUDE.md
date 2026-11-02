# studyroom

KEG 플랫폼 구조를 손으로 익히려고 만드는 연습 프로젝트다. 도메인은 스터디룸 예약이고, 구조·컨벤션·스택은 우리 플랫폼을 줄여서 그대로 따른다. 결정의 근거는 온보딩 레포 `../onboarding`이 원본이다.

## 진행 방식

사용자가 직접 타이핑하면서 익히는 게 목적이다. Claude는 이 순서를 지킨다.

1. **코드는 채팅으로 준다.** 사용자가 요청하지 않으면 Claude가 `src/` 아래 파일을 직접 만들거나 고치지 않는다. 빌드 설정처럼 사용자가 맡긴 파일만 예외다.
2. **한 번에 파일 하나나 둘만 준다.** 코드마다 담긴 결정과 그 근거 문서 절을 함께 적는다(예: 「컨벤션 04 「5. JPA 엔티티」」). 근거는 기억에 기대지 말고 `../onboarding` 문서를 다시 읽어 확인한다. 문서에 없는 선택은 "문서에 없음, 연습용 선택"이라고 밝힌다.
3. **설명 주석은 미리 달지 않는다.** 이해한 내용은 사용자가 주석으로 직접 쓴다.
4. **다 쳤다고 하면 파일을 읽고 확인한다.** 오타나 컴파일 문제와 함께, 사용자 주석에 적힌 이해가 문서와 어긋나는 곳을 짚는다. 고칠 곳은 알려만 주고, 고치는 건 사용자가 한다.
5. 빌드와 테스트가 통과하면 단계를 커밋한다. 커밋 메시지는 컨벤션 08을 따른다. 진행 상황은 README 「단계」 체크박스에 반영한다.

## 스택

[spring/00 「1. 우리 플랫폼의 기본 스택」](../onboarding/spring/00_Kotlin-Spring-Boot.md)과 같다. 버전 숫자만은 온보딩 문서가 아니라 sparrow `gradle/libs.versions.toml`을 원본으로 삼는다. 온보딩 문서는 sparrow 0.49(Boot 4.2.0-RC1)를 아직 반영하지 않았다. 숫자는 `gradle/libs.versions.toml`에만 적는다.

- Kotlin 2.4.21, JVM 25, Gradle Kotlin DSL 멀티모듈
- Spring Boot 4.2.0-RC1(Spring Framework 7.1), Spring MVC + Virtual Thread
- WebFlux·코루틴·`suspend`·`Flow`는 쓰지 않는다
- Spring Data JPA + MySQL + Flyway, gRPC(proto 계약), Kafka
- 테스트: JUnit 5, mockito-kotlin, AssertJ, Testcontainers. MockK와 H2는 쓰지 않는다

## 모듈과 요청 흐름

[README 「요청 흐름」](../onboarding/README.md)을 줄인 모양이다.

```text
client ──REST──> gateway ──REST──> room ──gRPC 9090──> member
                 X-Passport 부착     │
                                     └─ Outbox → Kafka studyroom.reservation.created.v1 ──> notifier
```

| 모듈 | 플랫폼에서 맡는 서비스 | 역할 |
| --- | --- | --- |
| `gateway` | [weaver](../onboarding/platform/services/weaver.md) | 유일한 입구. 인증을 확인하고 `X-Passport`를 새로 붙여 뒤로 보낸다 |
| `room` | [crane](../onboarding/platform/services/crane.md) | 본체. 방 조회, 예약, 취소. REST 인바운드, JPA |
| `member` | crane `ResolveIdentity` | 회원 상태(ACTIVE/SUSPENDED)를 gRPC로 답한다 |
| `notifier` | [pigeon](../onboarding/platform/services/pigeon.md) | 예약 이벤트를 받아 알림을 보낸다(로그로 대신해도 된다) |
| `proto` | parrot | gRPC·이벤트 계약 |

## 구조 원칙

플랫폼과 같은 모양을 지키는 것이 목표다. 연습이라고 구조를 줄이지 않는다.

- **서비스마다 별도 Spring Boot 앱이다.** 레포 하나에 Gradle 멀티모듈로 두지만 모듈끼리 코드를 import하지 않는다. 서비스끼리는 gRPC, Kafka, gateway를 거친 REST로만 만난다. 공유하는 것은 `proto` 계약뿐이다. 하나로 합치면 Deadline, 장애 시 막을지 통과시킬지, `X-Passport` 홉, 중복 소비를 겪을 수 없다.
- **서비스 안은 기능 우선 패키지다.** 컨벤션 01 「2. 패키지 구조와 의존 방향」대로 최상위를 기능으로 나누고, 기능 사이에는 상대의 `application/port/in/` 계약만 쓴다.
- 1~2단계는 room 앱만 만든다. 3단계부터 member, gateway, notifier를 별도 앱으로 붙이고 docker compose로 함께 띄운다.

## 컨벤션

사내 위키 컨벤션의 사본이 온보딩 레포에 있다. 코드를 줄 때 해당 절을 짚는다.

| 문서 | 주로 쓰는 절 |
| --- | --- |
| [01 아키텍처·Spring](../onboarding/platform/convention/01_Java-Architecture-Spring.md) | 「2. 패키지 구조와 의존 방향」, 「4. 인바운드 어댑터」, 「5. 예외」, 「6. 트랜잭션과 이벤트」, 「7. 외부 연동」 |
| [02 Java 코드 스타일](../onboarding/platform/convention/02_Java-Code-Style.md) | 04가 바꿔 읽으라는 절만 |
| [03 테스트·관측](../onboarding/platform/convention/03_Java-Test-Observability.md) | 「1. 계층별 테스트」, 「3. ArchUnit 규칙」, 「4. Testcontainers와 DB」 |
| [04 Kotlin 코드 스타일](../onboarding/platform/convention/04_Kotlin-Code-Style.md) | 전체. 「0. Java 문서에서 바꿔 읽는 절」부터 본다 |
| [05 DB 스키마](../onboarding/platform/convention/05_DB-Schema-Design.md) | 「1. 네이밍」~「5. 제약」, 「9. 스키마 변경」, 「10. JPA 매핑 규칙」 |
| [06 DB 쿼리](../onboarding/platform/convention/06_DB-Query.md) | 조회 쿼리를 짤 때 |
| [08 커밋 메시지](../onboarding/platform/convention/08_Git-Commit-Message.md) | 단계별 커밋 |

컨벤션 전체 목록은 [00](../onboarding/platform/convention/00_Conventions.md)에 있다.

## 기술 문서

컨벤션의 "왜"를 풀어 쓴 문서다. 단계마다 읽을 곳은 README 「단계」에 적었다.

- Kotlin: [../onboarding/kotlin/](../onboarding/kotlin/)
- Spring: [00 Kotlin+Spring](../onboarding/spring/00_Kotlin-Spring-Boot.md), [01 DI·설정](../onboarding/spring/01_DI-and-Configuration.md), [02 MVC·DTO](../onboarding/spring/02_MVC-and-DTO.md), [03 JPA·트랜잭션](../onboarding/spring/03_JPA-and-Transaction.md), [04 gRPC·Kafka](../onboarding/spring/04_gRPC-and-Kafka.md), [05 플랫폼 아키텍처](../onboarding/spring/05_Platform-Architecture.md), [06 테스트](../onboarding/spring/06_Test.md)
- Kafka 토픽·등급: [../onboarding/k8s/kafka/00_Kafka.md](../onboarding/k8s/kafka/00_Kafka.md)

## 문장

답변과 문서는 현업 개발자가 쓰는 한국어로 쓴다. 번역투(~를 통해, ~에 대한)와 빈말(다양한, 효율적인)을 쓰지 않는다.
