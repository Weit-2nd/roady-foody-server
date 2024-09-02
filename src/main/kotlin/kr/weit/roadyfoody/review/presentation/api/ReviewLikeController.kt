package kr.weit.roadyfoody.review.presentation.api

import jakarta.validation.constraints.Positive
import kr.weit.roadyfoody.auth.security.LoginUser
import kr.weit.roadyfoody.review.application.service.ReviewLikeCommandService
import kr.weit.roadyfoody.review.presentation.spec.ReviewLikeControllerSpec
import kr.weit.roadyfoody.user.domain.User
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/likes")
class ReviewLikeController(
    private val reviewLikeCommandService: ReviewLikeCommandService,
) : ReviewLikeControllerSpec {
    @PostMapping
    @ResponseStatus(CREATED)
    override fun likeReview(
        @LoginUser
        user: User,
        @Positive(message = "리뷰 ID는 양수여야 합니다.")
        @PathVariable("reviewId") reviewId: Long,
    ) {
        reviewLikeCommandService.likeReview(reviewId, user)
    }

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    override fun unlikeReview(
        @LoginUser
        user: User,
        @Positive(message = "리뷰 ID는 양수여야 합니다.")
        @PathVariable("reviewId") reviewId: Long,
    ) {
        reviewLikeCommandService.unlikeReview(reviewId, user)
    }
}
