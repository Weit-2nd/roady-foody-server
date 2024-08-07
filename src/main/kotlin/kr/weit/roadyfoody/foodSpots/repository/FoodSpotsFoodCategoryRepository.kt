package kr.weit.roadyfoody.foodSpots.repository

import kr.weit.roadyfoody.foodSpots.domain.FoodSpotsFoodCategory
import org.springframework.data.jpa.repository.JpaRepository

interface FoodSpotsFoodCategoryRepository : JpaRepository<FoodSpotsFoodCategory, Long>
