import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    alias(libs.plugins.kotlin.jvm)
    // @Component·@Transactional 이 붙은 클래스를 열어 스프링이 프록시를 만들 수 있게 한다.
    alias(libs.plugins.kotlin.spring)
    // @Entity 에 JPA 가 쓰는 기본 생성자를 만든다.
    alias(libs.plugins.kotlin.jpa)
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

// plugin.jpa 는 클래스를 열지 않는다. 엔티티가 final 이면 Hibernate 프록시를 못 만들어 지연 로딩이 깨진다.
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    // 의존성 버전은 BOM 이 정한다. platform-bom 이 kotlin-bom 을 같이 묶는 모양을 따라 Kotlin 도 BOM 으로 고정한다.
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform(libs.kotlin.bom))

    implementation(libs.spring.boot.starter.webmvc)
    // 스프링이 생성자 파라미터의 기본값과 널 여부를 읽는다.
    implementation(kotlin("reflect"))
    // Jackson 이 data class 생성자와 기본값, 널 여부를 읽는다.
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.spring.boot.starter.webmvc.test)
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
