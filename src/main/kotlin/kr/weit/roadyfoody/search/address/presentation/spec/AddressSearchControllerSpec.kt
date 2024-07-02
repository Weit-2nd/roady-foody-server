package kr.weit.roadyfoody.search.address.presentation.spec

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kr.weit.roadyfoody.common.exception.ErrorCode
import kr.weit.roadyfoody.global.swagger.ApiErrorCodeExamples
import kr.weit.roadyfoody.global.swagger.v1.SwaggerTag
import kr.weit.roadyfoody.search.address.application.dto.AddressSearchResponse
import kr.weit.roadyfoody.search.address.application.dto.AddressSearchResponses

@Tag(name = SwaggerTag.SEARCH)
interface AddressSearchControllerSpec {
    @Operation(
        description = "주소 검색 API",
        parameters = [
            Parameter(name = "numOfRows", description = "반환받을 데이터 수", required = true, example = "10"),
            Parameter(name = "keyword", description = "검색할 키워드", required = true, example = "명륜진사갈비"),
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AddressSearchResponse::class),
                    ),
                ],
            ),
        ],
    )
    @ApiErrorCodeExamples(
        [
            ErrorCode.REST_CLIENT_ERROR,
            ErrorCode.SEARCH_KEYWORD_LENGTH_SHORT,
            ErrorCode.SEARCH_KEYWORD_LENGTH_LONG,
        ],
    )
    fun searchAddress(
        numOfRows: Int,
        keyword: String,
    ): AddressSearchResponses
}
