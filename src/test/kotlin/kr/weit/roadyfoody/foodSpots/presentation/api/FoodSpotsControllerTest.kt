package kr.weit.roadyfoody.foodSpots.presentation.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kr.weit.roadyfoody.common.dto.SliceResponse
import kr.weit.roadyfoody.foodSpots.fixture.TEST_FOOD_SPOTS_LAST_ID
import kr.weit.roadyfoody.foodSpots.fixture.TEST_FOOD_SPOTS_REQUEST_NAME
import kr.weit.roadyfoody.foodSpots.fixture.TEST_FOOD_SPOTS_REQUEST_PHOTO
import kr.weit.roadyfoody.foodSpots.fixture.TEST_FOOD_SPOTS_SIZE
import kr.weit.roadyfoody.foodSpots.fixture.TEST_FOOD_SPOT_NAME_EMPTY
import kr.weit.roadyfoody.foodSpots.fixture.TEST_FOOD_SPOT_NAME_INVALID_STR
import kr.weit.roadyfoody.foodSpots.fixture.TEST_FOOD_SPOT_NAME_TOO_LONG
import kr.weit.roadyfoody.foodSpots.fixture.createMockPhotoList
import kr.weit.roadyfoody.foodSpots.fixture.createTestReportHistoriesResponse
import kr.weit.roadyfoody.foodSpots.fixture.createTestReportRequest
import kr.weit.roadyfoody.foodSpots.service.FoodSpotsService
import kr.weit.roadyfoody.support.annotation.ControllerTest
import kr.weit.roadyfoody.support.utils.ImageFormat
import kr.weit.roadyfoody.support.utils.ImageFormat.WEBP
import kr.weit.roadyfoody.support.utils.createMultipartFile
import kr.weit.roadyfoody.support.utils.createTestImageFile
import kr.weit.roadyfoody.support.utils.getWithAuth
import kr.weit.roadyfoody.support.utils.multipartWithAuth
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FoodSpotsController::class)
@ControllerTest
class FoodSpotsControllerTest(
    @MockkBean private val foodSpotsService: FoodSpotsService,
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
) : BehaviorSpec(
        {
            val requestPath = "/api/v1/food-spots"

            given("POST $requestPath Test") {
                var reportRequest = createTestReportRequest()
                var reportPhotos = createMockPhotoList(WEBP)
                every {
                    foodSpotsService.createReport(any(), any(), any())
                } returns Unit
                `when`("정상적인 데이터가 들어올 경우") {
                    then("가게 리포트가 등록된다.") {
                        mockMvc
                            .perform(
                                multipartWithAuth(requestPath)
                                    .file(
                                        createMultipartFile(
                                            TEST_FOOD_SPOTS_REQUEST_NAME,
                                            objectMapper.writeValueAsBytes(reportRequest).inputStream(),
                                        ),
                                    ).file("reportPhotos", reportPhotos[0].bytes)
                                    .file("reportPhotos", reportPhotos[1].bytes),
                            ).andExpect(status().isCreated)
                    }
                }

                reportRequest = createTestReportRequest(name = TEST_FOOD_SPOT_NAME_EMPTY)
                `when`("상호명이 공백인 경우") {
                    then("400을 반환") {
                        mockMvc
                            .perform(
                                multipartWithAuth(requestPath)
                                    .file(
                                        createMultipartFile(
                                            TEST_FOOD_SPOTS_REQUEST_NAME,
                                            objectMapper.writeValueAsBytes(reportRequest).inputStream(),
                                        ),
                                    ),
                            ).andExpect(status().isBadRequest)
                    }
                }

                reportRequest = createTestReportRequest(name = TEST_FOOD_SPOT_NAME_INVALID_STR)
                `when`("상호명에 허용되지 않은 특수문자가 포함된 경우") {
                    then("400을 반환") {
                        mockMvc
                            .perform(
                                multipartWithAuth(requestPath)
                                    .file(
                                        createMultipartFile(
                                            TEST_FOOD_SPOTS_REQUEST_NAME,
                                            objectMapper.writeValueAsBytes(reportRequest).inputStream(),
                                        ),
                                    ),
                            ).andExpect(status().isBadRequest)
                    }
                }

                reportRequest = createTestReportRequest(name = TEST_FOOD_SPOT_NAME_TOO_LONG)
                `when`("상호명이 30자 초과인 경우") {
                    then("400을 반환") {
                        mockMvc
                            .perform(
                                multipartWithAuth(requestPath)
                                    .file(
                                        createMultipartFile(
                                            TEST_FOOD_SPOTS_REQUEST_NAME,
                                            objectMapper.writeValueAsBytes(reportRequest).inputStream(),
                                        ),
                                    ),
                            ).andExpect(status().isBadRequest)
                    }
                }

                reportRequest = createTestReportRequest(longitude = 190.0)
                `when`("경도가 범위를 벗어난 경우") {
                    then("400을 반환") {
                        mockMvc
                            .perform(
                                multipartWithAuth(requestPath)
                                    .file(
                                        createMultipartFile(
                                            TEST_FOOD_SPOTS_REQUEST_NAME,
                                            objectMapper.writeValueAsBytes(reportRequest).inputStream(),
                                        ),
                                    ),
                            ).andExpect(status().isBadRequest)
                    }
                }

                reportRequest = createTestReportRequest(latitude = -190.0)
                `when`("위도가 범위를 벗어난 경우") {
                    then("400을 반환") {
                        mockMvc
                            .perform(
                                multipartWithAuth(requestPath)
                                    .file(
                                        createMultipartFile(
                                            TEST_FOOD_SPOTS_REQUEST_NAME,
                                            objectMapper.writeValueAsBytes(reportRequest).inputStream(),
                                        ),
                                    ),
                            ).andExpect(status().isBadRequest)
                    }
                }

                reportRequest = createTestReportRequest()
                reportPhotos = createMockPhotoList(WEBP) + createMockPhotoList(WEBP)
                `when`("이미지가 3개 초과인 경우") {
                    then("400을 반환") {
                        mockMvc
                            .perform(
                                multipartWithAuth(requestPath)
                                    .file(
                                        createMultipartFile(
                                            TEST_FOOD_SPOTS_REQUEST_NAME,
                                            objectMapper.writeValueAsBytes(reportRequest).inputStream(),
                                        ),
                                    ).file("reportPhotos", reportPhotos[0].bytes)
                                    .file("reportPhotos", reportPhotos[1].bytes)
                                    .file("reportPhotos", reportPhotos[2].bytes)
                                    .file("reportPhotos", reportPhotos[3].bytes),
                            ).andExpect(status().isBadRequest)
                    }
                }

                reportPhotos = createMockPhotoList(ImageFormat.JPEG)
                `when`("이미지 형식이 webp가 아닌 경우") {
                    then("400을 반환") {
                        mockMvc
                            .perform(
                                multipartWithAuth(requestPath)
                                    .file(
                                        createMultipartFile(
                                            TEST_FOOD_SPOTS_REQUEST_NAME,
                                            objectMapper.writeValueAsBytes(reportRequest).inputStream(),
                                        ),
                                    ).file("reportPhotos", reportPhotos[0].bytes)
                                    .file("reportPhotos", reportPhotos[1].bytes),
                            ).andExpect(status().isBadRequest)
                    }
                }

                `when`("파일의 크기가 1MB를 초과하면") {
                    val mockFile: MockMultipartFile = mockk<MockMultipartFile>()
                    every { mockFile.size } returns 1024 * 1024 + 1
                    every { mockFile.name } returns TEST_FOOD_SPOTS_REQUEST_PHOTO
                    every { mockFile.inputStream } returns createTestImageFile(WEBP).inputStream
                    every { foodSpotsService.createReport(any(), any(), any()) } just runs
                    then("400을 반환") {
                        mockMvc
                            .perform(
                                multipartWithAuth(requestPath)
                                    .file(
                                        createMultipartFile(
                                            TEST_FOOD_SPOTS_REQUEST_NAME,
                                            objectMapper.writeValueAsBytes(reportRequest).inputStream(),
                                        ),
                                    ).file(mockFile),
                            ).andExpect(status().isBadRequest)
                        verify(exactly = 0) { foodSpotsService.createReport(any(), any(), any()) }
                    }
                }
            }

            given("GET $requestPath/histories Test") {
                val response =
                    SliceResponse(
                        TEST_FOOD_SPOTS_SIZE,
                        listOf(createTestReportHistoriesResponse()),
                    )
                every {
                    foodSpotsService.getReportHistories(any(), any(), any())
                } returns response
                `when`("정상적인 요청이 들어올 경우") {
                    then("해당 유저의 리포트 이력을 반환한다.") {
                        mockMvc
                            .perform(
                                getWithAuth("$requestPath/histories")
                                    .param("size", TEST_FOOD_SPOTS_SIZE.toString())
                                    .param("lastId", TEST_FOOD_SPOTS_LAST_ID.toString()),
                            ).andExpect(status().isOk)
                    }
                }

                `when`("size와 lastId가 없는 경우") {
                    every {
                        foodSpotsService.getReportHistories(any(), any(), any())
                    } returns response
                    then("기본값으로 해당 유저의 리포트 이력을 반환한다.") {
                        mockMvc
                            .perform(
                                getWithAuth("$requestPath/histories"),
                            ).andExpect(status().isOk)
                    }
                }

                `when`("size가 양수가 아닌 경우") {
                    then("400을 반환") {
                        mockMvc
                            .perform(
                                getWithAuth("$requestPath/histories")
                                    .param("size", "0"),
                            ).andExpect(status().isBadRequest)
                    }
                }

                `when`("lastId가 양수가 아닌 경우") {
                    then("400을 반환") {
                        mockMvc
                            .perform(
                                getWithAuth("$requestPath/histories")
                                    .param("lastId", "-1"),
                            ).andExpect(status().isBadRequest)
                    }
                }
            }
        },
    )
