// 루트는 앱이 아니다. 플러그인을 한 번만 클래스패스에 올리고 적용은 서비스 모듈이 한다.
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spotless) apply false
}
