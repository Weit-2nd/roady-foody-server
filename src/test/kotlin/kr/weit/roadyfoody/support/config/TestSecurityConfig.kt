package kr.weit.roadyfoody.support.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.weit.roadyfoody.auth.security.SecurityUser
import kr.weit.roadyfoody.auth.security.config.NOT_PERMITTED_URL_PATTERNS
import kr.weit.roadyfoody.auth.security.config.PERMITTED_URL_PATTERNS
import kr.weit.roadyfoody.auth.security.handler.CustomAuthenticationEntryPoint
import kr.weit.roadyfoody.user.domain.User
import kr.weit.roadyfoody.user.fixture.TEST_SOCIAL_ID
import kr.weit.roadyfoody.user.fixture.TEST_SOCIAL_LOGIN_TYPE
import kr.weit.roadyfoody.user.fixture.TEST_USER_NICKNAME
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@TestConfiguration
class TestSecurityConfig(
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(
                MockPassFilter(),
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

class MockPassFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (request.headerNames.toList().contains("userid")) {
            val socialId = TEST_SOCIAL_ID
            val securityUser = SecurityUser(User.of(socialId = "$TEST_SOCIAL_LOGIN_TYPE $socialId", nickname = TEST_USER_NICKNAME))
            val authentication = UsernamePasswordAuthenticationToken(securityUser, null, securityUser.authorities)
            SecurityContextHolder.getContext().authentication = authentication
        }
        filterChain.doFilter(request, response)
    }
}
