import com.diffplug.gradle.spotless.SpotlessExtension

// room 과 따로 뜨는 Spring Boot 앱이다. room 코드를 import 하지 않고 proto 계약만 공유한다(CLAUDE.md 「구조 원칙」).
plugins {
    alias(libs.plugins.kotlin.jvm)
    // @Service·@Component 클래스를 열어 스프링이 프록시를 만들 수 있게 한다.
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spotless)
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        // 바이트코드에 파라미터 이름을 남겨 스프링이 읽을 수 있게 한다.
        javaParameters = true
    }
}

dependencies {
    // 버전은 BOM 이 정한다. grpc·protobuf 런타임 숫자도 이 BOM 에서 온다(parrot 「2.」 버전과 BOM).
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform(libs.kotlin.bom))

    // 서버가 구현할 MemberServiceGrpc 베이스 클래스와 메시지 타입. 서비스끼리 공유하는 건 이것뿐이다.
    implementation(project(":proto"))
    // HTTP 서버 없이 gRPC 서버만 연다. member 는 REST 진입점이 없다.
    implementation(libs.spring.boot.starter.grpc.server)
    // 스프링이 생성자 파라미터의 기본값과 널 여부를 읽는다.
    implementation(kotlin("reflect"))

    testImplementation(libs.spring.boot.starter.grpc.server.test)
    testImplementation(libs.mockito.kotlin)
    // Gradle 9 는 JUnit 플랫폼 런처를 직접 넣어야 테스트를 돌린다.
    testRuntimeOnly(libs.junit.platform.launcher)
}

// 일반 jar 를 끄고 실행 가능한 bootJar 하나만 만든다.
tasks.named<Jar>("jar") {
    enabled = false
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// ./gradlew build 가 spotlessCheck 를 돌린다. 포맷은 ./gradlew spotlessApply 로 맞춘다.
configure<SpotlessExtension> {
    kotlin {
        target("src/**/*.kt")
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}
