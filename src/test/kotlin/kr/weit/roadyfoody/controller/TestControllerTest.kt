package kr.weit.roadyfoody.controller

import io.kotest.core.spec.style.BehaviorSpec
import kr.weit.roadyfoody.config.SecurityConfig
import kr.weit.roadyfoody.security.handler.CustomAuthenticationEntryPoint
import kr.weit.roadyfoody.support.log.TraceManager
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ActiveProfiles("test")
@Import(TraceManager::class, SecurityConfig::class, CustomAuthenticationEntryPoint::class)
@WebMvcTest(TestController::class)
class TestControllerTest(
    private val mockMvc: MockMvc,
) : BehaviorSpec({
        given("Header에 userid가 있는 경우") {
            `when`("/api/v1/test/filter GET 요청하면") {
                then("성공한다.") {
                    mockMvc.perform(
                        get("/api/v1/test/filter")
                            .header("userid", ""),
                    )
                        .andExpect(status().isOk)
                }
            }
        }

        given("Header에 userid가 없는 경우") {
            `when`("/api/v1/test/filter GET 요청하면") {
                then("실패한다.") {
                    mockMvc.perform(
                        get("/api/v1/test/filter"),
                    )
                        .andExpect(status().isUnauthorized)
                }
            }
        }
    })