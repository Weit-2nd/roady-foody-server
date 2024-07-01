package kr.weit.roadyfoody.auth.application.service

import kr.weit.roadyfoody.auth.application.dto.ServiceTokensResponse
import kr.weit.roadyfoody.auth.application.dto.SignUpRequest
import kr.weit.roadyfoody.auth.exception.InvalidTokenException
import kr.weit.roadyfoody.auth.exception.UserAlreadyExistsException
import kr.weit.roadyfoody.auth.exception.UserNotRegisteredException
import kr.weit.roadyfoody.auth.security.jwt.JwtUtil
import kr.weit.roadyfoody.global.service.ImageService
import kr.weit.roadyfoody.term.application.service.TermCommandService
import kr.weit.roadyfoody.user.domain.SocialLoginType
import kr.weit.roadyfoody.user.domain.User
import kr.weit.roadyfoody.user.repository.UserRepository
import kr.weit.roadyfoody.useragreedterm.application.service.UserAgreedTermCommandService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class AuthCommandService(
    private val authQueryService: AuthQueryService,
    private val termCommandService: TermCommandService,
    private val userAgreedTermCommandService: UserAgreedTermCommandService,
    private val userRepository: UserRepository,
    private val imageService: ImageService,
    private val jwtUtil: JwtUtil,
) {
    @Transactional
    fun register(
        socialAccessToken: String,
        signUpRequest: SignUpRequest,
        profileImage: MultipartFile?,
    ) {
        val socialId = obtainUserSocialId(signUpRequest.socialLoginType, socialAccessToken)

        if (userRepository.existsBySocialId(socialId) ||
            userRepository.existsByProfileNickname(signUpRequest.nickname)
        ) {
            throw UserAlreadyExistsException()
        }
        termCommandService.checkRequiredTermsOrThrow(signUpRequest.agreedTermIds)

        val user = User.of(socialId, signUpRequest.nickname)
        userRepository.save(user)
        userAgreedTermCommandService.storeUserAgreedTerms(user, signUpRequest.agreedTermIds)

        if (profileImage != null) {
            val imageName = imageService.generateImageName(profileImage)
            user.profile.changeProfileImageName(imageName)
            imageService.upload(imageName, profileImage)
        }
    }

    fun login(socialAccessToken: String): ServiceTokensResponse {
        val userSocialId = obtainUserSocialId(SocialLoginType.KAKAO, socialAccessToken)
        if (!userRepository.existsBySocialId(userSocialId)) {
            throw UserNotRegisteredException()
        }
        val accessToken = jwtUtil.generateAccessToken(userSocialId)
        val rotateId = jwtUtil.generateRotateId()
        val refreshToken = jwtUtil.generateRefreshToken(userSocialId, rotateId)
        jwtUtil.storeCachedRefreshTokenRotateId(userSocialId, rotateId)
        return ServiceTokensResponse(accessToken, refreshToken)
    }

    private fun obtainUserSocialId(
        socialLoginType: SocialLoginType,
        socialAccessToken: String,
    ): String = "$socialLoginType ${authQueryService.requestKakaoUserInfo(socialAccessToken).id}"

    fun reissueTokens(refreshToken: String): ServiceTokensResponse {
        if (!jwtUtil.validateToken(jwtUtil.refreshKey, refreshToken) ||
            !jwtUtil.validateCachedRefreshTokenRotateId(refreshToken)
        ) {
            throw InvalidTokenException()
        }
        val socialId = jwtUtil.getSocialId(jwtUtil.refreshKey, refreshToken)
        val newAccessToken = jwtUtil.generateAccessToken(socialId)
        val rotateId = jwtUtil.generateRotateId()
        val newRefreshToken = jwtUtil.generateRefreshToken(socialId, rotateId)
        jwtUtil.storeCachedRefreshTokenRotateId(socialId, rotateId)
        return ServiceTokensResponse(newAccessToken, newRefreshToken)
    }

    fun logout(user: User) {
        jwtUtil.removeCachedRefreshToken(user.socialId)
    }
}
