package kr.weit.roadyfoody.search.address.presentation.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.verify
import kr.weit.roadyfoody.search.address.application.service.AddressSearchService
import kr.weit.roadyfoody.search.address.fixture.AddressFixture.createSearchResponses
import kr.weit.roadyfoody.support.annotation.ControllerTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerTest
@WebMvcTest(AddressSearchController::class)
class AddressSearchControllerTest(
    private val objectMapper: ObjectMapper,
    @MockkBean private val addressSearchService: AddressSearchService,
    private val mockMvc: MockMvc,
) : BehaviorSpec({
        val requestPath = "/api/v1/address"

        given("GET $requestPath/search 테스트") {
            `when`("키워드로 주소 검색 요청을 보내면") {
                every { addressSearchService.searchAddress("주소", 2) } returns createSearchResponses()
                then("200 상태 번호와 AddressSearchResponses 반환한다.") {
                    mockMvc.perform(get("$requestPath/search?keyword=주소&numOfRows=2"))
                        .andExpect(status().isOk)
                        .andExpect(
                            content().json(
                                objectMapper.writeValueAsString(
                                    createSearchResponses(),
                                ),
                            ),
                        )
                    verify(exactly = 1) { addressSearchService.searchAddress("주소", 2) }
                }
            }
        }
    })
