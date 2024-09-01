package kr.weit.roadyfoody.review.repository

import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicate
import com.linecorp.kotlinjdsl.querymodel.jpql.sort.Sortable
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import kr.weit.roadyfoody.badge.domain.Badge
import kr.weit.roadyfoody.foodSpots.application.dto.ReviewAggregatedInfoResponse
import kr.weit.roadyfoody.foodSpots.domain.FoodSpots
import kr.weit.roadyfoody.global.utils.getSlice
import kr.weit.roadyfoody.review.domain.FoodSpotsReview
import kr.weit.roadyfoody.review.exception.FoodSpotsReviewNotFoundException
import kr.weit.roadyfoody.user.domain.User
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository

fun FoodSpotsReviewRepository.getReviewByReviewId(reviewId: Long): FoodSpotsReview =
    findById(reviewId).orElseThrow {
        FoodSpotsReviewNotFoundException()
    }

interface FoodSpotsReviewRepository :
    JpaRepository<FoodSpotsReview, Long>,
    CustomFoodSpotsReviewRepository {
    fun findByUser(user: User): List<FoodSpotsReview>
}

interface CustomFoodSpotsReviewRepository {
    fun sliceByUser(
        user: User,
        size: Int,
        lastId: Long?,
    ): Slice<FoodSpotsReview>

    fun sliceByFoodSpots(
        foodSpotsId: Long,
        size: Int,
        lastId: Long?,
        sortType: ReviewSortType,
        badge: Badge? = null,
    ): Slice<FoodSpotsReview>

    fun getReviewAggregatedInfo(foodSpots: FoodSpots): ReviewAggregatedInfoResponse
}

class CustomFoodSpotsReviewRepositoryImpl(
    private val kotlinJdslJpqlExecutor: KotlinJdslJpqlExecutor,
) : CustomFoodSpotsReviewRepository {
    override fun sliceByUser(
        user: User,
        size: Int,
        lastId: Long?,
    ): Slice<FoodSpotsReview> {
        val pageable = Pageable.ofSize(size)
        return kotlinJdslJpqlExecutor.getSlice(pageable) {
            select(entity(FoodSpotsReview::class))
                .from(entity(FoodSpotsReview::class))
                .whereAnd(
                    if (lastId != null) {
                        path(FoodSpotsReview::id).lessThan(lastId)
                    } else {
                        null
                    },
                    path(FoodSpotsReview::user).equal(user),
                ).orderBy(path(FoodSpotsReview::id).desc())
        }
    }

    override fun sliceByFoodSpots(
        foodSpotsId: Long,
        size: Int,
        lastId: Long?,
        sortType: ReviewSortType,
        badge: Badge?,
    ): Slice<FoodSpotsReview> {
        val pageable = Pageable.ofSize(size)
        return kotlinJdslJpqlExecutor.getSlice(pageable) {
            select(entity(FoodSpotsReview::class))
                .from(entity(FoodSpotsReview::class))
                .whereAnd(
                    dynamicLastId(sortType, lastId),
                    path(FoodSpotsReview::foodSpots)(FoodSpots::id).equal(foodSpotsId),
                    badge?.let { path(FoodSpotsReview::user)(User::badge).equal(badge) },
                ).orderBy(
                    *dynamicOrder(sortType),
                )
        }
    }

    override fun getReviewAggregatedInfo(foodSpots: FoodSpots): ReviewAggregatedInfoResponse =
        kotlinJdslJpqlExecutor
            .findAll {
                selectNew<ReviewAggregatedInfoResponse>(
                    avg(path(FoodSpotsReview::rate)),
                    count(path(FoodSpotsReview::id)),
                ).from(entity(FoodSpotsReview::class))
                    .where(path(FoodSpotsReview::foodSpots).equal(foodSpots))
            }.first()!!

    private fun Jpql.dynamicOrder(sortType: ReviewSortType): Array<Sortable> =
        when (sortType) {
            ReviewSortType.LATEST -> arrayOf(path(FoodSpotsReview::id).desc())
            ReviewSortType.HIGHEST ->
                arrayOf(
                    path(FoodSpotsReview::rate).desc(),
                    path(FoodSpotsReview::id).desc(),
                )
        }

    private fun Jpql.dynamicLastId(
        sortType: ReviewSortType,
        lastId: Long?,
    ): Predicate? =
        if (lastId != null) {
            when (sortType) {
                ReviewSortType.LATEST -> path(FoodSpotsReview::id).lessThan(lastId)
                ReviewSortType.HIGHEST -> {
                    val rate = getRateByLastId(lastId)
                    or(
                        and(
                            path(FoodSpotsReview::rate).equal(rate),
                            path(FoodSpotsReview::id).lessThan(lastId),
                        ),
                        path(FoodSpotsReview::rate).lessThan(rate),
                    )
                }
            }
        } else {
            null
        }

    private fun getRateByLastId(lastId: Long): Int =
        kotlinJdslJpqlExecutor
            .findAll {
                select(path(FoodSpotsReview::rate))
                    .from(entity(FoodSpotsReview::class))
                    .where(path(FoodSpotsReview::id).equal(lastId))
            }.firstNotNullOf { it }
}

enum class ReviewSortType {
    LATEST,
    HIGHEST,
}
