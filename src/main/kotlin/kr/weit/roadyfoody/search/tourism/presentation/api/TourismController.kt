package kr.weit.roadyfoody.search.tourism.presentation.api

import kr.weit.roadyfoody.search.tourism.application.service.TourismService
import kr.weit.roadyfoody.search.tourism.dto.SearchResponses
import kr.weit.roadyfoody.search.tourism.presentation.spec.TourismControllerSpec
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tourism")
class TourismController(
    private val tourismService: TourismService,
) : TourismControllerSpec {
    @GetMapping("/search")
    override fun searchTourismKeyword(
        numOfRows: Int,
        keyword: String,
    ): SearchResponses {
        return tourismService.searchTourism(numOfRows, keyword)
    }
}
