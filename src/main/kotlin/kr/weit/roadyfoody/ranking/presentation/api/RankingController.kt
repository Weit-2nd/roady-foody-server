package kr.weit.roadyfoody.ranking.presentation.api

import jakarta.validation.constraints.Positive
import kr.weit.roadyfoody.ranking.application.service.RankingQueryService
import kr.weit.roadyfoody.ranking.dto.UserRankingResponse
import kr.weit.roadyfoody.ranking.presentation.spec.RankingControllerSpec
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/ranking")
class RankingController(
    private val rankingQueryService: RankingQueryService,
) : RankingControllerSpec {
    @GetMapping("/report")
    override fun getReportRanking(
        @Positive(message = "size는 양수여야 합니다.")
        @RequestParam(defaultValue = "10")
        size: Long,
        @Positive(message = "시작값은 양수여야 합니다")
        @RequestParam(defaultValue = "1")
        start: Long,
    ): List<UserRankingResponse> = rankingQueryService.getReportRanking(size, start)

    @GetMapping("/review")
    override fun getReviewRanking(
        @Positive(message = "size는 양수여야 합니다.")
        @RequestParam(defaultValue = "10")
        size: Long,
        @Positive(message = "시작값은 양수여야 합니다")
        @RequestParam(defaultValue = "1")
        start: Long,
    ): List<UserRankingResponse> = rankingQueryService.getReviewRanking(size, start)

    @GetMapping("/like")
    override fun getLikeRanking(
        @Positive(message = "size는 양수여야 합니다")
        @RequestParam(defaultValue = "10")
        size: Long,
        @Positive(message = "시작값은 양수여야 합니다")
        @RequestParam(defaultValue = "1")
        start: Long,
    ): List<UserRankingResponse> = rankingQueryService.getLikeRanking(size, start)

    @GetMapping("total")
    override fun getTotalRanking(
        @Positive(message = "size는 양수여야 합니다")
        @RequestParam(defaultValue = "10")
        size: Long,
        @Positive(message = "시작값은 양수여야 합니다")
        @RequestParam(defaultValue = "1")
        start: Long,
    ): List<UserRankingResponse> = rankingQueryService.getTotalRanking(size, start)
}
