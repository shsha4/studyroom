import com.diffplug.gradle.spotless.SpotlessExtension
import com.google.protobuf.gradle.id

// 플랫폼의 parrot 자리. Spring 앱이 아니라 proto 에서 만든 Java 스텁을 담는 jar 하나다(parrot 「1.」).
// 서비스 모듈은 이 모듈만 의존하고, 서비스끼리 서로의 코드를 import 하지 않는다.
// parrot 은 계약마다 jar 를 나누지만 여기선 모듈 하나에 둔다. 문서에 없음, 연습용 선택
plugins {
    `java-library`
    alias(libs.plugins.protobuf)
    alias(libs.plugins.spotless)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    // 이 모듈은 버전 없이 이름만 적는다. 숫자는 Boot BOM(grpc-bom·protobuf-bom 포함)이 정한다.
    // implementation 이라 BOM 이 소비 모듈로 새지 않는다. 소비 서비스는 자기 BOM 으로 런타임 버전을 받는다(parrot 「2.」 버전과 BOM).
    implementation(platform(libs.spring.boot.dependencies))
    // 생성된 코드가 공개 API 에 이 타입들을 드러내서 api 로 둔다. 소비 모듈 컴파일 클래스패스에도 올라간다.
    api(libs.protobuf.java)
    api(libs.grpc.protobuf)
    api(libs.grpc.stub)
}

protobuf {
    // protoc 은 실행 파일이라 BOM 이 못 정한다. 런타임과 같은 숫자를 버전 카탈로그에서 읽는다(parrot 「2.」 스텁 생성).
    // 카탈로그에 protobuf-plugin 이 있어 protobuf 가 묶음 이름이 됐다. 묶음 자신의 값은 asProvider() 로 꺼낸다.
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.asProvider().get()}"
    }
    // service 정의에서 서버 베이스 클래스와 클라이언트 스텁(XxxGrpc)을 만드는 플러그인.
    // Kotlin 스텁(grpc-kotlin)은 코루틴 기반이라 쓰지 않는다. 플랫폼은 Java 스텁 jar 다.
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
        }
    }
    generateProtoTasks {
        all().configureEach {
            plugins {
                // 생성 코드에 javax.annotation.Generated 를 붙이지 않는다. 붙이면 JDK 에 없는 javax 의존성을 따로 넣어야 한다.
                id("grpc") {
                    option("@generated=omit")
                }
            }
        }
    }
}

// Kotlin 소스가 없어 build 파일 포맷만 본다.
configure<SpotlessExtension> {
    kotlinGradle {
        ktlint()
    }
}
