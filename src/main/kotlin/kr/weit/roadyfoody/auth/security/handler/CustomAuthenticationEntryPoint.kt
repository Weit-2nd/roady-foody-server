package kr.weit.roadyfoody.auth.security.handler

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.weit.roadyfoody.common.exception.ErrorCode
import kr.weit.roadyfoody.common.exception.ErrorResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        objectMapper.writeValue(
            response?.outputStream,
            ErrorResponse.of(ErrorCode.UNAUTHORIZED, "인증정보가 없습니다."),
        )
    }
}
