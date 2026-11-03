package com.studyroom.room.shared.adapter.out.persistence

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.time.Clock
import java.time.Instant
import java.util.Optional

@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
class JpaAuditingConfiguration {
    // DateTimeProvider.getNow() 가 Optional 을 반환
    @Bean
    fun auditingDateTimeProvider(clock: Clock): DateTimeProvider = DateTimeProvider { Optional.of(Instant.now(clock)) }
}
