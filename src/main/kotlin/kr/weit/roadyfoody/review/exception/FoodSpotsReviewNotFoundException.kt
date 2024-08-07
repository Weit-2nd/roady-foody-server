package kr.weit.roadyfoody.review.exception

import kr.weit.roadyfoody.common.exception.BaseException
import kr.weit.roadyfoody.common.exception.ErrorCode

class FoodSpotsReviewNotFoundException(
    message: String = ErrorCode.NOT_FOUND_FOOD_SPOTS_REVIEW.errorMessage,
) : BaseException(ErrorCode.NOT_FOUND_FOOD_SPOTS_REVIEW, message)
