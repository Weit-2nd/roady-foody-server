package kr.weit.roadyfoody.auth.fixture

import kr.weit.roadyfoody.auth.application.dto.KakaoUserResponse
import kr.weit.roadyfoody.auth.application.dto.ServiceTokensResponse
import kr.weit.roadyfoody.auth.application.dto.SignUpRequest
import kr.weit.roadyfoody.auth.security.SecurityUser
import kr.weit.roadyfoody.auth.security.jwt.generateHmac256Key
import kr.weit.roadyfoody.term.fixture.createTestRequiredTermIdSet
import kr.weit.roadyfoody.user.domain.SocialLoginType
import kr.weit.roadyfoody.user.fixture.TEST_SOCIAL_ID
import kr.weit.roadyfoody.user.fixture.TEST_SOCIAL_LOGIN_TYPE
import kr.weit.roadyfoody.user.fixture.TEST_USER_NICKNAME
import kr.weit.roadyfoody.user.fixture.createTestUser
import java.time.LocalDateTime

const val TEST_SOCIAL_ACCESS_TOKEN = "test-token"
val TEST_CONNECTED_AT: LocalDateTime = LocalDateTime.now()

fun createTestKakaoUserResponse(): KakaoUserResponse =
    KakaoUserResponse(
        TEST_SOCIAL_ID,
        TEST_CONNECTED_AT,
    )

fun createTestSignUpRequest(
    nickname: String = TEST_USER_NICKNAME,
    termIdSet: Set<Long> = createTestRequiredTermIdSet(),
    socialLoginType: SocialLoginType = TEST_SOCIAL_LOGIN_TYPE,
): SignUpRequest =
    SignUpRequest(
        nickname,
        termIdSet,
        socialLoginType,
    )

val TEST_ACCESS_TOKEN = generateHmac256Key()
val TEST_REFRESH_TOKEN = generateHmac256Key()

val TEST_BEARER_ACCESS_TOKEN = "Bearer $TEST_ACCESS_TOKEN"
val TEST_BEARER_REFRESH_TOKEN = "Bearer $TEST_REFRESH_TOKEN"

fun createTestTokensResponse(): ServiceTokensResponse =
    ServiceTokensResponse(
        TEST_ACCESS_TOKEN,
        TEST_REFRESH_TOKEN,
    )

fun createTestSecurityUser() =
    SecurityUser(
        createTestUser(),
    )
