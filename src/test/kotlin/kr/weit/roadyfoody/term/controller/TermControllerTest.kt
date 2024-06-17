package kr.weit.roadyfoody.term.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.verify
import kr.weit.roadyfoody.common.exception.ErrorResponse
import kr.weit.roadyfoody.global.jsonmapper.ObjectMapperProvider
import kr.weit.roadyfoody.support.annotation.ControllerTest
import kr.weit.roadyfoody.term.exception.TermNotFoundException
import kr.weit.roadyfoody.term.exception.TermNotFoundException.Companion.getTermNotFoundMessage
import kr.weit.roadyfoody.term.fixture.TEST_NONEXISTENT_TERM_ID
import kr.weit.roadyfoody.term.fixture.createTestDetailedTermResponse
import kr.weit.roadyfoody.term.fixture.createTestDetailedTermsResponse
import kr.weit.roadyfoody.term.fixture.createTestSummaryTermsResponse
import kr.weit.roadyfoody.term.fixture.createTestTermIds
import kr.weit.roadyfoody.term.presentation.api.TermController
import kr.weit.roadyfoody.term.service.TermQueryService
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerTest
@WebMvcTest(TermController::class)
class TermControllerTest(
    private val objectMapperProvider: ObjectMapperProvider,
    @MockkBean val termQueryService: TermQueryService,
    private val mockMvc: MockMvc,
) : BehaviorSpec({
        val requestPath = "/api/v1/terms"

        given("GET $requestPath/summary 테스트") {
            `when`("정상적인 요청을 보내면") {
                every { termQueryService.getAllSummaryTerms() } returns createTestSummaryTermsResponse()
                then("200 상태번호와 SummaryTermsResponse 를 반환한다.") {
                    mockMvc
                        .perform(get("$requestPath/summary"))
                        .andExpect(status().isOk)
                        .andExpect(
                            content().json(
                                objectMapperProvider.objectMapper.writeValueAsString(createTestSummaryTermsResponse()),
                            ),
                        )
                    verify { termQueryService.getAllSummaryTerms() }
                }
            }
        }

        given("GET $requestPath 테스트") {
            `when`("정상적인 요청을 보내면") {
                every { termQueryService.getAllDetailedTerms() } returns createTestDetailedTermsResponse()
                then("200 상태번호와 DetailedTermsResponse 를 반환한다.") {
                    mockMvc
                        .perform(get(requestPath))
                        .andExpect(status().isOk)
                        .andExpect(
                            content().json(
                                objectMapperProvider.objectMapper.writeValueAsString(createTestDetailedTermsResponse()),
                            ),
                        )
                    verify { termQueryService.getAllDetailedTerms() }
                }
            }
        }

        given("GET $requestPath/{termId} 테스트") {
            `when`("정상적인 요청을 보내면") {
                every { termQueryService.getDetailedTerm(createTestTermIds().first()) } returns
                    createTestDetailedTermResponse(createTestTermIds().first())
                then("200 상태번호와 DetailedTermResponse 를 반환한다.") {
                    mockMvc
                        .perform(get("$requestPath/${createTestTermIds().first()}"))
                        .andExpect(status().isOk)
                        .andExpect(
                            content().json(
                                objectMapperProvider.objectMapper.writeValueAsString(
                                    createTestDetailedTermResponse(createTestTermIds().first()),
                                ),
                            ),
                        )
                    verify { termQueryService.getDetailedTerm(createTestTermIds().first()) }
                }
            }

            `when`("존재하지 않는 termId 를 보내면") {
                val termNotFoundEx = TermNotFoundException(getTermNotFoundMessage(TEST_NONEXISTENT_TERM_ID))
                every { termQueryService.getDetailedTerm(TEST_NONEXISTENT_TERM_ID) } throws termNotFoundEx
                then("${termNotFoundEx.errorCode.httpStatus.value()} 응답을 반환한다.") {
                    mockMvc
                        .perform(get("$requestPath/${TEST_NONEXISTENT_TERM_ID}"))
                        .andExpect(status().`is`(termNotFoundEx.errorCode.httpStatus.value()))
                        .andExpect(
                            content().json(
                                objectMapperProvider.objectMapper.writeValueAsString(
                                    ErrorResponse.of(termNotFoundEx.errorCode, termNotFoundEx.message),
                                ),
                            ),
                        )
                    verify { termQueryService.getDetailedTerm(TEST_NONEXISTENT_TERM_ID) }
                }
            }
        }
    })