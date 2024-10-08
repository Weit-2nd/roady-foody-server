package kr.weit.roadyfoody.foodSpots.presentation.api

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import kr.weit.roadyfoody.auth.security.LoginUser
import kr.weit.roadyfoody.badge.domain.Badge
import kr.weit.roadyfoody.common.dto.SliceResponse
import kr.weit.roadyfoody.foodSpots.application.dto.FoodSpotsDetailResponse
import kr.weit.roadyfoody.foodSpots.application.dto.FoodSpotsReviewResponse
import kr.weit.roadyfoody.foodSpots.application.dto.FoodSpotsUpdateRequest
import kr.weit.roadyfoody.foodSpots.application.dto.ReportHistoryDetailResponse
import kr.weit.roadyfoody.foodSpots.application.dto.ReportRequest
import kr.weit.roadyfoody.foodSpots.application.service.FoodSpotsCommandService
import kr.weit.roadyfoody.foodSpots.application.service.FoodSpotsQueryService
import kr.weit.roadyfoody.foodSpots.presentation.spec.FoodSpotsControllerSpec
import kr.weit.roadyfoody.foodSpots.validator.WebPImageList
import kr.weit.roadyfoody.review.repository.ReviewSortType
import kr.weit.roadyfoody.user.domain.User
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/food-spots")
class FoodSpotsController(
    private val foodSpotsCommandService: FoodSpotsCommandService,
    private val foodSpotsQueryService: FoodSpotsQueryService,
) : FoodSpotsControllerSpec {
    @ResponseStatus(CREATED)
    @PostMapping(consumes = [MULTIPART_FORM_DATA_VALUE])
    override fun createReport(
        @LoginUser
        user: User,
        @Valid
        @RequestPart
        reportRequest: ReportRequest,
        @Size(max = 3, message = "이미지는 최대 3개까지 업로드할 수 있습니다.")
        @WebPImageList
        @RequestPart(required = false)
        reportPhotos: List<MultipartFile>?,
    ) = foodSpotsCommandService.createReport(user, reportRequest, reportPhotos)

    @ResponseStatus(CREATED)
    @PatchMapping("/{foodSpotsId}", consumes = [MULTIPART_FORM_DATA_VALUE])
    override fun updateFoodSpots(
        @LoginUser
        user: User,
        @Positive(message = "음식점 ID는 양수여야 합니다.")
        @PathVariable("foodSpotsId")
        foodSpotsId: Long,
        @Valid
        @RequestPart(required = false)
        request: FoodSpotsUpdateRequest?,
        @Size(max = 3, message = "이미지는 최대 3개까지 업로드할 수 있습니다.")
        @WebPImageList
        @RequestPart(required = false)
        reportPhotos: List<MultipartFile>?,
    ) {
        foodSpotsCommandService.doUpdateReport(user, foodSpotsId, request, reportPhotos)
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/histories/{historyId}")
    override fun deleteFoodSpotsHistories(
        @LoginUser
        user: User,
        @Positive(message = "음식점 리포트 ID는 양수여야 합니다.")
        @PathVariable("historyId")
        historyId: Long,
    ) = foodSpotsCommandService.deleteFoodSpotsHistories(user, historyId)

    @GetMapping("/histories/{historyId}")
    override fun getReportHistory(
        @PathVariable("historyId")
        historyId: Long,
    ): ReportHistoryDetailResponse = foodSpotsQueryService.getReportHistory(historyId)

    @GetMapping("/{foodSpotsId}/reviews")
    override fun getFoodSpotsReviews(
        @PathVariable("foodSpotsId")
        @Positive(message = "음식점 ID는 양수여야 합니다.")
        foodSpotsId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam(defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam(required = false)
        lastId: Long?,
        @RequestParam(required = false, defaultValue = "LATEST")
        sortType: ReviewSortType,
        @RequestParam(required = false)
        badge: String?,
    ): SliceResponse<FoodSpotsReviewResponse> =
        foodSpotsQueryService.getFoodSpotsReview(foodSpotsId, size, lastId, sortType, badge?.let { Badge.fromDescriptionOrNull(it) })

    @GetMapping("/{foodSpotsId}")
    override fun getFoodSpotsDetail(
        @PathVariable("foodSpotsId")
        @Positive(message = "음식점 ID는 양수여야 합니다.")
        foodSpotsId: Long,
    ): FoodSpotsDetailResponse = foodSpotsQueryService.getFoodSpotsDetail(foodSpotsId)
}
