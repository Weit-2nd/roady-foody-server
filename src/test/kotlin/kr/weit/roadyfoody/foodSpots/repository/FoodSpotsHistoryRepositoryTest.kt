package kr.weit.roadyfoody.foodSpots.repository

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.weit.roadyfoody.foodSpots.domain.FoodSpots
import kr.weit.roadyfoody.foodSpots.fixture.TEST_FOOD_SPOTS_SIZE
import kr.weit.roadyfoody.foodSpots.fixture.createTestFoodHistory
import kr.weit.roadyfoody.foodSpots.fixture.createTestFoodSpots
import kr.weit.roadyfoody.support.annotation.RepositoryTest
import kr.weit.roadyfoody.user.domain.User
import kr.weit.roadyfoody.user.fixture.createTestUser
import kr.weit.roadyfoody.user.repository.UserRepository

@RepositoryTest
class FoodSpotsHistoryRepositoryTest(
    private val foodSpotsHistoryRepository: FoodSpotsHistoryRepository,
    private val userRepository: UserRepository,
    private val foodSpotsRepository: FoodSpotsRepository,
) : DescribeSpec({
        lateinit var user: User
        lateinit var otherUser: User
        lateinit var foodSpots: FoodSpots
        lateinit var otherFoodSpots: FoodSpots
        beforeEach {
            user = userRepository.save(createTestUser(0L))
            otherUser = userRepository.save(createTestUser(0L, nickname = "otherUser"))
            foodSpots = foodSpotsRepository.save(createTestFoodSpots())
            otherFoodSpots = foodSpotsRepository.save(createTestFoodSpots())
            foodSpotsHistoryRepository.saveAll(
                listOf(
                    createTestFoodHistory(user = user, foodSpots = foodSpots),
                    createTestFoodHistory(user = user, foodSpots = otherFoodSpots),
                    createTestFoodHistory(user = otherUser, foodSpots = foodSpots),
                ),
            )
        }

        describe("getHistoriesByUser 메소드는") {
            context("존재하는 user 와 size, lastId 를 받는 경우") {
                it("해당 user 의 size 만큼의 FoodSpotsHistory 리스트를 반환한다.") {
                    val histories = foodSpotsHistoryRepository.getHistoriesByUser(user, TEST_FOOD_SPOTS_SIZE, null)
                    histories.map { it.foodSpots.id } shouldBe listOf(otherFoodSpots.id, foodSpots.id)
                    histories.size shouldBe 2
                }
            }
        }
    })
