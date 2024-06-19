package kr.weit.roadyfoody.test.controller

import io.kotest.core.spec.style.BehaviorSpec
import kr.weit.roadyfoody.global.jsonmapper.ObjectMapperProvider
import kr.weit.roadyfoody.global.log.TraceManager
import kr.weit.roadyfoody.support.annotation.ControllerTest
import kr.weit.roadyfoody.support.utils.getWithAuth
import kr.weit.roadyfoody.test.presentation.api.TestController
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// TODO : TestControllerTest.kt 를 걷어낼 때 같이 걷어내면 됩니다.
@ControllerTest
@WebMvcTest(TestController::class)
class TestControllerTest(
    private val mockMvc: MockMvc,
) : BehaviorSpec({
        given("Header에 userid가 있는 경우") {
            `when`("/api/v1/test/filter GET 요청하면") {
                then("성공한다.") {
                    mockMvc
                        .perform(
                            getWithAuth("/api/v1/test/filter"),
                        ).andExpect(status().isOk)
                }
            }
        }

        given("Header에 userid가 없는 경우") {
            `when`("/api/v1/test/filter GET 요청하면") {
                then("실패한다.") {
                    mockMvc
                        .perform(
                            get("/api/v1/test/filter"),
                        ).andExpect(status().isUnauthorized)
                }
            }
        }
    })
