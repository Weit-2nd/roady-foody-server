package kr.weit.roadyfoody.foodSpots.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import kr.weit.roadyfoody.foodSpots.domain.FoodSpots
import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsHistory
import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsPhoto
import kr.weit.roadyfoody.global.utils.findOne
import org.springframework.data.jpa.repository.JpaRepository

interface FoodSpotsPhotoRepository :
    JpaRepository<FoodSpotsPhoto, Long>,
    CustomFoodSpotsPhotoRepository {
    fun findByHistoryId(historyId: Long): List<FoodSpotsPhoto>

    fun findByHistoryIn(histories: List<FoodSpotsHistory>): List<FoodSpotsPhoto>
}

interface CustomFoodSpotsPhotoRepository {
    fun findOneByFoodSpots(foodSpotsId: Long): FoodSpotsPhoto?
}

class CustomFoodSpotsPhotoRepositoryImpl(
    private val kotlinJdslJpqlExecutor: KotlinJdslJpqlExecutor,
) : CustomFoodSpotsPhotoRepository {
    override fun findOneByFoodSpots(foodSpotsId: Long): FoodSpotsPhoto? =
        kotlinJdslJpqlExecutor
            .findOne {
                select(entity(FoodSpotsPhoto::class))
                    .from(entity(FoodSpotsPhoto::class))
                    .where(
                        path(FoodSpotsPhoto::history)
                            .path(FoodSpotsHistory::foodSpots)
                            .path(FoodSpots::id)
                            .eq(foodSpotsId),
                    ).orderBy(path(FoodSpotsPhoto::id).desc())
            }
}
