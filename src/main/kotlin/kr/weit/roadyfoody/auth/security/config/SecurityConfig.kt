package kr.weit.roadyfoody.auth.security.config

import kr.weit.roadyfoody.auth.security.CustomUserDetailService
import kr.weit.roadyfoody.auth.security.filter.JwtFilter
import kr.weit.roadyfoody.auth.security.handler.CustomAuthenticationEntryPoint
import kr.weit.roadyfoody.auth.security.jwt.JwtUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Profile("!test")
@Configuration
class SecurityConfig(
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val jwtUtil: JwtUtil,
    private val customUserDetailService: CustomUserDetailService,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(
                JwtFilter(jwtUtil, customUserDetailService),
                UsernamePasswordAuthenticationFilter::class.java,
            )
            .authorizeHttpRequests {
                it
                    .requestMatchers(*NOT_PERMITTED_URL_PATTERNS).authenticated()
                    .requestMatchers(*PERMITTED_URL_PATTERNS).permitAll()
                    .anyRequest().authenticated()
            }.exceptionHandling {
                it.authenticationEntryPoint(customAuthenticationEntryPoint)
            }.build()
}

val PERMITTED_URL_PATTERNS =
    arrayOf(
        "/health",
        "/ready",
        "/api/v1/tourism/**",
        "/api/v1/address/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/actuator/prometheus",
        "/api/v1/terms/**",
        "/api/v1/auth/**",
        "/api/v1/food-spots/**",
    )

val NOT_PERMITTED_URL_PATTERNS = arrayOf("/api/v1/auth/sign-out")
