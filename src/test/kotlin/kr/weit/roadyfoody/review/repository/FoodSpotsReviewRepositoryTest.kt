package kr.weit.roadyfoody.review.repository

import createTestFoodSpotsReview
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.weit.roadyfoody.badge.domain.Badge
import kr.weit.roadyfoody.foodSpots.application.dto.ReviewAggregatedInfoResponse
import kr.weit.roadyfoody.foodSpots.domain.FoodSpots
import kr.weit.roadyfoody.foodSpots.fixture.createTestFoodSpots
import kr.weit.roadyfoody.foodSpots.repository.FoodSpotsRepository
import kr.weit.roadyfoody.global.TEST_PAGE_SIZE
import kr.weit.roadyfoody.review.domain.FoodSpotsReview
import kr.weit.roadyfoody.review.domain.ReviewLike
import kr.weit.roadyfoody.review.exception.FoodSpotsReviewNotFoundException
import kr.weit.roadyfoody.support.annotation.RepositoryTest
import kr.weit.roadyfoody.user.domain.User
import kr.weit.roadyfoody.user.fixture.createTestUser
import kr.weit.roadyfoody.user.repository.UserRepository

@RepositoryTest
class FoodSpotsReviewRepositoryTest(
    private val userRepository: UserRepository,
    private val foodSpotsRepository: FoodSpotsRepository,
    private val reviewRepository: FoodSpotsReviewRepository,
    private val reviewLikeRepository: ReviewLikeRepository,
) : DescribeSpec(
        {
            lateinit var user: User
            lateinit var otherUser: User
            lateinit var testUser: User
            lateinit var noReviewsUser: User
            lateinit var foodSpots: FoodSpots
            lateinit var otherFoodSpots: FoodSpots
            lateinit var testFoodSpots: FoodSpots
            lateinit var noReviewsFoodSpots: FoodSpots
            lateinit var reviewList: List<FoodSpotsReview>
            beforeEach {
                user = userRepository.save(createTestUser(0L))
                otherUser = userRepository.save(createTestUser(0L, "otherUser"))
                testUser = userRepository.save(createTestUser(0L, "testUser"))
                noReviewsUser = userRepository.save(createTestUser(0L, "noReviewsUser"))
                foodSpots = foodSpotsRepository.save(createTestFoodSpots())
                otherFoodSpots = foodSpotsRepository.save(createTestFoodSpots())
                testFoodSpots = foodSpotsRepository.save(createTestFoodSpots())
                noReviewsFoodSpots = foodSpotsRepository.save(createTestFoodSpots())

                reviewList =
                    reviewRepository.saveAll(
                        listOf(
                            createTestFoodSpotsReview(user, foodSpots, 5),
                            createTestFoodSpotsReview(user, otherFoodSpots, 5),
                            createTestFoodSpotsReview(otherUser, foodSpots, 2),
                            createTestFoodSpotsReview(user, otherFoodSpots, 5),
                            createTestFoodSpotsReview(testUser, testFoodSpots, 2),
                        ),
                    )

                reviewLikeRepository.saveAll(
                    listOf(
                        ReviewLike(reviewRepository.save(createTestFoodSpotsReview(otherUser, testFoodSpots, 4)), user),
                        ReviewLike(reviewRepository.save(createTestFoodSpotsReview(testUser, testFoodSpots, 4)), otherUser),
                    ),
                )
            }

            describe("findByUser 메소드는") {
                context("리뷰를 작성한 사용자를 받는 경우") {
                    it("해당 사용자의 리뷰 리스트를 반환한다.") {
                        reviewRepository.findByUser(user).size shouldBe 3
                    }
                }
            }

            describe("findReviewById 메소드는") {
                context("리뷰 ID를 받는 경우") {
                    it("해당 ID의 리뷰를 반환한다.") {
                        reviewRepository.findReviewById(reviewList.first().id) shouldBe reviewList.first()
                    }
                }
            }

            describe("deleteAll 메소드는") {
                context("삭제할 리스트를 받는 경우") {
                    it("해당 리스트 모두 삭제한다.") {
                        val reviews = reviewRepository.findByUser(user)
                        reviewRepository.deleteAll(reviews)
                        reviewRepository.findAll().size shouldBe 4
                    }
                }
            }

            describe("getReviewByReviewId 메소드는") {
                context("리뷰 ID를 받는 경우") {
                    it("해당 ID의 리뷰를 반환한다.") {
                        reviewRepository.getReviewByReviewId(reviewList.first().id) shouldBe reviewList.first()
                    }
                }

                context("존재하지 않는 리뷰 ID 를 받는 경우") {
                    it("에러가 발생한다") {
                        shouldThrow<FoodSpotsReviewNotFoundException> {
                            reviewRepository.getReviewByReviewId(0L)
                        }
                    }
                }
            }

            describe("getByIdWithPessimisticLock 메소드는") {
                context("리뷰 ID를 받는 경우") {
                    it("해당 ID의 리뷰를 반환한다.") {
                        reviewRepository.getByIdWithPessimisticLock(reviewList.first().id) shouldBe reviewList.first()
                    }
                }

                context("존재하지 않는 리뷰 ID 를 받는 경우") {
                    it("에러가 발생한다") {
                        shouldThrow<FoodSpotsReviewNotFoundException> {
                            reviewRepository.getByIdWithPessimisticLock(0L)
                        }
                    }
                }
            }

            describe("sliceByUser 메소드는") {
                context("유저 ID, 사이즈, 마지막 ID을 받는 경우") {
                    it("해당 유저의 리뷰 리스트를 반환한다.") {
                        reviewRepository
                            .sliceByUser(
                                user,
                                TEST_PAGE_SIZE,
                                reviewList[1].id,
                            ).content.size shouldBe 1
                    }
                }

                context("유저 ID, 사이즈만 받는 경우") {
                    it("해당 유저의 리뷰 리스트를 반환한다.") {
                        reviewRepository
                            .sliceByUser(
                                user,
                                TEST_PAGE_SIZE,
                                null,
                            ).content.size shouldBe 3
                    }
                }
            }

            describe("sliceByFoodSpots 메소드는") {
                context("음식점 ID, 사이즈, 별점 높은 순 정렬 타입을 받는 경우") {
                    it("별점 높은 순으로 음식점의 리뷰 리스트를 반환한다.") {
                        val contents =
                            reviewRepository
                                .sliceByFoodSpots(
                                    foodSpots.id,
                                    TEST_PAGE_SIZE,
                                    null,
                                    ReviewSortType.HIGHEST,
                                )
                        contents.content shouldBe listOf(reviewList[0], reviewList[2])
                    }
                }

                context("별점 높은 순 정렬타입에 별점이 똑같은 경우") {
                    it("최신순으로 나온다") {
                        val contents =
                            reviewRepository
                                .sliceByFoodSpots(
                                    otherFoodSpots.id,
                                    TEST_PAGE_SIZE,
                                    null,
                                    ReviewSortType.HIGHEST,
                                )
                        contents.content shouldBe listOf(reviewList[3], reviewList[1])
                    }
                }

                context("음식점 ID, 사이즈, 최신순 정렬 타입을 받는 경우") {
                    it("최신 순으로 해당 음식점의 리뷰 리스트를 반환한다.") {
                        val contents =
                            reviewRepository
                                .sliceByFoodSpots(
                                    foodSpots.id,
                                    TEST_PAGE_SIZE,
                                    null,
                                    ReviewSortType.LATEST,
                                )
                        contents.content shouldBe listOf(reviewList[2], reviewList[0])
                    }
                }

                context("음식점 ID, 사이즈, 마지막 ID, 정렬 타입을 받는 경우") {
                    it("해당 음식점의 리뷰 리스트를 반환한다.") {
                        val contents =
                            reviewRepository
                                .sliceByFoodSpots(
                                    foodSpots.id,
                                    TEST_PAGE_SIZE,
                                    reviewList[0].id,
                                    ReviewSortType.HIGHEST,
                                )
                        contents.content shouldBe listOf(reviewList[2])
                    }
                }

                context("음식점 ID, 사이즈, 마지막 ID, 정렬 타입, 뱃지를 받는 경우") {
                    it("해당 음식점의 리뷰 리스트를 반환한다.") {
                        val contents =
                            reviewRepository
                                .sliceByFoodSpots(
                                    foodSpots.id,
                                    TEST_PAGE_SIZE,
                                    reviewList.last().id,
                                    ReviewSortType.LATEST,
                                    Badge.BEGINNER,
                                )
                        contents.content shouldBe listOf(reviewList[2], reviewList[0])
                    }
                }
            }
            describe("getReviewAggregatedInfo 메소드는") {
                context("리뷰가 있는 음식점을 받는 경우") {
                    it("해당 음식점의 리뷰 평균 별점과 리뷰 개수를 반환한다.") {
                        val reviewAggregatedInfoResponse =
                            reviewRepository.getReviewAggregatedInfo(foodSpots)
                        reviewAggregatedInfoResponse shouldBe ReviewAggregatedInfoResponse(3.5, 2)
                    }
                }

                context("리뷰가 없는 음식점을 받는 경우") {
                    it("해당 음식점의 리뷰 평균 별점과 리뷰 개수를 반환한다.") {
                        val reviewAggregatedInfoResponse =
                            reviewRepository.getReviewAggregatedInfo(noReviewsFoodSpots)
                        reviewAggregatedInfoResponse shouldBe ReviewAggregatedInfoResponse(0.0, 0)
                    }
                }
            }

            describe("findAllUserReviewCount 메소드는") {
                it("전체 회원의 닉네임과 리뷰 개수를 정렬하여 리스트로 반환한다") {
                    val userReportCounts = reviewRepository.findAllUserReviewCount()
                    userReportCounts.size shouldBe 3
                    userReportCounts[0].userNickname shouldBe "existentNick"
                    userReportCounts[0].total shouldBe 3
                    userReportCounts[0].profileImageUrl shouldBe "test_image_name_0"

                    userReportCounts[1].userNickname shouldBe "otherUser"
                    userReportCounts[1].total shouldBe 2
                    userReportCounts[1].profileImageUrl shouldBe "test_image_name_0"

                    userReportCounts[2].userNickname shouldBe "testUser"
                    userReportCounts[2].total shouldBe 2
                    userReportCounts[2].profileImageUrl shouldBe "test_image_name_0"
                }
            }

            describe("findAllUserLikeCount 메소드는") {
                it("전체 회원의 닉네임과 좋아요 개수를 정렬하여 리스트로 반환한다") {
                    val userLikeCounts = reviewRepository.findAllUserLikeCount()
                    userLikeCounts.size shouldBe 3

                    userLikeCounts[0].userNickname shouldBe "existentNick"
                    userLikeCounts[0].total shouldBe 3
                    userLikeCounts[0].profileImageUrl shouldBe "test_image_name_0"

                    userLikeCounts[1].userNickname shouldBe "otherUser"
                    userLikeCounts[1].total shouldBe 2
                    userLikeCounts[1].profileImageUrl shouldBe "test_image_name_0"

                    userLikeCounts[2].userNickname shouldBe "testUser"
                    userLikeCounts[2].total shouldBe 2
                    userLikeCounts[2].profileImageUrl shouldBe "test_image_name_0"
                }
            }

            describe("getRatingCount 메소드는") {
                context("음식점 ID를 받는 경우") {
                    it("해당 음식점의 별점 개수를 반환한다.") {
                        val countRates = reviewRepository.getRatingCount(foodSpots.id)
                        countRates.size shouldBe 5
                        val countList = listOf(1, 0, 0, 1, 0)
                        for (i in countRates.indices) {
                            countRates[i].rating shouldBe 5 - i
                            countRates[i].count shouldBe countList[i]
                        }
                    }
                }
            }

            describe("findAllUserTotalCount 메소드는") {
                it("전체 회원의 종합 랭킹을 리스트로 반환한다") {
                    val userTotalCounts = reviewRepository.findAllUserTotalCount()
                    userTotalCounts.size shouldBe 4

                    userTotalCounts[0].userNickname shouldBe "existentNick"
                    userTotalCounts[0].total shouldBe 6
                    userTotalCounts[0].profileImageUrl shouldBe "test_image_name_0"

                    userTotalCounts[1].userNickname shouldBe "otherUser"
                    userTotalCounts[1].total shouldBe 4
                    userTotalCounts[1].profileImageUrl shouldBe "test_image_name_0"

                    userTotalCounts[2].userNickname shouldBe "testUser"
                    userTotalCounts[2].total shouldBe 4
                    userTotalCounts[2].profileImageUrl shouldBe "test_image_name_0"

                    userTotalCounts[3].userNickname shouldBe "noReviewsUser"
                    userTotalCounts[3].total shouldBe 0
                }
            }

            describe("countByUser 메소드는") {
                context("리뷰를 작성한 user 를 받는 경우") {
                    it("해당 user 의 리뷰 이력 개수를 반환한다.") {
                        val count = reviewRepository.countByUser(user)
                        count shouldBe 3
                    }
                }

                context("리뷰를 작성하지 않은 user 를 받는 경우") {
                    it("0을 반환한다.") {
                        val count = reviewRepository.countByUser(noReviewsUser)
                        count shouldBe 0
                    }
                }
            }
        },
    )
