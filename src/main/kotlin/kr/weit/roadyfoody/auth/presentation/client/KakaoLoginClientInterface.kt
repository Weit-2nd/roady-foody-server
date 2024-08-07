package kr.weit.roadyfoody.auth.presentation.client

import kr.weit.roadyfoody.auth.application.dto.KakaoUserResponse
import kr.weit.roadyfoody.global.annotation.ClientInterface
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.GetExchange

@ClientInterface
interface KakaoLoginClientInterface {
    @GetExchange("/v2/user/me")
    fun requestUserInfo(
        @RequestHeader(AUTHORIZATION) socialAccessToken: String,
    ): KakaoUserResponse
}
