# studyroom

스터디룸 예약 미니 플랫폼이다. KEG 플랫폼의 컨벤션, 요청 흐름, 스택을 직접 타이핑하며 익히려고 만든다. 진행 방식과 모듈 구성은 [CLAUDE.md](CLAUDE.md)에 있다.

서비스마다 별도 Spring Boot 앱이고, 서비스끼리는 gRPC와 Kafka, gateway를 거친 REST로만 만난다. 서비스 안은 기능 우선 패키지다.

## 도메인

- 회원이 스터디룸을 시간 단위로 예약하고 취소한다.
- 같은 방, 같은 시간대에는 예약이 하나만 선다.
- 정지된 회원(`SUSPENDED`)은 예약할 수 없지만 방 목록은 볼 수 있다.
- 예약이 확정되면 알림을 보낸다.

## 단계

단계마다 직접 겪을 결정과 먼저 읽을 문서다. 경로는 온보딩 레포(`../onboarding`) 기준이다.

- [x] **0. 빌드 골격**: Gradle 멀티모듈, Kotlin 2.4.21, JVM 25, Spring Boot 4.2.0-RC1(sparrow 0.49 기준)
  - [컨벤션 04 「1. 빌드와 도구」](../onboarding/platform/convention/04_Kotlin-Code-Style.md), [spring/00](../onboarding/spring/00_Kotlin-Spring-Boot.md)
- [x] **1. 도메인과 Kotlin**: 값 객체는 data class, 결과는 sealed, 프로퍼티는 `val`. 도메인은 Spring을 모른다
  - [컨벤션 04 「2. 널 안전성」「3. 데이터 타입과 불변성」](../onboarding/platform/convention/04_Kotlin-Code-Style.md), [kotlin/03 Data·Sealed·Object](../onboarding/kotlin/03_Data-Sealed-Object.md), [spring/06 「2. Domain은 Spring 없이 테스트한다」](../onboarding/spring/06_Test.md)
- [ ] **2. room REST + JPA**: 기능 우선 패키지, DTO는 어댑터 소유, 엔티티는 일반 class, Flyway, `ddl-auto=validate`, `open-in-view=false`, 읽기는 `readOnly`
  - [컨벤션 01 「2.」「3.」「4.」](../onboarding/platform/convention/01_Java-Architecture-Spring.md), [컨벤션 05](../onboarding/platform/convention/05_DB-Schema-Design.md), [spring/03 「1.」~「18.」「25.」](../onboarding/spring/03_JPA-and-Transaction.md), [spring/05](../onboarding/spring/05_Platform-Architecture.md)
- [ ] **3. member gRPC**: 회원 상태를 포트 뒤에서 묻는다. Deadline은 반드시 걸고 재시도는 앱에서 하지 않는다. member가 답하지 못하면 예약(쓰기)은 막고 조회는 통과시킨다
  - [spring/04 「2.」~「12.」](../onboarding/spring/04_gRPC-and-Kafka.md), [컨벤션 01 「7. 외부 연동」](../onboarding/platform/convention/01_Java-Architecture-Spring.md), [crane 「2. 핵심 판단」 외부 판정이 안 될 때의 처리](../onboarding/platform/services/crane.md), [weaver 「2. 핵심 판단」 회원 신원 재조회](../onboarding/platform/services/weaver.md)
- [ ] **4. gateway와 X-Passport**: 입구는 gateway 하나다. 밖에서 온 `X-Passport`를 지운 뒤 새로 붙인다. room은 passport만 믿는다
  - [README 「서비스 호출과 데이터」](../onboarding/README.md), [weaver](../onboarding/platform/services/weaver.md)
- [ ] **5. Outbox + Kafka + notifier**: 예약 저장과 이벤트 행을 한 트랜잭션에 커밋하고, 발행은 커밋 뒤에 한다. notifier는 같은 이벤트가 두 번 와도 알림을 한 번만 보낸다
  - [spring/03 「19.」~「24.」](../onboarding/spring/03_JPA-and-Transaction.md), [spring/04 「13.」~「23.」](../onboarding/spring/04_gRPC-and-Kafka.md), [Kafka 문서](../onboarding/k8s/kafka/00_Kafka.md), [pigeon 「2. 핵심 판단」](../onboarding/platform/services/pigeon.md)
- [ ] **6. 멱등 예약**: 같은 요청 id로 다시 보내면 이미 만든 예약을 돌려준다
  - [crane 「2. 핵심 판단」 가입은 보상으로 맞춘다](../onboarding/platform/services/crane.md)
- [ ] **7. 테스트 정리**: 어댑터는 Testcontainers(MySQL·Kafka), ArchUnit으로 계층·기능 규칙을 건다. 유스케이스 테스트에 `@SpringBootTest`를 쓰지 않는다
  - [컨벤션 03](../onboarding/platform/convention/03_Java-Test-Observability.md), [spring/06](../onboarding/spring/06_Test.md), [spring/05 「14.」「15.」](../onboarding/spring/05_Platform-Architecture.md)

## 내 기록

단계마다 헷갈렸던 것과 문서를 읽고 바로잡은 것을 적는다.

| 단계 | 헷갈린 것 | 바로잡은 이해 |
| --- | --- | --- |
| | | |
