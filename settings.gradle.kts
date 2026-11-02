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
