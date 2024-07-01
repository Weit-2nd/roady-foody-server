package kr.weit.roadyfoody.auth.application.service

import com.ninjasquad.springmockk.MockkBean
import io.awspring.cloud.s3.S3Template
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.verify
import kr.weit.roadyfoody.auth.application.dto.ServiceTokensResponse
import kr.weit.roadyfoody.auth.exception.InvalidTokenException
import kr.weit.roadyfoody.auth.exception.UserAlreadyExistsException
import kr.weit.roadyfoody.auth.fixture.TEST_SOCIAL_ACCESS_TOKEN
import kr.weit.roadyfoody.auth.fixture.createTestKakaoUserResponse
import kr.weit.roadyfoody.auth.fixture.createTestSignUpRequest
import kr.weit.roadyfoody.auth.security.jwt.JwtUtil.Companion.getRefreshTokenCacheKey
import kr.weit.roadyfoody.global.config.S3Properties
import kr.weit.roadyfoody.global.service.StorageService.Companion.getObjectStorageCacheKey
import kr.weit.roadyfoody.support.annotation.ServiceIntegrateTest
import kr.weit.roadyfoody.support.utils.ImageFormat.WEBP
import kr.weit.roadyfoody.support.utils.createTestImageFile
import kr.weit.roadyfoody.term.fixture.createTestTerms
import kr.weit.roadyfoody.term.repository.TermRepository
import kr.weit.roadyfoody.user.fixture.TEST_USER_NICKNAME
import kr.weit.roadyfoody.user.fixture.TEST_USER_SOCIAL_ID
import kr.weit.roadyfoody.user.fixture.createTestUser
import kr.weit.roadyfoody.user.repository.UserRepository
import kr.weit.roadyfoody.user.repository.getByNickname
import kr.weit.roadyfoody.useragreedterm.exception.RequiredTermNotAgreedException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.transaction.annotation.Transactional

@Transactional
@ServiceIntegrateTest
class AuthIntegrationServiceTest(
    private val s3Template: S3Template,
    private val s3Properties: S3Properties,
    private val userRepository: UserRepository,
    private val termRepository: TermRepository,
    @MockkBean private val authQueryService: AuthQueryService,
    private val authCommandService: AuthCommandService,
    private val redisTemplate: StringRedisTemplate,
) : BehaviorSpec({
        lateinit var validTermIdSet: Set<Long>
        beforeSpec {
            s3Template.createBucket(s3Properties.bucket)
            validTermIdSet = termRepository.saveAll(createTestTerms()).map { it.id }.toSet()
        }
        beforeEach {
            every { authQueryService.requestKakaoUserInfo(any<String>()) } returns createTestKakaoUserResponse()
        }
        afterEach {
            userRepository.findAll()
                .forEach {
                    it.profile.profileImageName?.let { imageName ->
                        s3Template.deleteObject(s3Properties.bucket, imageName)
                    }
                }
            clearAllMocks()
            redisTemplate.opsForValue().operations.delete(getObjectStorageCacheKey(TEST_USER_SOCIAL_ID))
        }
        afterSpec {
            termRepository.deleteAll()
            s3Template.deleteBucket(s3Properties.bucket)
        }

        given("프로필 사진이 존재하는 경우") {
            `when`("회원가입을 요청하면") {
                then("회원가입이 성공한다") {
                    val signUpRequest = createTestSignUpRequest(termIdSet = validTermIdSet)
                    authCommandService.register(TEST_SOCIAL_ACCESS_TOKEN, signUpRequest, createTestImageFile(WEBP))
                    val profileImageName = userRepository.getByNickname(signUpRequest.nickname).profile.profileImageName
                    profileImageName.shouldNotBeNull()
                    s3Template.objectExists(s3Properties.bucket, profileImageName).shouldBeTrue()
                    verify(exactly = 1) { authQueryService.requestKakaoUserInfo(any<String>()) }
                }
            }
        }

        given("프로필 사진이 존재하지 않는 경우") {
            `when`("회원가입을 요청하면") {
                then("회원가입이 성공한다") {
                    val signUpRequest = createTestSignUpRequest(termIdSet = validTermIdSet)
                    authCommandService.register(TEST_SOCIAL_ACCESS_TOKEN, signUpRequest, null)
                    val profileImageName = userRepository.getByNickname(signUpRequest.nickname).profile.profileImageName
                    profileImageName.shouldBeNull()
                    verify(exactly = 1) { authQueryService.requestKakaoUserInfo(any<String>()) }
                }
            }
        }

        given("필수약관을 동의하지 않은 경우") {
            `when`("회원가입을 요청하면") {
                then("RequiredTermNotAgreedException 을 던진다") {
                    shouldThrow<RequiredTermNotAgreedException> {
                        authCommandService.register(
                            TEST_SOCIAL_ACCESS_TOKEN,
                            createTestSignUpRequest(termIdSet = emptySet()),
                            null,
                        )
                    }
                    verify(exactly = 1) { authQueryService.requestKakaoUserInfo(any<String>()) }
                }
            }
        }

        given("이미 가입된 사용자인 경우") {
            beforeContainer {
                userRepository.save(createTestUser(socialId = TEST_USER_SOCIAL_ID))
            }
            afterContainer {
                userRepository.deleteAll()
            }
            `when`("회원가입을 요청하면") {
                then("UserAlreadyExistsException 을 던진다") {
                    shouldThrow<UserAlreadyExistsException> {
                        authCommandService.register(
                            TEST_SOCIAL_ACCESS_TOKEN,
                            createTestSignUpRequest(termIdSet = validTermIdSet),
                            null,
                        )
                    }
                    verify(exactly = 1) { authQueryService.requestKakaoUserInfo(any<String>()) }
                }
            }
        }

        given("중복된 닉네임인 경우") {
            beforeContainer {
                userRepository.save(createTestUser(nickname = TEST_USER_NICKNAME))
            }
            afterContainer {
                userRepository.deleteAll()
            }
            `when`("회원가입을 요청하면") {
                then("UserAlreadyExistsException 을 던진다") {
                    shouldThrow<UserAlreadyExistsException> {
                        authCommandService.register(
                            TEST_SOCIAL_ACCESS_TOKEN,
                            createTestSignUpRequest(nickname = TEST_USER_NICKNAME, termIdSet = validTermIdSet),
                            null,
                        )
                    }
                    verify(exactly = 1) { authQueryService.requestKakaoUserInfo(any<String>()) }
                }
            }
        }

        given("login 메소드") {
            `when`("유효한 socialAccessToken 을 전달하면") {
                then("로그인이 성공한다") {
                    authCommandService.register(TEST_SOCIAL_ACCESS_TOKEN, createTestSignUpRequest(termIdSet = validTermIdSet), null)
                    val tokensResponse = authCommandService.login(TEST_SOCIAL_ACCESS_TOKEN)
                    val isStored = redisTemplate.hasKey(getRefreshTokenCacheKey(TEST_USER_SOCIAL_ID))
                    tokensResponse.shouldNotBeNull()
                    isStored.shouldBeTrue()
                    verify(exactly = 2) { authQueryService.requestKakaoUserInfo(any<String>()) }
                }
            }
        }

        given("reissueTokens 메소드") {
            lateinit var tokensResponse: ServiceTokensResponse
            beforeEach {
                authCommandService.register(TEST_SOCIAL_ACCESS_TOKEN, createTestSignUpRequest(termIdSet = validTermIdSet), null)
                tokensResponse = authCommandService.login(TEST_SOCIAL_ACCESS_TOKEN)
            }
            afterEach { redisTemplate.delete(getRefreshTokenCacheKey(TEST_USER_SOCIAL_ID)) }
            `when`("유효한 refreshToken 을 전달하면") {
                then("토큰 재발급이 성공한다") {
                    val reissuedTokensResponse = authCommandService.reissueTokens(tokensResponse.refreshToken)
                    val isStored = redisTemplate.hasKey(getRefreshTokenCacheKey(TEST_USER_SOCIAL_ID))
                    reissuedTokensResponse.shouldNotBeNull()
                    isStored.shouldBeTrue()
                    verify(exactly = 2) { authQueryService.requestKakaoUserInfo(any<String>()) }
                }
            }

            `when`("캐시된 RotateId 가 소멸한 뒤 RefreshToken 을 전달하면") {
                then("InvalidTokenException 을 던진다") {
                    redisTemplate.delete(getRefreshTokenCacheKey(TEST_USER_SOCIAL_ID))
                    shouldThrow<InvalidTokenException> {
                        authCommandService.reissueTokens(tokensResponse.refreshToken)
                    }
                    val isStored = redisTemplate.hasKey(getRefreshTokenCacheKey(TEST_USER_SOCIAL_ID))
                    isStored.shouldBeFalse()
                    verify(exactly = 2) { authQueryService.requestKakaoUserInfo(any<String>()) }
                }
            }

            `when`("이미 한 번 사용된 refreshToken 을 전달하면") {
                then("InvalidTokenException 을 던진다") {
                    val prevReissuedTokensResponse = authCommandService.reissueTokens(tokensResponse.refreshToken)
                    authCommandService.reissueTokens(prevReissuedTokensResponse.refreshToken)
                    shouldThrow<InvalidTokenException> {
                        authCommandService.reissueTokens(prevReissuedTokensResponse.refreshToken)
                    }
                    verify(exactly = 2) { authQueryService.requestKakaoUserInfo(any<String>()) }
                }
            }
        }

        given("logout 메소드") {
            `when`("Refresh Token 이 캐시에 저장되어있다면") {
                then("이를 제거하고 로그아웃이 성공한다") {
                    authCommandService.register(TEST_SOCIAL_ACCESS_TOKEN, createTestSignUpRequest(termIdSet = validTermIdSet), null)
                    val user = userRepository.getByNickname(TEST_USER_NICKNAME)
                    authCommandService.login(user.socialId)
                    authCommandService.logout(user)
                    val actual = redisTemplate.hasKey(getRefreshTokenCacheKey(TEST_USER_SOCIAL_ID))
                    actual.shouldBeFalse()
                    verify(exactly = 2) { authQueryService.requestKakaoUserInfo(any<String>()) }
                }
            }

            `when`("Refresh Token 이 캐시에 저장되어있지 않다면") {
                then("아무런 동작을 하지 않는다") {
                    val user = userRepository.save(createTestUser())
                    authCommandService.logout(user)
                    val actual = redisTemplate.hasKey(getRefreshTokenCacheKey(user.socialId))
                    actual.shouldBeFalse()
                }
            }
        }
    })
