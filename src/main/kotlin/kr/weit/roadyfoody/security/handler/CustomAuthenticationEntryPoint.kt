package kr.weit.roadyfoody.security.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.weit.roadyfoody.dto.ErrorResponse
import kr.weit.roadyfoody.support.exception.ErrorCode
import kr.weit.roadyfoody.support.jsonmapper.ObjectMapperProvider
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapperProvider: ObjectMapperProvider,
) : AuthenticationEntryPoint {
    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        objectMapperProvider.objectMapper.writeValue(
            response?.outputStream,
            ErrorResponse.of(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.errorMessage),
        )
    }
}
