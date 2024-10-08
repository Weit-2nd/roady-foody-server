package kr.weit.roadyfoody.rewards.repository

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kr.weit.roadyfoody.foodSpots.domain.FoodSpots
import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsHistory
import kr.weit.roadyfoody.foodSpots.fixture.createTestFoodHistory
import kr.weit.roadyfoody.foodSpots.fixture.createTestFoodSpots
import kr.weit.roadyfoody.foodSpots.repository.FoodSpotsHistoryRepository
import kr.weit.roadyfoody.foodSpots.repository.FoodSpotsRepository
import kr.weit.roadyfoody.rewards.domain.RewardType
import kr.weit.roadyfoody.rewards.domain.Rewards
import kr.weit.roadyfoody.rewards.fixture.createTestRewards
import kr.weit.roadyfoody.support.annotation.RepositoryTest
import kr.weit.roadyfoody.user.domain.User
import kr.weit.roadyfoody.user.fixture.createTestUser
import kr.weit.roadyfoody.user.repository.UserRepository
import org.springframework.data.domain.PageRequest

@RepositoryTest
class RewardsRepositoryTest(
    private val rewardsRepository: RewardsRepository,
    private val userRepository: UserRepository,
    private val foodSpotRepository: FoodSpotsRepository,
    private val foodSpotsHistoryRepository: FoodSpotsHistoryRepository,
) : DescribeSpec(
        {
            lateinit var user: User
            lateinit var rewardsList: List<Rewards>
            lateinit var foodSpots: FoodSpots
            lateinit var foodSpotsHistory: FoodSpotsHistory

            beforeEach {
                user = userRepository.save(createTestUser())
                foodSpotsHistory =
                    createTestFoodHistory(
                        foodSpots = createTestFoodSpots(),
                        user = user,
                    )
                foodSpots = foodSpotRepository.save(foodSpotsHistory.foodSpots)
                foodSpotsHistory = foodSpotsHistoryRepository.save(foodSpotsHistory)
                rewardsList =
                    rewardsRepository.saveAll(
                        listOf(
                            createTestRewards(user, foodSpotsHistory, 100, true, RewardType.REPORT_UPDATE),
                        ),
                    )
            }

            afterEach {
                userRepository.delete(user)
            }

            describe("findAllByUser 메소드는") {
                context("size, page를 받는 경우") {
                    it("해당 유저의 리워드 리스트를 반환한다") {
                        val result =
                            rewardsRepository.findAllByUser(
                                user,
                                PageRequest.of(0, 10),
                            )
                        result.content.size shouldBe rewardsList.size
                    }
                }
            }

            describe("deleteAllByUser 메소드는") {
                context("user의 정보를 받으면") {
                    it("해당 유저의 리워드 내역을 다 삭제한다") {
                        rewardsRepository.deleteAllByUser(
                            user,
                        )
                        val result =
                            rewardsRepository.findAllByUser(
                                user,
                                PageRequest.of(0, 10),
                            )

                        result.shouldBeEmpty()
                    }
                }
            }
        },
    )
