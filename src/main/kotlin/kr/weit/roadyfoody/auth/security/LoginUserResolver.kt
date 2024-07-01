package kr.weit.roadyfoody.auth.security

import kr.weit.roadyfoody.common.exception.BaseException
import kr.weit.roadyfoody.common.exception.ErrorCode
import kr.weit.roadyfoody.user.domain.User
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class LoginUserResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(LoginUser::class.java) && parameter.parameterType == User::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): User {
        val authentication = SecurityContextHolder.getContext().authentication?.principal
        return (authentication as? SecurityUser)?.user ?: throw AuthenticatedUserNotFoundException()
    }
}

class AuthenticatedUserNotFoundException(message: String = "인증된 사용자를 찾을 수 없습니다.") :
    BaseException(ErrorCode.UNAUTHORIZED, message)
