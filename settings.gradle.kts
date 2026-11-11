pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    // 저장소는 여기서만 정한다. 모듈 build 파일에 repositories 를 적으면 빌드가 실패한다.
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "studyroom"

// 서비스마다 별도 Spring Boot 앱이다. 3단계부터 member, gateway, notifier, proto 를 더한다.
include("room")
// 플랫폼의 parrot 자리. 앱이 아니라 계약 스텁 jar 다. 서비스끼리 공유하는 건 이 모듈뿐이다.
include("proto")
// 플랫폼의 crane ResolveIdentity 자리. 회원 상태를 gRPC 로만 답하는 별도 앱이다.
include("member")
