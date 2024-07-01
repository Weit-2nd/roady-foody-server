package kr.weit.roadyfoody.global.swagger.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders.AUTHORIZATION

@Profile("sandbox")
@Configuration
class ApiDocsConfig {
    @Bean
    fun publicApi(): GroupedOpenApi {
        // pathsToMatch로 원하는 경로의 api만 나오도록 설정
        return GroupedOpenApi.builder()
            .group("API")
            .pathsToMatch("/api/**")
            .build()
    }

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(createApiInfo())
            .components(createSecurityComponents())
            .addSecurityItem(createSecurityRequirement())
    }

    private fun createApiInfo(): Info {
        return Info()
            .title("로디푸디 API")
            .description("로디푸디 API 문서입니다.")
            .version("1.0.0")
    }

    private fun createSecurityComponents(): Components {
        return Components()
            .addSecuritySchemes(
                AUTHORIZATION,
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .`in`(SecurityScheme.In.HEADER)
                    .name(AUTHORIZATION),
            )
    }

    private fun createSecurityRequirement(): SecurityRequirement {
        return SecurityRequirement()
            .addList(AUTHORIZATION)
    }
}
