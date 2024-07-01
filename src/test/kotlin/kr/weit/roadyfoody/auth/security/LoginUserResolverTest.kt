package kr.weit.roadyfoody.auth.security

import io.kotest.core.spec.style.BehaviorSpec
import kr.weit.roadyfoody.support.annotation.ControllerTest
import kr.weit.roadyfoody.support.utils.getWithAuth
import kr.weit.roadyfoody.user.domain.User
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ControllerTest
@WebMvcTest(TestController::class)
class LoginUserResolverTest(
    private val mockMvc: MockMvc,
) : BehaviorSpec({
        given("TestLoginUserResolver 테스트") {
            beforeEach { SecurityContextHolder.clearContext() }
            `when`("로그인 정보가 있는 경우") {
                then("로그인 사용자 정보를 가져온다.") {
                    mockMvc.perform(
                        getWithAuth("/test"),
                    ).andExpect(status().isOk)
                }
            }

            `when`("로그인 정보가 없는 경우") {
                then("인증된 사용자를 찾을 수 없다는 예외를 던진다.") {
                    mockMvc.perform(
                        get("/test"),
                    ).andExpect(status().isUnauthorized)
                }
            }
        }
    })

@RestController
class TestController {
    @RequestMapping("/test")
    fun testLoginUserResolver(
        @LoginUser user: User,
    ) = ResponseEntity.ok().build<User>()
}
