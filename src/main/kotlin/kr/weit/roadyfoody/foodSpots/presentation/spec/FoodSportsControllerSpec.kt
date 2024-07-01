package kr.weit.roadyfoody.foodSpots.presentation.spec

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import kr.weit.roadyfoody.common.dto.SliceResponse
import kr.weit.roadyfoody.common.exception.ErrorResponse
import kr.weit.roadyfoody.foodSpots.dto.ReportHistoriesResponse
import kr.weit.roadyfoody.foodSpots.dto.ReportRequest
import kr.weit.roadyfoody.foodSpots.utils.SliceReportHistories
import kr.weit.roadyfoody.foodSpots.validator.WebPImageList
import kr.weit.roadyfoody.global.swagger.v1.SwaggerTag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Tag(name = SwaggerTag.FOOD_SPOTS)
interface FoodSportsControllerSpec {
    @Operation(
        description = "음식점 정보 리포트 API",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "리포트 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "리포트 실패",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Empty FoodSpot Name",
                                summary = "음식점 이름 미입력",
                                value = """
                        {
                            "code": -10000,
                            "errorMessage": "상호명은 1자 이상 20자 이하 한글, 영문, 숫자, 특수문자 여야 합니다."
                        }
                        """,
                            ),
                            ExampleObject(
                                name = "Invalid FoodSpot Name",
                                summary = "상호명에 허용되지 않은 특수문자가 포함된 경우",
                                value = """
                                {
                                    "code": -10000,
                                    "errorMessage": "상호명은 1자 이상 20자 이하 한글, 영문, 숫자, 특수문자 여야 합니다."
                                }
                                """,
                            ),
                            ExampleObject(
                                name = "FoodSpot Name Length Too Long",
                                summary = "상호명이 30자 초과인 경우",
                                value = """
                            {
                                "code": -10000,
                                "errorMessage": "상호명은 1자 이상 20자 이하 한글, 영문, 숫자, 특수문자 여야 합니다."
                            }
                            """,
                            ),
                            ExampleObject(
                                name = "latitude too high",
                                summary = "경도가 범위보다 높은 경우",
                                value = """
                            {
                                "code": -10000,
                                "errorMessage": "경도는 180 이하여야 합니다."
                            }
                            """,
                            ),
                            ExampleObject(
                                name = "latitude too low",
                                summary = "경도가 범위보다 낮은 경우",
                                value = """
                            {
                                "code": -10000,
                                "errorMessage": "경도는 -180 이상이어야 합니다."
                            }
                            """,
                            ),
                            ExampleObject(
                                name = "longitude too high",
                                summary = "위도가 범위보다 높은 경우",
                                value = """
                            {
                                "code": -10000,
                                "errorMessage": "위도는 180 이하여야 합니다."
                            }
                            """,
                            ),
                            ExampleObject(
                                name = "longitude too low",
                                summary = "위도가 범위보다 낮은 경우",
                                value = """
                            {
                                "code": -10000,
                                "errorMessage": "위도는 -180 이상여야 합니다."
                            }
                            """,
                            ),
                            ExampleObject(
                                name = "Too Many Images",
                                summary = "이미지가 3개 초과인 경우",
                                value = """
                        {
                            "code": -10000,
                            "errorMessage": "이미지는 최대 3개까지 업로드할 수 있습니다."
                        }
                        """,
                            ),
                            ExampleObject(
                                name = "Invalid Image type",
                                summary = "WEBP 이외의 이미지 입력",
                                value = """
                        {
                            "code": -10000,
                            "errorMessage": "하나 이상의 파일이 잘못 되었습니다."
                        }
                        """,
                            ),
                            ExampleObject(
                                name = "Too large image",
                                summary = "이미지 용량이 1MB 초과인 경우",
                                value = """
                        {
                            "code": -10000,
                            "errorMessage": "하나 이상의 파일이 잘못 되었습니다."
                        }
                        """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun createReport(
        @RequestHeader
        userId: Long,
        @Valid
        @RequestPart
        reportRequest: ReportRequest,
        @Size(max = 3, message = "이미지는 최대 3개까지 업로드할 수 있습니다.")
        @WebPImageList
        @RequestPart(required = false)
        reportPhotos: List<MultipartFile>?,
    )

    @Operation(
        description = "음식점 정보 리스트 조회 API",
        parameters = [
            Parameter(name = "size", description = "조회할 개수", required = false, example = "10"),
            Parameter(name = "lastId", description = "마지막으로 조회된 ID", required = false, example = "1"),
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "리포트 리스트 조회 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema =
                            Schema(
                                implementation = SliceReportHistories::class,
                            ),
                    ),
                ],
            ),

            ApiResponse(
                responseCode = "400",
                description = "리포트 리스트 조회 실패",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Invalid Size",
                                summary = "양수가 아닌 사이즈 입력",
                                value = """
                        {
                            "code": -10000,
                            "errorMessage": "조회할 개수는 양수여야 합니다."
                        }
                        """,
                            ),
                            ExampleObject(
                                name = "Invalid Last ID",
                                summary = "양수가 아닌 마지막 ID 입력",
                                value = """
                        {
                            "code": -10000,
                            "errorMessage": "마지막 ID는 양수여야 합니다."
                        }
                        """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun getReportHistories(
        @RequestHeader
        userId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam(defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam(required = false)
        lastId: Long?,
    ): SliceResponse<ReportHistoriesResponse>
}
