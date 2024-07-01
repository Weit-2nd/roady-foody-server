package kr.weit.roadyfoody.foodSpots.repository

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsHistory
import kr.weit.roadyfoody.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun FoodSpotsHistoryRepository.getHistoriesByUser(
    user: User,
    size: Int,
    lastId: Long?,
): List<FoodSpotsHistory> = findSliceByUser(user, size, lastId)

@Repository
interface FoodSpotsHistoryRepository :
    JpaRepository<FoodSpotsHistory, Long>,
    CustomFoodSpotsHistoryRepository

interface CustomFoodSpotsHistoryRepository {
    fun findSliceByUser(
        user: User,
        size: Int,
        lastId: Long?,
    ): List<FoodSpotsHistory>
}

class CustomFoodSpotsHistoryRepositoryImpl(
    private val queryFactory: QueryFactory,
) : CustomFoodSpotsHistoryRepository {
    override fun findSliceByUser(
        user: User,
        size: Int,
        lastId: Long?,
    ): List<FoodSpotsHistory> =
        queryFactory.listQuery {
            select(entity(FoodSpotsHistory::class))
            from(entity(FoodSpotsHistory::class))
            where(
                and(
                    if (lastId != null) {
                        col(FoodSpotsHistory::id).lessThan(lastId)
                    } else {
                        PredicateSpec.empty
                    },
                    col(FoodSpotsHistory::user).equal(user),
                ),
            )
            orderBy(col(FoodSpotsHistory::id).desc())
            limit(size + 1)
        }
}
