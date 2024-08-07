package kr.weit.roadyfoody.user.application.service

import kr.weit.roadyfoody.common.dto.SliceResponse
import kr.weit.roadyfoody.foodSpots.repository.FoodSpotsHistoryRepository
import kr.weit.roadyfoody.foodSpots.repository.FoodSpotsPhotoRepository
import kr.weit.roadyfoody.foodSpots.repository.ReportFoodCategoryRepository
import kr.weit.roadyfoody.foodSpots.repository.getByHistoryId
import kr.weit.roadyfoody.foodSpots.repository.getHistoriesByUser
import kr.weit.roadyfoody.global.service.ImageService
import kr.weit.roadyfoody.review.application.dto.ReviewPhotoResponse
import kr.weit.roadyfoody.review.repository.FoodSpotsReviewPhotoRepository
import kr.weit.roadyfoody.review.repository.FoodSpotsReviewRepository
import kr.weit.roadyfoody.review.repository.getByReview
import kr.weit.roadyfoody.user.application.dto.UserInfoResponse
import kr.weit.roadyfoody.user.application.dto.UserReportCategoryResponse
import kr.weit.roadyfoody.user.application.dto.UserReportHistoriesResponse
import kr.weit.roadyfoody.user.application.dto.UserReportPhotoResponse
import kr.weit.roadyfoody.user.application.dto.UserReviewResponse
import kr.weit.roadyfoody.user.domain.User
import kr.weit.roadyfoody.user.repository.UserRepository
import kr.weit.roadyfoody.user.repository.getByUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserQueryService(
    private val userRepository: UserRepository,
    private val imageService: ImageService,
    private val foodSpotsHistoryRepository: FoodSpotsHistoryRepository,
    private val foodSpotsPhotoRepository: FoodSpotsPhotoRepository,
    private val reportFoodCategoryRepository: ReportFoodCategoryRepository,
    private val reviewRepository: FoodSpotsReviewRepository,
    private val reviewPhotoRepository: FoodSpotsReviewPhotoRepository,
) {
    fun getUserInfo(user: User): UserInfoResponse {
        val user = userRepository.getByUserId(user.id)
        val profileImageUrl = user.profile.profileImageName?.let { imageService.getDownloadUrl(it) }

        return UserInfoResponse.of(
            user.profile.nickname,
            profileImageUrl,
            user.coin,
        )
    }

    @Transactional(readOnly = true)
    fun getReportHistories(
        userId: Long,
        size: Int,
        lastId: Long?,
    ): SliceResponse<UserReportHistoriesResponse> {
        val user = userRepository.getByUserId(userId)
        val reportResponse =
            foodSpotsHistoryRepository.getHistoriesByUser(user, size, lastId).map {
                val reportPhotoResponse =
                    foodSpotsPhotoRepository.getByHistoryId(it.id).map { photo ->
                        UserReportPhotoResponse(photo, imageService.getDownloadUrl(photo.fileName))
                    }
                val reportCategoryResponse =
                    reportFoodCategoryRepository.getByHistoryId(it.id).map { category ->
                        UserReportCategoryResponse(category)
                    }
                UserReportHistoriesResponse(it, reportPhotoResponse, reportCategoryResponse)
            }
        return SliceResponse(reportResponse)
    }

    @Transactional(readOnly = true)
    fun getUserReviews(
        userId: Long,
        size: Int,
        lastId: Long?,
    ): SliceResponse<UserReviewResponse> {
        val user = userRepository.getByUserId(userId)
        val response =
            reviewRepository
                .sliceByUser(user, size, lastId)
                .map {
                    val reviewPhotos =
                        reviewPhotoRepository.getByReview(it).map { photo ->
                            ReviewPhotoResponse(
                                photo.id,
                                imageService.getDownloadUrl(photo.fileName),
                            )
                        }
                    UserReviewResponse(it, reviewPhotos)
                }
        return SliceResponse(response)
    }
}
