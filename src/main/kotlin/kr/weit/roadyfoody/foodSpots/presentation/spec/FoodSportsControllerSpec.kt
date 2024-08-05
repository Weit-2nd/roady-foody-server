package kr.weit.roadyfoody.foodSpots.presentation.spec

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import kr.weit.roadyfoody.auth.security.LoginUser
import kr.weit.roadyfoody.common.exception.ErrorCode
import kr.weit.roadyfoody.foodSpots.application.dto.FoodSpotsUpdateRequest
import kr.weit.roadyfoody.foodSpots.application.dto.ReportRequest
import kr.weit.roadyfoody.foodSpots.validator.WebPImageList
import kr.weit.roadyfoody.global.swagger.ApiErrorCodeExamples
import kr.weit.roadyfoody.global.swagger.v1.SwaggerTag
import kr.weit.roadyfoody.user.domain.User
import org.springframework.web.bind.annotation.PathVariable
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
        ],
    )
    @ApiErrorCodeExamples(
        [
            ErrorCode.INVALID_LENGTH_FOOD_SPOTS_NAME,
            ErrorCode.INVALID_CHARACTERS_FOOD_SPOTS_NAME,
            ErrorCode.LATITUDE_TOO_HIGH,
            ErrorCode.LATITUDE_TOO_LOW,
            ErrorCode.LONGITUDE_TOO_HIGH,
            ErrorCode.LONGITUDE_TOO_LOW,
            ErrorCode.NO_CATEGORY_SELECTED,
            ErrorCode.INVALID_FORMAT_OPERATION_HOURS,
            ErrorCode.IMAGES_TOO_MANY,
            ErrorCode.INVALID_IMAGE_TYPE,
            ErrorCode.IMAGES_SIZE_TOO_LARGE,
            ErrorCode.NOT_FOUND_FOOD_CATEGORY,
            ErrorCode.TOO_MANY_REPORT_REQUESTS,
        ],
    )
    fun createReport(
        @LoginUser
        user: User,
        @Valid
        @RequestPart
        reportRequest: ReportRequest,
        @Size(max = 3, message = "이미지는 최대 3개까지 업로드할 수 있습니다.")
        @WebPImageList
        @RequestPart(required = false)
        reportPhotos: List<MultipartFile>?,
    )

    @Operation(
        description = "음식점 정보 수정 API",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "음식점 정보 수정 성공",
            ),
        ],
    )
    @ApiErrorCodeExamples(
        [
            ErrorCode.INVALID_LENGTH_FOOD_SPOTS_NAME,
            ErrorCode.INVALID_CHARACTERS_FOOD_SPOTS_NAME,
            ErrorCode.LATITUDE_TOO_HIGH,
            ErrorCode.LATITUDE_TOO_LOW,
            ErrorCode.LONGITUDE_TOO_HIGH,
            ErrorCode.LONGITUDE_TOO_LOW,
            ErrorCode.NO_CATEGORY_SELECTED,
            ErrorCode.INVALID_FORMAT_OPERATION_HOURS,
            ErrorCode.NOT_FOUND_FOOD_CATEGORY,
            ErrorCode.INVALID_CHANGE_VALUE,
            ErrorCode.NON_POSITIVE_FOOD_SPOT_ID,
            ErrorCode.TOO_MANY_REPORT_REQUESTS,
        ],
    )
    fun updateFoodSpots(
        user: User,
        @Positive(message = "음식점 ID는 양수여야 합니다.")
        @Parameter(description = "음식점 ID", required = true, example = "1")
        foodSpotsId: Long,
        @Valid
        request: FoodSpotsUpdateRequest,
    )

    @Operation(
        description = "음식점 정보 리포트 삭제 API",
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "리포트 삭제 성공",
            ),
        ],
    )
    @ApiErrorCodeExamples(
        [
            ErrorCode.NOT_FOUND_FOOD_SPOTS_HISTORIES,
            ErrorCode.NON_POSITIVE_FOOD_SPOTS_HISTORIES_ID,
            ErrorCode.NOT_FOOD_SPOTS_HISTORIES_OWNER,
        ],
    )
    fun deleteFoodSpotsHistories(
        @LoginUser
        user: User,
        @Positive(message = "음식점 리포트 ID는 양수여야 합니다.")
        @PathVariable("historyId")
        historyId: Long,
    )
}
