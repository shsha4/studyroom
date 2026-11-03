package com.studyroom.room

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

// 메서드끼리 서로 부르지 않으니 CGLIB 프록시가 필요 없다. (Bean 을 클래스 내부에서 호출하는 부분이 없어서)
@Configuration(proxyBeanMethods = false)
class TimeConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
