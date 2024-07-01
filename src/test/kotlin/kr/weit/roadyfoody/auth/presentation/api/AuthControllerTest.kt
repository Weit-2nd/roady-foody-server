package kr.weit.roadyfoody.auth.presentation.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kr.weit.roadyfoody.auth.application.dto.DuplicatedNicknameResponse
import kr.weit.roadyfoody.auth.application.dto.SignUpRequest
import kr.weit.roadyfoody.auth.application.service.AuthCommandService
import kr.weit.roadyfoody.auth.application.service.AuthQueryService
import kr.weit.roadyfoody.auth.fixture.PROFILE_IMAGE_FILE_NAME
import kr.weit.roadyfoody.auth.fixture.SIGN_UP_REQUEST_FILE_NAME
import kr.weit.roadyfoody.auth.fixture.TEST_BEARER_REFRESH_TOKEN
import kr.weit.roadyfoody.auth.fixture.TEST_BEARER_SOCIAL_ACCESS_TOKEN
import kr.weit.roadyfoody.auth.fixture.TEST_REFRESH_TOKEN
import kr.weit.roadyfoody.auth.fixture.createTestSignUpRequest
import kr.weit.roadyfoody.auth.fixture.createTestTokensResponse
import kr.weit.roadyfoody.support.annotation.ControllerTest
import kr.weit.roadyfoody.support.utils.ImageFormat.GIF
import kr.weit.roadyfoody.support.utils.ImageFormat.JPEG
import kr.weit.roadyfoody.support.utils.ImageFormat.PNG
import kr.weit.roadyfoody.support.utils.ImageFormat.WEBP
import kr.weit.roadyfoody.support.utils.createMultipartFile
import kr.weit.roadyfoody.support.utils.createTestImageFile
import kr.weit.roadyfoody.support.utils.postWithAuth
import kr.weit.roadyfoody.user.fixture.TEST_USER_NICKNAME
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.multipart.MultipartFile

@WebMvcTest(AuthController::class)
@ControllerTest
class AuthControllerTest(
    private val objectMapper: ObjectMapper,
    @MockkBean private val authCommandService: AuthCommandService,
    @MockkBean private val authQueryService: AuthQueryService,
    private val mockMvc: MockMvc,
) : BehaviorSpec({
        val requestPath = "/api/v1/auth"

        given("POST $requestPath") {
            `when`("WEBP 이미지 프로필 사진을 업로드하면") {
                every { authCommandService.register(any<String>(), any<SignUpRequest>(), any<MultipartFile>()) } just runs
                then("회원가입에 성공하고 201 상태번호를 반환한다") {
                    mockMvc.perform(
                        multipart(requestPath)
                            .file(createTestImageFile(WEBP))
                            .file(
                                createMultipartFile(
                                    SIGN_UP_REQUEST_FILE_NAME,
                                    objectMapper.writeValueAsString(createTestSignUpRequest()).byteInputStream(),
                                ),
                            )
                            .header(AUTHORIZATION, TEST_BEARER_SOCIAL_ACCESS_TOKEN)
                            .contentType(MediaType.MULTIPART_FORM_DATA),
                    ).andExpect(status().isCreated)
                    verify(exactly = 1) { authCommandService.register(any<String>(), any<SignUpRequest>(), any<MultipartFile>()) }
                }
            }

            `when`("프로필사진을 업로드하지 않아도") {
                every { authCommandService.register(any<String>(), any<SignUpRequest>(), any<MultipartFile>()) } just runs
                then("회원가입에 성공하고 201 상태번호를 반환한다") {
                    mockMvc.perform(
                        multipart(requestPath)
                            .file(
                                createMultipartFile(
                                    SIGN_UP_REQUEST_FILE_NAME,
                                    objectMapper.writeValueAsString(createTestSignUpRequest()).byteInputStream(),
                                ),
                            )
                            .header(AUTHORIZATION, TEST_BEARER_SOCIAL_ACCESS_TOKEN)
                            .contentType(MediaType.MULTIPART_FORM_DATA),
                    ).andExpect(status().isCreated)
                    verify(exactly = 1) { authCommandService.register(any<String>(), any<SignUpRequest>(), any<MultipartFile>()) }
                }
            }

            `when`("WEBP 이미지가 아닌 프로필 사진을 업로드하면") {
                every { authCommandService.register(any<String>(), any<SignUpRequest>(), any<MultipartFile>()) } just runs
                then("회원가입에 실패하고 400 상태번호를 반환한다") {
                    forAll(
                        row(JPEG),
                        row(PNG),
                        row(GIF),
                    ) { format ->
                        mockMvc.perform(
                            multipart(requestPath)
                                .file(createTestImageFile(format, PROFILE_IMAGE_FILE_NAME))
                                .file(
                                    createMultipartFile(
                                        SIGN_UP_REQUEST_FILE_NAME,
                                        objectMapper.writeValueAsString(createTestSignUpRequest()).byteInputStream(),
                                    ),
                                )
                                .header(AUTHORIZATION, TEST_BEARER_SOCIAL_ACCESS_TOKEN)
                                .contentType(MediaType.MULTIPART_FORM_DATA),
                        ).andExpect(status().isBadRequest)
                    }
                    verify(exactly = 0) { authCommandService.register(any<String>(), any<SignUpRequest>(), any<MultipartFile>()) }
                }
            }

            `when`("파일의 크기가 1MB를 초과하면") {
                every { authCommandService.register(any<String>(), any<SignUpRequest>(), any<MultipartFile>()) } just runs
                val mockFile: MockMultipartFile = mockk<MockMultipartFile>()
                every { mockFile.size } returns 1024 * 1024 + 1
                every { mockFile.name } returns PROFILE_IMAGE_FILE_NAME
                every { mockFile.inputStream } returns createTestImageFile(WEBP).inputStream
                then("회원가입에 실패하고 400 상태번호를 반환한다") {
                    mockMvc.perform(
                        multipart(requestPath)
                            .file(mockFile)
                            .file(
                                createMultipartFile(
                                    SIGN_UP_REQUEST_FILE_NAME,
                                    objectMapper.writeValueAsString(createTestSignUpRequest()).byteInputStream(),
                                ),
                            )
                            .header(AUTHORIZATION, TEST_BEARER_SOCIAL_ACCESS_TOKEN)
                            .contentType(MediaType.MULTIPART_FORM_DATA),
                    ).andExpect(status().isBadRequest)
                    verify(exactly = 0) {
                        authCommandService.register(any<String>(), any<SignUpRequest>(), any<MultipartFile>())
                    }
                    verify {
                        mockFile.size
                        mockFile.name
                        mockFile.inputStream
                    }
                }
            }

            `when`("소셜 로그인 AccessToken 이 없으면") {
                every { authCommandService.register(any<String>(), any<SignUpRequest>(), any<MultipartFile>()) } just runs
                then("회원가입에 실패하고 400 상태번호를 반환한다") {
                    mockMvc.perform(
                        multipart(requestPath)
                            .file(createTestImageFile(WEBP))
                            .file(
                                createMultipartFile(
                                    SIGN_UP_REQUEST_FILE_NAME,
                                    objectMapper.writeValueAsString(createTestSignUpRequest()).byteInputStream(),
                                ),
                            )
                            .contentType(MediaType.MULTIPART_FORM_DATA),
                    ).andExpect(status().isBadRequest)
                    verify(exactly = 0) { authCommandService.register(any<String>(), any<SignUpRequest>(), any<MultipartFile>()) }
                }
            }
        }

        given("GET $requestPath/check-nickname") {
            `when`("중복되지 않는 닉네임을 전달하면") {
                every { authQueryService.checkDuplicatedNickname(any()) } returns DuplicatedNicknameResponse(false)
                then("200 상태번호와 내용이 false 인 응답을 반환한다") {
                    mockMvc.perform(
                        get("$requestPath/check-nickname")
                            .param("nickname", TEST_USER_NICKNAME),
                    )
                        .andExpect(status().isOk)
                        .andExpect {
                            content().json(
                                objectMapper.writeValueAsString(DuplicatedNicknameResponse(false)),
                            )
                        }
                    verify(exactly = 1) { authQueryService.checkDuplicatedNickname(any()) }
                }
            }

            `when`("중복되는 닉네임을 전달하면") {
                every { authQueryService.checkDuplicatedNickname(any()) } returns DuplicatedNicknameResponse(true)
                then("200 상태번호와 내용이 true 인 응답을 반환한다") {
                    mockMvc.perform(
                        get("$requestPath/check-nickname")
                            .param("nickname", TEST_USER_NICKNAME),
                    )
                        .andExpect(status().isOk)
                        .andExpect {
                            content().json(
                                objectMapper.writeValueAsString(DuplicatedNicknameResponse(true)),
                            )
                        }
                    verify(exactly = 1) { authQueryService.checkDuplicatedNickname(any()) }
                }
            }
        }

        given("GET $requestPath") {
            `when`("소셜 로그인 AccessToken 을 전달하면") {
                val tokensResponse = createTestTokensResponse()
                every { authCommandService.login(any()) } returns tokensResponse
                then("200 상태번호와 ServiceTokensResponse 를 반환한다") {
                    mockMvc.perform(
                        get(requestPath)
                            .header(AUTHORIZATION, TEST_BEARER_SOCIAL_ACCESS_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andExpect {
                            content().json(
                                objectMapper.writeValueAsString(tokensResponse),
                            )
                        }
                    verify(exactly = 1) { authCommandService.login(any()) }
                }
            }

            `when`("소셜 로그인 AccessToken 이 없으면") {
                then("400 상태번호를 반환한다") {
                    mockMvc.perform(
                        get(requestPath),
                    ).andExpect(status().isBadRequest)
                }
            }
        }

        given("GET $requestPath/refresh") {
            `when`("리프레시 토큰을 전달하면") {
                val tokensResponse = createTestTokensResponse()
                every { authCommandService.reissueTokens(any()) } returns tokensResponse
                then("200 상태번호와 ServiceTokensResponse 를 반환한다") {
                    mockMvc.perform(
                        get("$requestPath/refresh")
                            .header(AUTHORIZATION, TEST_BEARER_REFRESH_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andExpect {
                            content().json(
                                objectMapper.writeValueAsString(tokensResponse),
                            )
                        }
                    verify(exactly = 1) { authCommandService.reissueTokens(any()) }
                }
            }

            `when`("리프레시 토큰이 없으면") {
                every { authCommandService.reissueTokens(any()) } returns mockk()
                then("400 상태번호를 반환한다") {
                    mockMvc.perform(
                        get("$requestPath/refresh"),
                    ).andExpect(status().isBadRequest)
                    verify(exactly = 0) { authCommandService.reissueTokens(any()) }
                }
            }

            `when`("Bearer 토큰이 아니라면") {
                every { authCommandService.reissueTokens(any()) } returns mockk()
                then("400 상태번호를 반환한다") {
                    mockMvc.perform(
                        get("$requestPath/refresh")
                            .header(AUTHORIZATION, TEST_REFRESH_TOKEN),
                    ).andExpect(status().isBadRequest)
                    verify(exactly = 0) { authCommandService.reissueTokens(any()) }
                }
            }
        }

        given("POST $requestPath/sign-out") {
            `when`("로그인 사용자 정보를 전달하면") {
                every { authCommandService.logout(any()) } just runs
                then("로그아웃에 성공하고 200 상태번호를 반환한다") {
                    mockMvc.perform(
                        postWithAuth("$requestPath/sign-out"),
                    ).andExpect(status().isOk)
                    verify(exactly = 1) { authCommandService.logout(any()) }
                }
            }

            `when`("로그인 사용자 정보가 없으면") {
                every { authCommandService.logout(any()) } just runs
                then("401 상태번호를 반환한다") {
                    mockMvc.perform(
                        post("$requestPath/sign-out"),
                    ).andExpect(status().isUnauthorized)
                    verify(exactly = 0) { authCommandService.logout(any()) }
                }
            }
        }
    })
