package kr.weit.roadyfoody.foodSpots.fixture

import kr.weit.roadyfoody.foodSpots.domain.FoodSpots
import kr.weit.roadyfoody.foodSpots.domain.FoodSpots.Companion.SRID_WGS84
import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsHistory
import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsPhoto
import kr.weit.roadyfoody.foodSpots.dto.ReportHistoriesResponse
import kr.weit.roadyfoody.foodSpots.dto.ReportPhotoResponse
import kr.weit.roadyfoody.foodSpots.dto.ReportRequest
import kr.weit.roadyfoody.foodSpots.utils.FOOD_SPOTS_NAME_MAX_LENGTH
import kr.weit.roadyfoody.support.utils.ImageFormat
import kr.weit.roadyfoody.support.utils.createTestImageFile
import kr.weit.roadyfoody.user.domain.User
import kr.weit.roadyfoody.user.fixture.createTestUser
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

const val TEST_FOOD_SPOT_NAME = "testFoodSpot"
const val TEST_FOOD_SPOT_NAME_EMPTY = ""
val TEST_FOOD_SPOT_NAME_TOO_LONG = "a".repeat(FOOD_SPOTS_NAME_MAX_LENGTH + 1)
const val TEST_FOOD_SPOT_NAME_INVALID_STR = "testFoodSpot|"
const val TEST_FOOD_SPOT_LATITUDE = 37.123456
const val TEST_FOOD_SPOT_LONGITUDE = 127.123456
val TEST_FOOD_SPOT_POINT: Point =
    GeometryFactory().createPoint(Coordinate(TEST_FOOD_SPOT_LONGITUDE, TEST_FOOD_SPOT_LATITUDE)).also { it.srid = SRID_WGS84 }
const val TEST_FOOD_SPOT_FOOD_TRUCK = true
const val TEST_FOOD_SPOT_OPEN = true
const val TEST_FOOD_SPOT_STORE_CLOSURE = false
const val TEST_PHOTO_NAME = "test_photo_name"
const val TEST_FOOD_SPOTS_REQUEST_NAME = "reportRequest"
const val TEST_FOOD_SPOTS_REQUEST_PHOTO = "reportPhotos"
const val TEST_FOOD_SPOTS_SIZE = 10
const val TEST_FOOD_SPOTS_LAST_ID = 1L
const val TEST_FOOD_SPOTS_PHOTO_URL = "test_url"

fun createMockTestFoodSpot(id: Long = 0L) = MockTestFoodSpot(id)

fun createMockTestFoodHistory(
    user: User = createTestUser(0L),
    foodSpots: FoodSpots = createMockTestFoodSpot(),
) = MockTestFoodSpotsHistory(user = user, foodSpots = foodSpots)

fun createTestFoodSpots(
    id: Long = 0L,
    name: String = TEST_FOOD_SPOT_NAME,
    foodTruck: Boolean = TEST_FOOD_SPOT_FOOD_TRUCK,
    open: Boolean = TEST_FOOD_SPOT_OPEN,
    storeClosure: Boolean = TEST_FOOD_SPOT_STORE_CLOSURE,
    point: Point = TEST_FOOD_SPOT_POINT,
) = FoodSpots(id, name, foodTruck, open, storeClosure, point)

fun createTestFoodHistory(
    id: Long = 0L,
    foodSpots: FoodSpots = createTestFoodSpots(0L),
    user: User = createTestUser(0L),
    name: String = TEST_FOOD_SPOT_NAME,
    foodTruck: Boolean = TEST_FOOD_SPOT_FOOD_TRUCK,
    open: Boolean = TEST_FOOD_SPOT_OPEN,
    storeClosure: Boolean = TEST_FOOD_SPOT_STORE_CLOSURE,
    point: Point = TEST_FOOD_SPOT_POINT,
) = FoodSpotsHistory(id, foodSpots, user, name, foodTruck, open, storeClosure, point)

fun createTestFoodSpotsPhoto(foodSpotsHistory: FoodSpotsHistory = createTestFoodHistory()) =
    FoodSpotsPhoto(0L, foodSpotsHistory, TEST_PHOTO_NAME)

fun createTestReportRequest(
    name: String = TEST_FOOD_SPOT_NAME,
    longitude: Double = TEST_FOOD_SPOT_LONGITUDE,
    latitude: Double = TEST_FOOD_SPOT_LATITUDE,
    foodTruck: Boolean = TEST_FOOD_SPOT_FOOD_TRUCK,
    open: Boolean = TEST_FOOD_SPOT_OPEN,
    storeClosure: Boolean = TEST_FOOD_SPOT_STORE_CLOSURE,
) = ReportRequest(
    name,
    longitude,
    latitude,
    foodTruck,
    open,
    storeClosure,
)

fun createMockPhotoList(
    format: ImageFormat,
    name: String = TEST_PHOTO_NAME,
): List<MultipartFile> =
    listOf(
        createTestImageFile(format),
        createTestImageFile(format, name),
    )

fun createTestReportPhotoResponse(
    id: Long = 0L,
    name: String = TEST_PHOTO_NAME,
    url: String = TEST_FOOD_SPOTS_PHOTO_URL,
) = ReportPhotoResponse(id, name, url)

fun createTestReportHistoriesResponse(
    foodSpotsHistory: FoodSpotsHistory = createMockTestFoodHistory(),
    reportPhotoResponse: List<ReportPhotoResponse> = listOf(createTestReportPhotoResponse()),
) = ReportHistoriesResponse(
    foodSpotsHistory.id,
    foodSpotsHistory.user.id,
    foodSpotsHistory.foodSpots.id,
    foodSpotsHistory.name,
    foodSpotsHistory.point.x,
    foodSpotsHistory.point.y,
    foodSpotsHistory.createdDateTime,
    reportPhotoResponse,
)

class MockTestFoodSpot(
    id: Long = 0L,
    name: String = TEST_FOOD_SPOT_NAME,
    foodTruck: Boolean = TEST_FOOD_SPOT_FOOD_TRUCK,
    open: Boolean = TEST_FOOD_SPOT_OPEN,
    closed: Boolean = TEST_FOOD_SPOT_STORE_CLOSURE,
    point: Point = TEST_FOOD_SPOT_POINT,
) : FoodSpots(id, name, foodTruck, open, closed, point) {
    override var createdDateTime: LocalDateTime = LocalDateTime.now()
    override var updatedDateTime: LocalDateTime = LocalDateTime.now()
}

class MockTestFoodSpotsHistory(
    id: Long = 0L,
    foodSpots: FoodSpots = MockTestFoodSpot(),
    user: User = createTestUser(),
    name: String = TEST_FOOD_SPOT_NAME,
    foodTruck: Boolean = TEST_FOOD_SPOT_FOOD_TRUCK,
    open: Boolean = TEST_FOOD_SPOT_OPEN,
    closed: Boolean = TEST_FOOD_SPOT_STORE_CLOSURE,
    point: Point = TEST_FOOD_SPOT_POINT,
) : FoodSpotsHistory(id, foodSpots, user, name, foodTruck, open, closed, point) {
    override var createdDateTime: LocalDateTime = LocalDateTime.now()
}
