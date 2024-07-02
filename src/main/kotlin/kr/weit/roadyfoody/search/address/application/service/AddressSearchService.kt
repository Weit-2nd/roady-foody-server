package kr.weit.roadyfoody.search.address.application.service

import kr.weit.roadyfoody.common.exception.ErrorCode
import kr.weit.roadyfoody.common.exception.RoadyFoodyBadRequestException
import kr.weit.roadyfoody.search.address.application.dto.AddressResponseWrapper
import kr.weit.roadyfoody.search.address.application.dto.AddressSearchResponse
import kr.weit.roadyfoody.search.address.application.dto.AddressSearchResponses
import kr.weit.roadyfoody.search.address.config.KakaoProperties
import kr.weit.roadyfoody.search.address.presentation.client.KakaoAddressClientInterface
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

const val KAKAO_AK = "KakaoAK "

@Service
class AddressSearchService(
    private val kakaoProperties: KakaoProperties,
    private val kakaoAddressClientInterface: KakaoAddressClientInterface,
) {
    fun searchAddress(
        keyword: String,
        size: Int,
    ): AddressSearchResponses {
        require(keyword.length <= 60) { throw RoadyFoodyBadRequestException(ErrorCode.SEARCH_KEYWORD_LENGTH_LONG) }
        require(keyword.length >= 2) { throw RoadyFoodyBadRequestException(ErrorCode.SEARCH_KEYWORD_LENGTH_SHORT) }

        val encodedKeyword: String = URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8)
        val originalResponse = kakaoAddressClientInterface.searchAddress(encodedKeyword, size)
        return convertResponse(originalResponse)
    }

    private fun convertResponse(originalResponse: AddressResponseWrapper): AddressSearchResponses {
        val items =
            originalResponse.documents.map {
                AddressSearchResponse.from(it)
            }
        return AddressSearchResponses(items = items)
    }
}
