package kr.weit.roadyfoody.auth.exception

import kr.weit.roadyfoody.common.exception.BaseException
import kr.weit.roadyfoody.common.exception.ErrorCode

class UserNotRegisteredException(
    message: String = "회원가입을 하지 않은 사용자입니다.",
) : BaseException(ErrorCode.NOT_FOUND_USER, message)
