package kr.weit.roadyfoody.auth.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
class JwtProperties(
    val accessKey: String = generateHmac256Key(),
    val refreshKey: String = generateHmac256Key(),
    val accessTokenExpirationTime: Long = 5 * 60 * 1000,
    val refreshTokenExpirationTime: Long = 30 * 60 * 1000,
)
