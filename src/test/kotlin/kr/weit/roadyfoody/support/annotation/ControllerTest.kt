package kr.weit.roadyfoody.support.annotation

import kr.weit.roadyfoody.auth.security.handler.CustomAuthenticationEntryPoint
import kr.weit.roadyfoody.global.log.TraceManager
import kr.weit.roadyfoody.support.config.TestSecurityConfig
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ActiveProfiles("test")
@Import(TraceManager::class, TestSecurityConfig::class, CustomAuthenticationEntryPoint::class)
annotation class ControllerTest
