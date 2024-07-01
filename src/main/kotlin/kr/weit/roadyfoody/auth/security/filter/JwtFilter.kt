package kr.weit.roadyfoody.auth.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.weit.roadyfoody.auth.security.CustomUserDetailService
import kr.weit.roadyfoody.auth.security.jwt.JwtUtil
import kr.weit.roadyfoody.auth.security.jwt.isNotBearerToken
import kr.weit.roadyfoody.auth.security.jwt.removeBearer
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtFilter(
    private val jwtUtil: JwtUtil,
    private val customUserDetailService: CustomUserDetailService,
) :
    OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val bearerToken = request.getHeader(AUTHORIZATION)
        if (bearerToken.isNullOrBlank() || bearerToken.isNotBearerToken()) {
            filterChain.doFilter(request, response)
            return
        }

        val token = bearerToken.removeBearer()
        if (jwtUtil.validateToken(jwtUtil.accessKey, token)) {
            val socialId = jwtUtil.getSocialId(jwtUtil.accessKey, token)
            val securityUser = customUserDetailService.loadUserByUsername(socialId)
            val authentication = UsernamePasswordAuthenticationToken(securityUser, null, securityUser.authorities)
            SecurityContextHolder.getContext().authentication = authentication
        }
        filterChain.doFilter(request, response)
    }
}
