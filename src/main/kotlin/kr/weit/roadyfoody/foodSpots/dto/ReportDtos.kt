package kr.weit.roadyfoody.foodSpots.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import kr.weit.roadyfoody.foodSpots.domain.DayOfWeek
import kr.weit.roadyfoody.foodSpots.domain.FoodSpots
import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsHistory
import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsPhoto
import kr.weit.roadyfoody.foodSpots.domain.ReportOperationHours
import kr.weit.roadyfoody.foodSpots.utils.FOOD_SPOTS_NAME_REGEX_DESC
import kr.weit.roadyfoody.foodSpots.utils.FOOD_SPOTS_NAME_REGEX_STR
import kr.weit.roadyfoody.foodSpots.validator.Latitude
import kr.weit.roadyfoody.foodSpots.validator.Longitude
import kr.weit.roadyfoody.global.utils.CoordinateUtils.Companion.createCoordinate
import kr.weit.roadyfoody.user.domain.User
import java.time.LocalDateTime

data class ReportRequest(
    @Schema(description = "상호명 : $FOOD_SPOTS_NAME_REGEX_DESC", example = "명륜진사갈비 본사")
    @field:Pattern(regexp = FOOD_SPOTS_NAME_REGEX_STR, message = FOOD_SPOTS_NAME_REGEX_DESC)
    @field:NotBlank(message = "상호명은 필수입니다.")
    val name: String,
    @Schema(description = "경도", example = "127.12312219099")
    @field:NotNull(message = "경도는 필수입니다.")
    @field:Longitude
    val longitude: Double,
    @Schema(description = "위도", example = "37.4940529587731")
    @field:NotNull(message = "위도는 필수입니다.")
    @field:Latitude
    val latitude: Double,
    @NotNull(message = "음식점 여부는 필수입니다.")
    @Schema(description = "푸드트럭여부(이동여부)", example = "false")
    val foodTruck: Boolean,
    @Schema(description = "영업 여부", example = "true")
    @NotNull(message = "영업 여부는 필수입니다.")
    val open: Boolean,
    @Schema(description = "폐업 여부", example = "false")
    @NotNull(message = "폐업 여부는 필수입니다.")
    val closed: Boolean,
    @Schema(description = "음식 카테고리", example = "한식")
    @NotEmpty(message = "음식 카테고리는 필수입니다.")
    val foodCategories: Set<Long>,
    @Schema(description = "운영시간 리스트")
    val operationHours: List<OperationHoursRequest>,
) {
    fun toFoodSpotsEntity() =
        FoodSpots(
            name = name,
            point = createCoordinate(longitude, latitude),
            foodTruck = foodTruck,
            open = open,
            storeClosure = closed,
        )

    fun toFoodSpotsHistoryEntity(
        foodSpots: FoodSpots,
        user: User,
    ) = FoodSpotsHistory(
        name = name,
        foodSpots = foodSpots,
        user = user,
        point = createCoordinate(longitude, latitude),
        foodTruck = foodTruck,
        open = open,
        storeClosure = closed,
    )

    fun toOperationHoursEntity(foodSpotsHistory: FoodSpotsHistory) =
        operationHours.map {
            ReportOperationHours(
                foodSpotsHistory = foodSpotsHistory,
                dayOfWeek = it.dayOfWeek,
                openingHours = it.openingHours,
                closingHours = it.closingHours,
            )
        }
}

data class OperationHoursRequest(
    @Schema(description = "요일", example = "MON")
    val dayOfWeek: DayOfWeek,
    @Schema(description = "오픈 시간", example = "09:00")
    @field:Pattern(regexp = "^([01]\\d|2[0-4]):([0-5]\\d)$", message = "오픈 시간은 01:00부터 24:00까지의 형식이어야 합니다.")
    val openingHours: String,
    @Schema(description = "마감 시간", example = "24:00")
    @field:Pattern(regexp = "^([01]\\d|2[0-4]):([0-5]\\d)$", message = "마감 시간은 01:00부터 24:00까지의 형식이어야 합니다.")
    val closingHours: String,
)

data class ReportHistoriesResponse(
    @Schema(description = "리포트 이력 ID", example = "1")
    val id: Long,
    @Schema(description = "유저 ID", example = "1")
    val userId: Long,
    @Schema(description = "음식점 ID", example = "1")
    val foodSpotsId: Long,
    @Schema(description = "상호명", example = "명륜진사갈비 본사")
    val name: String,
    @Schema(description = "경도", example = "127.12312219099")
    val longitude: Double,
    @Schema(description = "위도", example = "37.4940529587731")
    val latitude: Double,
    @Schema(description = "생성일시", example = "2021-08-01T00:00:00")
    val createdDateTime: LocalDateTime,
    @Schema(description = "리포트 사진 리스트")
    val reportPhotos: List<ReportPhotoResponse>,
) {
    constructor(foodSpotsHistory: FoodSpotsHistory, reportPhotoResponse: List<ReportPhotoResponse>) : this(
        id = foodSpotsHistory.id,
        userId = foodSpotsHistory.user.id,
        foodSpotsId = foodSpotsHistory.foodSpots.id,
        name = foodSpotsHistory.name,
        longitude = foodSpotsHistory.point.x,
        latitude = foodSpotsHistory.point.y,
        createdDateTime = foodSpotsHistory.createdDateTime,
        reportPhotos = reportPhotoResponse,
    )
}

data class ReportPhotoResponse(
    @Schema(description = "리포트 사진 ID", example = "1")
    val id: Long,
    @Schema(description = "리포트 사진 URL")
    val url: String,
) {
    constructor(reportPhoto: FoodSpotsPhoto, url: String) : this(
        id = reportPhoto.id,
        url = url,
    )
}
