package kr.weit.roadyfoody.foodSpots.repository

import kr.weit.roadyfoody.foodSpots.domain.ReportFoodCategory
import org.springframework.data.jpa.repository.JpaRepository

interface ReportFoodCategoryRepository : JpaRepository<ReportFoodCategory, Long>
