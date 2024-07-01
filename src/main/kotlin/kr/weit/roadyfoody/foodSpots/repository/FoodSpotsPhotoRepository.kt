package kr.weit.roadyfoody.foodSpots.repository

import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsPhoto
import org.springframework.data.jpa.repository.JpaRepository

fun FoodSpotsPhotoRepository.getByHistoryId(foodSpotsId: Long): List<FoodSpotsPhoto> = findByHistoryId(foodSpotsId)

interface FoodSpotsPhotoRepository : JpaRepository<FoodSpotsPhoto, Long> {
    fun findByHistoryId(foodSpotsId: Long): List<FoodSpotsPhoto>
}
