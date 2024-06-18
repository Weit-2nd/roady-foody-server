package kr.weit.roadyfoody.term.presentation.api

import kr.weit.roadyfoody.term.presentation.spec.TermControllerSpec
import kr.weit.roadyfoody.term.service.TermQueryService
import kr.weit.roadyfoody.term.service.dto.DetailedTermResponse
import kr.weit.roadyfoody.term.service.dto.SummaryTermsResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/terms")
class TermController(
    private val termQueryService: TermQueryService,
) : TermControllerSpec {
    @GetMapping
    override fun getAllSummaryTerms(): SummaryTermsResponse = termQueryService.getAllSummaryTerms()

    @GetMapping("/{termId}")
    override fun getDetailedTerm(
        @PathVariable termId: Long,
    ): DetailedTermResponse = termQueryService.getDetailedTerm(termId)
}
