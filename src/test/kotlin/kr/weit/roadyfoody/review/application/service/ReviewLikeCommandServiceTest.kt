package kr.weit.roadyfoody.review.application.service

import TEST_REVIEW_ID
import createMockReviewLike
import createMockTestReview
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.weit.roadyfoody.common.exception.ErrorCode
import kr.weit.roadyfoody.common.exception.RoadyFoodyBadRequestException
import kr.weit.roadyfoody.review.repository.FoodSpotsReviewRepository
import kr.weit.roadyfoody.review.repository.ReviewLikeRepository
import kr.weit.roadyfoody.review.repository.getReviewByReviewId
import kr.weit.roadyfoody.user.fixture.createTestUser

class ReviewLikeCommandServiceTest :
    BehaviorSpec(
        {
            val reviewRepository = mockk<FoodSpotsReviewRepository>()
            val reviewLikeRepository = mockk<ReviewLikeRepository>()
            val reviewLikeService =
                ReviewLikeCommandService(
                    reviewRepository,
                    reviewLikeRepository,
                )
            var review = createMockTestReview()
            afterEach { clearAllMocks() }

            given("toggleLike 테스트") {
                var expectedLikeTotal = review.likeTotal + 1
                every { reviewRepository.getReviewByReviewId(any()) } returns review
                every { reviewLikeRepository.existsByReviewAndUser(any(), any()) } returns false
                every { reviewLikeRepository.save(any()) } returns createMockReviewLike()
                `when`("리뷰에 좋아요 이력이 없는 경우") {
                    then("리뷰 좋아요 이력 생성되고 리뷰의 좋아요 수가 증가한다.") {
                        reviewLikeService.toggleLike(review.id, createTestUser())
                        verify(exactly = 1) {
                            reviewRepository.getReviewByReviewId(any())
                            reviewLikeRepository.existsByReviewAndUser(any(), any())
                            reviewLikeRepository.save(any())
                        }
                        review.likeTotal shouldBe expectedLikeTotal
                    }
                }

                expectedLikeTotal = review.likeTotal - 1
                every { reviewRepository.getReviewByReviewId(any()) } returns review
                every { reviewLikeRepository.existsByReviewAndUser(any(), any()) } returns true
                every { reviewLikeRepository.deleteByReviewAndUser(any(), any()) } returns Unit
                `when`("리뷰에 좋아요 이력이 있는 경우") {
                    then("리뷰 좋아요 이력이 삭제되고 리뷰의 좋아요 수가 감소한다.") {
                        reviewLikeService.toggleLike(TEST_REVIEW_ID, createTestUser())
                        verify(exactly = 1) {
                            reviewRepository.getReviewByReviewId(any())
                            reviewLikeRepository.existsByReviewAndUser(any(), any())
                            reviewLikeRepository.deleteByReviewAndUser(any(), any())
                        }
                        review.likeTotal shouldBe expectedLikeTotal
                    }
                }

                `when`("좋아요 수가 0인 경우") {
                    every { reviewRepository.getReviewByReviewId(any()) } returns
                        createMockTestReview(
                            likeTotal = 0,
                        )
                    every { reviewLikeRepository.existsByReviewAndUser(any(), any()) } returns true
                    then("리뷰 좋아요 수는 음수가 될 수 없다는 예외가 발생한다.") {
                        shouldThrow<RoadyFoodyBadRequestException> {
                            reviewLikeService.toggleLike(TEST_REVIEW_ID, createTestUser())
                        }.message shouldBe ErrorCode.NEGATIVE_NUMBER_OF_LIKED.errorMessage
                    }
                }
            }

            given("decreaseLikeLock 테스트") {
                review = createMockTestReview(likeTotal = 0)
                `when`("리뷰 좋아요 수가 0인 경우") {
                    then("리뷰 좋아요 수는 음수가 될 수 없다는 예외가 발생한다.") {
                        shouldThrow<RoadyFoodyBadRequestException> {
                            reviewLikeService.decreaseLikeLock(review, review.id)
                        }.message shouldBe ErrorCode.NEGATIVE_NUMBER_OF_LIKED.errorMessage
                    }
                }
                `when`("리뷰 좋아요 수가 2인 경우") {
                    review = createMockTestReview(likeTotal = 2)
                    then("리뷰 좋아요 수는 1이 된다.") {
                        reviewLikeService.decreaseLikeLock(review, review.id)
                        review.likeTotal shouldBe 1
                    }
                }
            }
        },
    )
