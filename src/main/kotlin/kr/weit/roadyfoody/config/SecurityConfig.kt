package kr.weit.roadyfoody.config

import kr.weit.roadyfoody.security.filter.MockPassFilter
import kr.weit.roadyfoody.security.handler.CustomAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(MockPassFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests {
                it
                    .requestMatchers(*PERMITTED_URL_PATTERNS).permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(CustomAuthenticationEntryPoint())
            }
            .build()
    }
}

private val PERMITTED_URL_PATTERNS =
    arrayOf(
        "/health",
        "/ready",
        "/api/v1/test/success",
        "/api/v1/test/error",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/actuator/prometheus",
    )
