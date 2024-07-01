package kr.weit.roadyfoody.foodSpots.repository

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.weit.roadyfoody.foodSpots.domain.FoodSpots
import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsHistory
import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsPhoto
import kr.weit.roadyfoody.foodSpots.fixture.createTestFoodHistory
import kr.weit.roadyfoody.foodSpots.fixture.createTestFoodSpots
import kr.weit.roadyfoody.foodSpots.fixture.createTestFoodSpotsPhoto
import kr.weit.roadyfoody.support.annotation.RepositoryTest
import kr.weit.roadyfoody.user.domain.User
import kr.weit.roadyfoody.user.fixture.createTestUser
import kr.weit.roadyfoody.user.repository.UserRepository

@RepositoryTest
class FoodSpotsPhotoRepositoryTest(
    private val foodSpotsPhotoRepository: FoodSpotsPhotoRepository,
    private val foodSpotsHistoryRepository: FoodSpotsHistoryRepository,
    private val foodSPotsRepository: FoodSpotsRepository,
    private val userRepository: UserRepository,
) : DescribeSpec({
        lateinit var givenReportPhotos: List<FoodSpotsPhoto>
        lateinit var user: User
        lateinit var foodSpots: FoodSpots
        lateinit var foodSpotsHistory: FoodSpotsHistory
        lateinit var otherFoodSpotsHistory: FoodSpotsHistory
        beforeEach {
            user = userRepository.save(createTestUser(0L))
            foodSpots = foodSPotsRepository.save(createTestFoodSpots())
            foodSpotsHistory = foodSpotsHistoryRepository.save(createTestFoodHistory(foodSpots = foodSpots, user = user))
            otherFoodSpotsHistory = foodSpotsHistoryRepository.save(createTestFoodHistory(foodSpots = foodSpots, user = user))
            givenReportPhotos =
                foodSpotsPhotoRepository.saveAll(
                    listOf(createTestFoodSpotsPhoto(foodSpotsHistory), createTestFoodSpotsPhoto(otherFoodSpotsHistory)),
                )
        }

        describe("getByHistoryId 메소드는") {
            context("존재하는 historyId 를 받는 경우") {
                it("일치하는 FoodSpotsPhoto 리스트를 반환한다.") {
                    val result = foodSpotsPhotoRepository.getByHistoryId(foodSpotsHistory.id)
                    result.map { it.id } shouldBe givenReportPhotos.filter { it.history.id == foodSpotsHistory.id }.map { it.id }
                }
            }

            context("존재하지 않는 historyId 를 받는 경우") {
                it("빈 리스트를 반환한다.") {
                    val result = foodSpotsPhotoRepository.getByHistoryId(0L)
                    result.size shouldBe 0
                }
            }
        }
    })
