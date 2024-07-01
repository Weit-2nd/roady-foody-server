package kr.weit.roadyfoody.global.config

import kr.weit.roadyfoody.auth.security.jwt.JwtProperties
import kr.weit.roadyfoody.search.address.config.KakaoProperties
import kr.weit.roadyfoody.search.tourism.config.TourismProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(TourismProperties::class, S3Properties::class, KakaoProperties::class, JwtProperties::class)
@Configuration
class EnablePropertiesConfig
