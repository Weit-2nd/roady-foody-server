package kr.weit.roadyfoody.auth.security.filter

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.weit.roadyfoody.auth.fixture.TEST_ACCESS_TOKEN
import kr.weit.roadyfoody.auth.fixture.TEST_BEARER_ACCESS_TOKEN
import kr.weit.roadyfoody.auth.fixture.createTestSecurityUser
import kr.weit.roadyfoody.auth.security.CustomUserDetailService
import kr.weit.roadyfoody.auth.security.jwt.JwtUtil
import kr.weit.roadyfoody.user.fixture.TEST_USER_SOCIAL_ID
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import javax.crypto.SecretKey

class JwtFilterTest : BehaviorSpec({
    val jwtUtil = mockk<JwtUtil>()
    val customUserDetailService = mockk<CustomUserDetailService>()
    val jwtFilter = JwtFilter(jwtUtil, customUserDetailService)

    val request = mockk<HttpServletRequest>()
    val response = mockk<HttpServletResponse>()
    val filterChain = mockk<FilterChain>(relaxed = true)

    beforeEach {
        every { request.getAttribute(any()) } returns null
        every { request.dispatcherType } returns null
        every { request.setAttribute(any(), any()) } returns Unit
        every { request.removeAttribute(any()) } returns Unit
    }

    afterEach {
        SecurityContextHolder.clearContext()
        clearAllMocks()
    }

    given("JwtFilter 테스트") {
        `when`("유효한 토큰이 제공되면") {
            every { request.getHeader(AUTHORIZATION) } returns TEST_BEARER_ACCESS_TOKEN
            every { jwtUtil.accessKey } returns mockk<SecretKey>()
            every { jwtUtil.validateToken(any<SecretKey>(), any<String>()) } returns true
            every { jwtUtil.getSocialId(any<SecretKey>(), any<String>()) } returns TEST_USER_SOCIAL_ID
            val securityUser = createTestSecurityUser()
            every { customUserDetailService.loadUserByUsername(TEST_USER_SOCIAL_ID) } returns securityUser
            then("SecurityContextHolder 에 인증 정보가 설정되어야 한다") {
                jwtFilter.doFilter(request, response, filterChain)

                val actual = SecurityContextHolder.getContext().authentication as UsernamePasswordAuthenticationToken
                actual.principal shouldBe securityUser
                verify {
                    request.getHeader(AUTHORIZATION)
                    jwtUtil.accessKey
                    jwtUtil.validateToken(any<SecretKey>(), any<String>())
                    jwtUtil.getSocialId(any<SecretKey>(), any<String>())
                    customUserDetailService.loadUserByUsername(TEST_USER_SOCIAL_ID)
                    filterChain.doFilter(request, response)
                }
            }
        }

        `when`("유효하지 않은 토큰이 제공되면") {
            every { request.getHeader(AUTHORIZATION) } returns TEST_BEARER_ACCESS_TOKEN
            every { jwtUtil.accessKey } returns mockk<SecretKey>()
            every { jwtUtil.validateToken(any<SecretKey>(), any<String>()) } returns false
            then("SecurityContextHolder 에 인증 정보가 설정되지 않아야 한다") {
                jwtFilter.doFilter(request, response, filterChain)

                val actual = SecurityContextHolder.getContext().authentication
                actual.shouldBeNull()
                verify {
                    request.getHeader(AUTHORIZATION)
                    jwtUtil.accessKey
                    jwtUtil.validateToken(any<SecretKey>(), any<String>())
                    filterChain.doFilter(request, response)
                }
            }
        }

        `when`("Bearer 토큰이 아닌 토큰이 제공되면") {
            every { request.getHeader(AUTHORIZATION) } returns TEST_ACCESS_TOKEN
            then("SecurityContextHolder 에 인증 정보가 설정되지 않아야 한다") {
                jwtFilter.doFilter(request, response, filterChain)

                val actual = SecurityContextHolder.getContext().authentication
                actual.shouldBeNull()
                verify {
                    request.getHeader(AUTHORIZATION)
                    filterChain.doFilter(request, response)
                }
            }
        }

        `when`("토큰이 제공되지 않으면") {
            every { request.getHeader(AUTHORIZATION) } returns null
            then("SecurityContextHolder 에 인증 정보가 설정되지 않아야 한다") {
                jwtFilter.doFilter(request, response, filterChain)

                val actual = SecurityContextHolder.getContext().authentication
                actual.shouldBeNull()
                verify {
                    request.getHeader(AUTHORIZATION)
                    filterChain.doFilter(request, response)
                }
            }
        }

        `when`("토큰이 빈 문자열로 제공되면") {
            every { request.getHeader(AUTHORIZATION) } returns ""
            then("SecurityContextHolder 에 인증 정보가 설정되지 않아야 한다") {
                jwtFilter.doFilter(request, response, filterChain)

                val actual = SecurityContextHolder.getContext().authentication
                actual.shouldBeNull()
                verify {
                    request.getHeader(AUTHORIZATION)
                    filterChain.doFilter(request, response)
                }
            }
        }

        `when`("토큰이 공백 문자열로 제공되면") {
            every { request.getHeader(AUTHORIZATION) } returns " "
            then("SecurityContextHolder 에 인증 정보가 설정되지 않아야 한다") {
                jwtFilter.doFilter(request, response, filterChain)

                val actual = SecurityContextHolder.getContext().authentication
                actual.shouldBeNull()
                verify {
                    request.getHeader(AUTHORIZATION)
                    filterChain.doFilter(request, response)
                }
            }
        }
    }
})
