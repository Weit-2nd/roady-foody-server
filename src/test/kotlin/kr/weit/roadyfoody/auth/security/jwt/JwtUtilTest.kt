package kr.weit.roadyfoody.auth.security.jwt

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import kr.weit.roadyfoody.auth.fixture.TEST_INVALID_ACCESS_TOKEN
import kr.weit.roadyfoody.auth.fixture.TEST_ROTATE_ID
import kr.weit.roadyfoody.user.fixture.TEST_USER_SOCIAL_ID
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit

class JwtUtilTest : BehaviorSpec({
    val jwtProperties = spyk<JwtProperties>()
    val redisTemplate = mockk<StringRedisTemplate>()

    val jwtUtil = JwtUtil(jwtProperties, redisTemplate)

    afterEach { clearAllMocks() }

    given("generateAccessToken 메서드") {
        `when`("유효한 socialId 를 전달하면") {
            then("AccessToken 이 생성된다") {
                val actual = jwtUtil.generateAccessToken(TEST_USER_SOCIAL_ID)
                actual.shouldNotBeNull()
            }
        }
    }

    given("generateRefreshToken 메서드") {
        `when`("유효한 socialId 와 rotateId 를 전달하면") {
            then("RefreshToken 이 생성된다") {
                val actual = jwtUtil.generateRefreshToken(TEST_USER_SOCIAL_ID, TEST_ROTATE_ID)
                actual.shouldNotBeNull()
            }
        }
    }

    given("storeCachedRefreshTokenRotateId 메서드") {
        `when`("유효한 socialId 와 rotateId 를 전달하면") {
            every { redisTemplate.opsForValue().set(any<String>(), any<String>()) } just runs
            every { redisTemplate.expire(any<String>(), any<Long>(), any<TimeUnit>()) } returns true
            then("Redis 에 rotateId 가 저장된다") {
                jwtUtil.storeCachedRefreshTokenRotateId(TEST_USER_SOCIAL_ID, TEST_ROTATE_ID)
                verify(exactly = 1) {
                    redisTemplate.opsForValue().set(any<String>(), any<String>())
                    redisTemplate.expire(any<String>(), any<Long>(), any<TimeUnit>())
                }
            }
        }
    }

    given("generateRotateId 메서드") {
        `when`("호출하면") {
            then("RotateId 가 생성된다") {
                val actual = jwtUtil.generateRotateId()
                actual.shouldNotBeNull()
            }
        }
    }

    given("getRefreshTokenCacheKey 메서드") {
        `when`("유효한 socialId 를 전달하면") {
            then("RefreshTokenCacheKey 가 생성된다") {
                val actual = JwtUtil.getRefreshTokenCacheKey(TEST_USER_SOCIAL_ID)
                actual.shouldNotBeNull()
            }
        }
    }

    given("validateCachedRefreshTokenRotateId 메서드") {
        `when`("유효한 RefreshToken 을 전달하면") {
            every { redisTemplate.hasKey(any<String>()) } returns true
            val refreshToken = jwtUtil.generateRefreshToken(TEST_USER_SOCIAL_ID, jwtUtil.generateRotateId())
            every { redisTemplate.opsForValue().get(any<String>()) } returns jwtUtil.getRotateId(refreshToken)
            then("true 가 반환된다") {
                val actual = jwtUtil.validateCachedRefreshTokenRotateId(refreshToken)
                actual.shouldBeTrue()
                verify(exactly = 1) {
                    redisTemplate.hasKey(any<String>())
                    redisTemplate.opsForValue().get(any<String>())
                }
            }
        }

        `when`("캐시된 RotateId 가 사라진 뒤 유효한 RefreshToken 을 전달하면") {
            every { redisTemplate.hasKey(any<String>()) } returns false
            then("false 를 반환된다") {
                val refreshToken = jwtUtil.generateRefreshToken(TEST_USER_SOCIAL_ID, jwtUtil.generateRotateId())
                val actual = jwtUtil.validateCachedRefreshTokenRotateId(refreshToken)
                actual.shouldBeFalse()
                verify(exactly = 1) {
                    redisTemplate.hasKey(any<String>())
                }
            }
        }
    }

    given("getRotateId 메서드") {
        `when`("유효한 RefreshToken 을 전달하면") {
            then("RotateId 가 반환된다") {
                val refreshToken = jwtUtil.generateRefreshToken(TEST_USER_SOCIAL_ID, jwtUtil.generateRotateId())
                val actual = jwtUtil.getRotateId(refreshToken)
                actual.shouldNotBeNull()
            }
        }
    }

    given("getSocialId 메서드") {
        `when`("유효한 RefreshToken 을 전달하면") {
            then("SocialId 가 반환된다") {
                val accessToken = jwtUtil.generateAccessToken(TEST_USER_SOCIAL_ID)
                val actual = jwtUtil.getSocialId(jwtUtil.accessKey, accessToken)
                actual.shouldNotBeNull()
            }
        }
    }

    given("validateToken 메서드") {
        `when`("유효한 토큰을 전달하면") {
            then("true 가 반환된다") {
                forAll(
                    row(jwtUtil.accessKey, jwtUtil.generateAccessToken(TEST_USER_SOCIAL_ID)),
                    row(jwtUtil.refreshKey, jwtUtil.generateRefreshToken(TEST_USER_SOCIAL_ID, jwtUtil.generateRotateId())),
                ) { key, token ->
                    val actual = jwtUtil.validateToken(key, token)
                    actual.shouldBeTrue()
                }
            }
        }

        `when`("유효하지 않은 토큰을 전달하면") {
            then("false 가 반환된다") {
                val actual = jwtUtil.validateToken(jwtUtil.accessKey, TEST_INVALID_ACCESS_TOKEN)
                actual.shouldBeFalse()
            }
        }

        `when`("AccessKey 로 RefreshToken 을 검증하면") {
            then("false 가 반환된다") {
                val refreshToken = jwtUtil.generateRefreshToken(TEST_USER_SOCIAL_ID, jwtUtil.generateRotateId())
                val actual = jwtUtil.validateToken(jwtUtil.accessKey, refreshToken)
                actual.shouldBeFalse()
            }
        }

        `when`("RefreshKey 로 AccessToken 을 검증하면") {
            then("false 가 반환된다") {
                val accessToken = jwtUtil.generateAccessToken(TEST_USER_SOCIAL_ID)
                val actual = jwtUtil.validateToken(jwtUtil.refreshKey, accessToken)
                actual.shouldBeFalse()
            }
        }

        `when`("만료기간이 지난 토큰을 전달하면") {
            then("false 가 반환된다") {
                val accessToken = jwtUtil.generateAccessToken(TEST_USER_SOCIAL_ID, expirationTime = -10000L)
                val actual = jwtUtil.validateToken(jwtUtil.accessKey, accessToken)
                actual.shouldBeFalse()
            }
        }
    }
})
