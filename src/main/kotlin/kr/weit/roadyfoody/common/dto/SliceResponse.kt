package kr.weit.roadyfoody.common.dto

import io.swagger.v3.oas.annotations.media.Schema

open class SliceResponse<T>(
    @Schema(description = "조회된 데이터 리스트")
    val contents: List<T>,
    @Schema(description = "다음 페이지 존재 여부")
    val hasNext: Boolean,
) {
    constructor(size: Int, contents: List<T>) : this(
        if (contents.size > size) contents.dropLast(1) else contents,
        contents.size > size,
    )
}
