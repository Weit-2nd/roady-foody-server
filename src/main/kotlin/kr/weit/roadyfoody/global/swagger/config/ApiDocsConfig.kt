package kr.weit.roadyfoody.global.swagger.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import kr.weit.roadyfoody.common.exception.ErrorCode
import kr.weit.roadyfoody.common.exception.ErrorResponse
import kr.weit.roadyfoody.global.swagger.ApiErrorCodeExample
import kr.weit.roadyfoody.global.swagger.ApiErrorCodeExamples
import kr.weit.roadyfoody.global.swagger.ExampleHolder
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.method.HandlerMethod

@Configuration
class ApiDocsConfig {
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

    @Bean
    fun customize(): OperationCustomizer {
        return OperationCustomizer { operation: Operation, handlerMethod: HandlerMethod ->
            val apiErrorCodeExamples = handlerMethod.getMethodAnnotation(ApiErrorCodeExamples::class.java)
            val apiErrorCodeExample = handlerMethod.getMethodAnnotation(ApiErrorCodeExample::class.java)

            apiErrorCodeExamples?.run {
                generateErrorCodeResponseExample(operation, apiErrorCodeExamples.value)
            }

            apiErrorCodeExample?.run {
                generateErrorCodeResponseExample(operation, apiErrorCodeExample.value)
            }

            operation
        }
    }

    // apiErrorCodeExamples 에서 받은 ErrorCode 배열을 통해 ErrorResponse Example을 생성
    private fun generateErrorCodeResponseExample(
        operation: Operation,
        errorCodes: Array<ErrorCode>,
    ) {
        val responses = operation.responses

        val statusWithExampleHolders =
            errorCodes.map { errorCode ->
                ExampleHolder(
                    holder = getSwaggerExample(errorCode),
                    code = errorCode.httpStatus.value(),
                    name = errorCode.name,
                )
            }.groupBy { it.code }

        addExamplesToResponses(responses, statusWithExampleHolders)
    }

    // apiErrorCodeExample 에서 받은 ErrorCode를 통해 ErrorResponse Example을 생성
    private fun generateErrorCodeResponseExample(
        operation: Operation,
        errorCode: ErrorCode,
    ) {
        val responses = operation.responses
        val exampleHolder =
            ExampleHolder(
                holder = getSwaggerExample(errorCode),
                name = errorCode.name,
                code = errorCode.httpStatus.value(),
            )
        addExamplesToResponses(responses, exampleHolder)
    }

    private fun getSwaggerExample(errorCode: ErrorCode): Example {
        val errorResponseDto = ErrorResponse.of(errorCode, errorCode.errorMessage)
        val example = Example()
        example.value = errorResponseDto
        return example
    }

    private fun addExamplesToResponses(
        responses: ApiResponses,
        statusWithExampleHolders: Map<Int, List<ExampleHolder>>,
    ) {
        statusWithExampleHolders.forEach { (status, v) ->
            val content = Content()
            val mediaType = MediaType()
            val apiResponse = ApiResponse()

            v.forEach { exampleHolder ->
                mediaType.addExamples(exampleHolder.name, exampleHolder.holder)
            }
            content.addMediaType("application/json", mediaType)
            apiResponse.content = content
            responses.addApiResponse(status.toString(), apiResponse)
        }
    }

    private fun addExamplesToResponses(
        responses: ApiResponses,
        exampleHolder: ExampleHolder,
    ) {
        val content = Content()
        val mediaType = MediaType()
        val apiResponse = ApiResponse()

        mediaType.addExamples(exampleHolder.name, exampleHolder.holder)
        content.addMediaType("application/json", mediaType)
        apiResponse.content = content
        responses.addApiResponse(exampleHolder.code.toString(), apiResponse)
    }
}
