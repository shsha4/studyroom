# studyroom

KEG 플랫폼 구조를 손으로 익히려고 만드는 연습 프로젝트다. 도메인은 스터디룸 예약이고, 구조·컨벤션·스택은 우리 플랫폼을 줄여서 그대로 따른다. 결정의 근거는 온보딩 레포 `../onboarding`이 원본이다.

## 진행 방식

사용자가 직접 타이핑하면서 익히는 게 목적이다. Claude는 이 순서를 지킨다.

1. **코드는 채팅으로 준다.** 사용자가 요청하지 않으면 Claude가 `src/` 아래 파일을 직접 만들거나 고치지 않는다. 빌드 설정처럼 사용자가 맡긴 파일만 예외다.
2. **한 번에 파일 하나나 둘만 준다.** 코드마다 담긴 결정과 그 근거 문서 절을 적는다(예: 「컨벤션 04 「5. JPA 엔티티」」). 근거는 기억에 기대지 말고 `../onboarding` 문서를 다시 읽어 확인한다. 문서에 없는 선택은 "문서에 없음, 연습용 선택"이라고 밝힌다.
3. **결정 설명은 코드 주석으로 붙여 준다.** 채팅 본문에 긴 설명을 따로 쓰지 않는다. 결정마다 줄 위에 2~3줄로 한 줄 요약, 안 하면 생기는 일, `근거: 컨벤션 05 「10.」`을 적는다. 사용자는 주석을 그대로 치거나 자기 말로 바꿔 친다.
4. **다 쳤다고 하면 파일을 읽고 확인한다.** 오타나 컴파일 문제와 함께, 사용자 주석에 적힌 이해가 문서와 어긋나는 곳을 짚는다. 고칠 곳은 알려만 주고, 고치는 건 사용자가 한다. 다만 `spotlessCheck`가 공백·줄바꿈 위반만으로 실패하면 Claude가 `spotlessApply`를 돌리고 진행한다.
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

답변, 문서, 주석 예시는 현업 개발자가 쓰는 한국어로 쓴다. 아래 말은 쓰지 않고 오른쪽처럼 고친다. 목록 원본은 온보딩 레포 `.claude/skills/debt/engine.py`의 `SLOP_FIX`다. 백틱 안 코드는 대상이 아니다.

| 쓰지 않는 말 | 고칠 말 |
| --- | --- |
| ~를 통해(서) | ~로, ~에서 (메서드를 통해 → 메서드로) |
| ~에 대해, ~에 대한 | ~를, ~의 (Service에 대한 정책 → Service의 정책) |
| ~에 의해 | 주어를 앞에 둔다 (Hibernate에 의해 생성된다 → Hibernate가 만든다) |
| 되어지다 | 되다 |
| ~로 인해 | ~라서, ~때문에 |
| ~하는 데 사용되는 | ~하는 |
| 가능하게 하다 | ~할 수 있게 하다 |
| 적절한 | 필요한, 맞는. 기준이 있으면 기준을 쓴다 |
| 효과적, 효율적 | 무엇이 나아지는지 쓴다 (빠르다, 쿼리가 준다) |
| 다양한 | 여러, 또는 무엇인지 나열한다 |
| 원활 | 막히지 않고, 끊기지 않고 |
| 손쉽게 | 바로, 또는 뺀다 |
| 강력한, 핵심적, 혁신, 중요한 역할 | 뺀다. 무엇을 하는지 동사로 쓴다 |

설명은 쉽게 쓴다.

- 컨벤션 문서 문장을 그대로 옮겨 설명을 대신하지 않는다. 문서 문장은 근거로 끝에 한 줄만 붙인다.
- 결정마다 ① 한 줄 요약 ② 안 하면 무슨 일이 생기는지 짧은 예시 코드 ③ 근거 문서 절 순서로 쓴다.
- 처음 나오는 용어(프록시, SAM, soft keyword 등)는 그 자리에서 풀어 쓴다.
- 결정을 큰 표에 몰아넣지 않는다. 표는 짧은 비교에만 쓴다.
