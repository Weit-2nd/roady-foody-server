package kr.weit.roadyfoody.auth.security.jwt

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.MILLIS
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

@Component
class JwtUtil(
    private val jwtProperties: JwtProperties,
    private val redisTemplate: StringRedisTemplate,
) {
    val accessKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.accessKey))
    val refreshKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.refreshKey))
    val accessTokenExpirationTime: Long = jwtProperties.accessTokenExpirationTime
    val refreshTokenExpirationTime: Long = jwtProperties.refreshTokenExpirationTime

    val log: Logger = LoggerFactory.getLogger(JwtUtil::class.java)

    fun generateAccessToken(
        socialId: String,
        expirationTime: Long = accessTokenExpirationTime,
    ): String =
        generateJwtToken(
            mapOf(
                "socialId" to socialId,
            ),
            expirationTime,
            accessKey,
        )

    fun generateRefreshToken(
        socialId: String,
        rotateId: String,
        expirationTime: Long = refreshTokenExpirationTime,
    ): String =
        generateJwtToken(
            mapOf(
                "socialId" to socialId,
                "rotateId" to rotateId,
            ),
            expirationTime,
            refreshKey,
        )

    fun storeCachedRefreshTokenRotateId(
        socialId: String,
        rotateId: String,
    ) {
        redisTemplate.opsForValue().set(
            getRefreshTokenCacheKey(socialId),
            rotateId,
        )
        redisTemplate.expire(getRefreshTokenCacheKey(socialId), jwtProperties.refreshTokenExpirationTime, TimeUnit.MILLISECONDS)
    }

    companion object {
        private const val REFRESH_TOKEN_CACHE_PREFIX = "rofo:refresh-token-cache:"

        fun getRefreshTokenCacheKey(socialId: String): String {
            return REFRESH_TOKEN_CACHE_PREFIX.plus(socialId)
        }
    }

    private fun generateJwtToken(
        claims: Map<String, String>,
        expirationTime: Long,
        key: SecretKey,
    ): String =
        Jwts.builder()
            .claims(claims)
            .expiration(afterMillis(expirationTime))
            .signWith(key)
            .compact()

    private fun afterMillis(ms: Long): Date =
        Date.from(
            ZonedDateTime.now().plus(ms, MILLIS).toInstant(),
        )

    fun generateRotateId(): String = UUID.randomUUID().toString()

    fun validateCachedRefreshTokenRotateId(token: String): Boolean {
        if (!redisTemplate.hasKey(getRefreshTokenCacheKey(getSocialId(refreshKey, token)))) {
            return false
        }
        val cachedRotateId = redisTemplate.opsForValue().get(getRefreshTokenCacheKey(getSocialId(refreshKey, token)))
        val rotateIdInToken = getRotateId(token)
        return cachedRotateId == rotateIdInToken
    }

    fun getRotateId(token: String): String = parseClaims(refreshKey, token).get("rotateId", String::class.java)

    fun getSocialId(
        key: SecretKey,
        token: String,
    ): String = parseClaims(key, token).get("socialId", String::class.java)

    fun validateToken(
        key: SecretKey,
        token: String,
    ): Boolean {
        try {
            val expirationTime = parseClaims(key, token).expiration
            return expirationTime.after(Date.from(ZonedDateTime.now().toInstant()))
        } catch (ex: JwtException) {
            log.error("Jwt Exception: $token")
        } catch (ex: IllegalArgumentException) {
            log.error("Invalid Auth Token: $token")
        }
        return false
    }

    private fun parseClaims(
        key: SecretKey,
        token: String,
    ) = Jwts.parser()
        .verifyWith(key)
        .decryptWith(key)
        .build()
        .parseSignedClaims(token)
        .payload

    fun removeCachedRefreshToken(socialId: String) {
        redisTemplate.delete(getRefreshTokenCacheKey(socialId))
    }
}

fun String.isNotBearerToken(): Boolean = !this.startsWith("Bearer ")

fun String.removeBearer(): String = this.removePrefix("Bearer ")
