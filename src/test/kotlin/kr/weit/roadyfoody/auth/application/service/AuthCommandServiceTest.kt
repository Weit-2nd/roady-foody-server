package kr.weit.roadyfoody.auth.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import kr.weit.roadyfoody.auth.exception.InvalidTokenException
import kr.weit.roadyfoody.auth.exception.UserAlreadyExistsException
import kr.weit.roadyfoody.auth.exception.UserNotRegisteredException
import kr.weit.roadyfoody.auth.fixture.TEST_ACCESS_TOKEN
import kr.weit.roadyfoody.auth.fixture.TEST_BEARER_ACCESS_TOKEN
import kr.weit.roadyfoody.auth.fixture.TEST_BEARER_REFRESH_TOKEN
import kr.weit.roadyfoody.auth.fixture.TEST_REFRESH_TOKEN
import kr.weit.roadyfoody.auth.fixture.TEST_ROTATE_ID
import kr.weit.roadyfoody.auth.fixture.TEST_SOCIAL_ACCESS_TOKEN
import kr.weit.roadyfoody.auth.fixture.createTestKakaoUserResponse
import kr.weit.roadyfoody.auth.fixture.createTestSignUpRequest
import kr.weit.roadyfoody.auth.security.jwt.JwtUtil
import kr.weit.roadyfoody.global.service.ImageService
import kr.weit.roadyfoody.support.utils.ImageFormat.WEBP
import kr.weit.roadyfoody.support.utils.createTestImageFile
import kr.weit.roadyfoody.term.application.service.TermCommandService
import kr.weit.roadyfoody.user.domain.User
import kr.weit.roadyfoody.user.fixture.TEST_USER_SOCIAL_ID
import kr.weit.roadyfoody.user.fixture.createTestUser
import kr.weit.roadyfoody.user.repository.UserRepository
import kr.weit.roadyfoody.useragreedterm.application.service.UserAgreedTermCommandService
import org.springframework.web.multipart.MultipartFile
import javax.crypto.SecretKey

class AuthCommandServiceTest : BehaviorSpec({
    val authQueryService = mockk<AuthQueryService>()
    val termCommandService = mockk<TermCommandService>()
    val userAgreedTermCommandService = mockk<UserAgreedTermCommandService>()
    val userRepository = mockk<UserRepository>()
    val imageService = spyk<ImageService>(ImageService(mockk()))
    val jwtUtil = mockk<JwtUtil>()
    val authCommandService =
        AuthCommandService(authQueryService, termCommandService, userAgreedTermCommandService, userRepository, imageService, jwtUtil)

    afterEach { clearAllMocks() }

    given("register 테스트") {
        beforeEach {
            every { authQueryService.requestKakaoUserInfo(any<String>()) } returns createTestKakaoUserResponse()
            every { termCommandService.checkRequiredTermsOrThrow(any<Set<Long>>()) } just runs
            every { userRepository.save(any<User>()) } returns mockk<User>()
            every { userAgreedTermCommandService.storeUserAgreedTerms(any<User>(), any<Set<Long>>()) } just runs
            every { imageService.upload(any<String>(), any<MultipartFile>()) } just runs
        }
        `when`("프로필 이미지가 없으면 ") {
            every { userRepository.existsBySocialId(any<String>()) } returns false
            every { userRepository.existsByProfileNickname(any<String>()) } returns false
            then("이미지를 업로드하지 않고 User 가 생성된다.") {
                authCommandService.register(TEST_SOCIAL_ACCESS_TOKEN, createTestSignUpRequest(), null)
                verify(exactly = 1) {
                    authQueryService.requestKakaoUserInfo(any<String>())
                    userRepository.existsBySocialId(any<String>())
                    userRepository.existsByProfileNickname(any<String>())
                    termCommandService.checkRequiredTermsOrThrow(any<Set<Long>>())
                    userRepository.save(any<User>())
                    userAgreedTermCommandService.storeUserAgreedTerms(any<User>(), any<Set<Long>>())
                }
                verify(exactly = 0) {
                    imageService.generateImageName(any<MultipartFile>())
                    imageService.upload(any<String>(), any<MultipartFile>())
                }
            }
        }

        `when`("프로필 이미지가 있으면") {
            every { userRepository.existsBySocialId(any<String>()) } returns false
            every { userRepository.existsByProfileNickname(any<String>()) } returns false
            then("이미지를 업로드하고 User 가 생성된다.") {
                authCommandService.register(
                    TEST_SOCIAL_ACCESS_TOKEN,
                    createTestSignUpRequest(),
                    createTestImageFile(WEBP),
                )
                verify(exactly = 1) {
                    authQueryService.requestKakaoUserInfo(any<String>())
                    userRepository.existsBySocialId(any<String>())
                    userRepository.existsByProfileNickname(any<String>())
                    termCommandService.checkRequiredTermsOrThrow(any<Set<Long>>())
                    imageService.generateImageName(any<MultipartFile>())
                    userRepository.save(any<User>())
                    userAgreedTermCommandService.storeUserAgreedTerms(any<User>(), any<Set<Long>>())
                    imageService.upload(any<String>(), any<MultipartFile>())
                }
            }
        }

        `when`("이미 가입된 사용자가 있으면") {
            every { userRepository.existsBySocialId(any<String>()) } returns true
            every { userRepository.existsByProfileNickname(any<String>()) } returns false
            then("UserAlreadyExistsException 예외가 발생한다.") {
                shouldThrow<UserAlreadyExistsException> {
                    authCommandService.register(
                        TEST_SOCIAL_ACCESS_TOKEN,
                        createTestSignUpRequest(),
                        createTestImageFile(WEBP),
                    )
                }
                verify(exactly = 1) {
                    authQueryService.requestKakaoUserInfo(any<String>())
                    userRepository.existsBySocialId(any<String>())
                }
                verify(exactly = 0) {
                    userRepository.existsByProfileNickname(any<String>())
                    termCommandService.checkRequiredTermsOrThrow(any<Set<Long>>())
                    imageService.generateImageName(any<MultipartFile>())
                    userRepository.save(any<User>())
                    userAgreedTermCommandService.storeUserAgreedTerms(any<User>(), any<Set<Long>>())
                    imageService.upload(any<String>(), any<MultipartFile>())
                }
            }
        }

        `when`("중복된 닉네임이라면") {
            every { userRepository.existsBySocialId(any<String>()) } returns false
            every { userRepository.existsByProfileNickname(any<String>()) } returns true
            then("UserAlreadyExistsException 예외가 발생한다.") {
                shouldThrow<UserAlreadyExistsException> {
                    authCommandService.register(
                        TEST_SOCIAL_ACCESS_TOKEN,
                        createTestSignUpRequest(),
                        createTestImageFile(WEBP),
                    )
                }
                verify(exactly = 1) {
                    authQueryService.requestKakaoUserInfo(any<String>())
                    userRepository.existsBySocialId(any<String>())
                    userRepository.existsByProfileNickname(any<String>())
                }
                verify(exactly = 0) {
                    termCommandService.checkRequiredTermsOrThrow(any<Set<Long>>())
                    imageService.generateImageName(any<MultipartFile>())
                    userRepository.save(any<User>())
                    userAgreedTermCommandService.storeUserAgreedTerms(any<User>(), any<Set<Long>>())
                    imageService.upload(any<String>(), any<MultipartFile>())
                }
            }
        }
    }

    given("login 테스트") {
        `when`("가입된 사용자가 있으면") {
            every { authQueryService.requestKakaoUserInfo(any<String>()) } returns createTestKakaoUserResponse()
            every { userRepository.existsBySocialId(any<String>()) } returns true
            every { jwtUtil.accessTokenExpirationTime } returns 1000
            every { jwtUtil.generateAccessToken(any<String>(), any<Long>()) } returns TEST_ACCESS_TOKEN
            every { jwtUtil.generateRotateId() } returns TEST_ROTATE_ID
            every { jwtUtil.refreshTokenExpirationTime } returns 5 * 1000
            every { jwtUtil.generateRefreshToken(any<String>(), any<String>(), any<Long>()) } returns TEST_REFRESH_TOKEN
            every { jwtUtil.storeCachedRefreshTokenRotateId(any<String>(), any<String>()) } just runs
            then("ServiceTokensResponse 가 반환된다.") {
                val response = authCommandService.login(TEST_SOCIAL_ACCESS_TOKEN)
                response.accessToken shouldBe TEST_ACCESS_TOKEN
                response.refreshToken shouldBe TEST_REFRESH_TOKEN
                verify(exactly = 1) {
                    authQueryService.requestKakaoUserInfo(any<String>())
                    userRepository.existsBySocialId(any<String>())
                    jwtUtil.generateAccessToken(any<String>(), any<Long>())
                    jwtUtil.generateRotateId()
                    jwtUtil.generateRefreshToken(any<String>(), any<String>(), any<Long>())
                    jwtUtil.storeCachedRefreshTokenRotateId(any<String>(), any<String>())
                }
            }
        }

        `when`("가입된 사용자가 없으면") {
            every { authQueryService.requestKakaoUserInfo(any<String>()) } returns createTestKakaoUserResponse()
            every { userRepository.existsBySocialId(any<String>()) } returns false
            then("UserNotRegisteredException 예외가 발생한다.") {
                shouldThrow<UserNotRegisteredException> {
                    authCommandService.login(TEST_SOCIAL_ACCESS_TOKEN)
                }
                verify(exactly = 1) {
                    authQueryService.requestKakaoUserInfo(any<String>())
                    userRepository.existsBySocialId(any<String>())
                }
            }
        }
    }

    given("reissueTokens 테스트") {
        beforeEach { every { jwtUtil.refreshKey } returns mockk<SecretKey>() }
        `when`("유효한 refreshToken 이면") {
            every { jwtUtil.validateToken(any<SecretKey>(), any<String>()) } returns true
            every { jwtUtil.validateCachedRefreshTokenRotateId(any<String>()) } returns true
            every { jwtUtil.getSocialId(any<SecretKey>(), any<String>()) } returns TEST_USER_SOCIAL_ID
            every { jwtUtil.accessTokenExpirationTime } returns 1000
            every { jwtUtil.generateAccessToken(any<String>(), any<Long>()) } returns TEST_ACCESS_TOKEN
            every { jwtUtil.generateRotateId() } returns TEST_ROTATE_ID
            every { jwtUtil.refreshTokenExpirationTime } returns 5 * 1000
            every { jwtUtil.generateRefreshToken(any<String>(), any<String>(), any<Long>()) } returns TEST_REFRESH_TOKEN
            every { jwtUtil.storeCachedRefreshTokenRotateId(any<String>(), any<String>()) } just runs
            then("ServiceTokensResponse 가 반환된다.") {
                val actual = authCommandService.reissueTokens(TEST_BEARER_REFRESH_TOKEN)
                actual.accessToken shouldBe TEST_ACCESS_TOKEN
                actual.refreshToken shouldBe TEST_REFRESH_TOKEN
                verify(exactly = 1) {
                    jwtUtil.validateToken(any<SecretKey>(), any<String>())
                    jwtUtil.validateCachedRefreshTokenRotateId(any<String>())
                    jwtUtil.getSocialId(any<SecretKey>(), any<String>())
                    jwtUtil.generateAccessToken(any<String>(), any<Long>())
                    jwtUtil.generateRotateId()
                    jwtUtil.generateRefreshToken(any<String>(), any<String>(), any<Long>())
                    jwtUtil.storeCachedRefreshTokenRotateId(any<String>(), any<String>())
                }
                verify(exactly = 2) { jwtUtil.refreshKey }
            }
        }

        `when`("유효하지 않은 refreshToken 이면") {
            every { jwtUtil.validateToken(any<SecretKey>(), any<String>()) } returns false
            then("InvalidTokenException 예외가 발생한다.") {
                shouldThrow<InvalidTokenException> {
                    authCommandService.reissueTokens(TEST_BEARER_ACCESS_TOKEN)
                }
                verify(exactly = 1) {
                    jwtUtil.refreshKey
                    jwtUtil.validateToken(any<SecretKey>(), any<String>())
                }
            }
        }

        `when`("일치하지 않은 refreshTokenRotateId 이면") {
            every { jwtUtil.validateToken(any<SecretKey>(), any<String>()) } returns true
            every { jwtUtil.validateCachedRefreshTokenRotateId(any<String>()) } returns false
            then("InvalidTokenException 예외가 발생한다.") {
                shouldThrow<InvalidTokenException> {
                    authCommandService.reissueTokens(TEST_BEARER_REFRESH_TOKEN)
                }
                verify(exactly = 1) {
                    jwtUtil.refreshKey
                    jwtUtil.validateToken(any<SecretKey>(), any<String>())
                    jwtUtil.validateCachedRefreshTokenRotateId(any<String>())
                }
            }
        }
    }

    given("logout 테스트") {
        `when`("로그아웃 요청이 들어오면") {
            every { jwtUtil.removeCachedRefreshToken(any<String>()) } just runs
            then("jwtUtil.removeCachedRefreshToken 이 호출된다.") {
                authCommandService.logout(createTestUser())
                verify(exactly = 1) { jwtUtil.removeCachedRefreshToken(any<String>()) }
            }
        }
    }
})
