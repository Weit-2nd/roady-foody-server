package kr.weit.roadyfoody.search.address.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.weit.roadyfoody.common.exception.ErrorCode
import kr.weit.roadyfoody.common.exception.RoadyFoodyBadRequestException
import kr.weit.roadyfoody.global.TEST_KEYWORD
import kr.weit.roadyfoody.global.TEST_PAGE_SIZE
import kr.weit.roadyfoody.search.address.config.KakaoProperties
import kr.weit.roadyfoody.search.address.fixture.AddressFixture
import kr.weit.roadyfoody.search.address.presentation.client.KakaoAddressClientInterface

class AddressSearchServiceTest :
    BehaviorSpec({
        val kakaoProperties = KakaoProperties("apiKey")
        val kakaoAddressClientInterface = mockk<KakaoAddressClientInterface>()

        val addressService = AddressSearchService(kakaoProperties, kakaoAddressClientInterface)

        given("searchAddress 테스트") {
            `when`("정상적으로 주소 검색이 가능한 경우") {
                val addressResponseWrapper = AddressFixture.loadAddressResponseSize10()

                every {
                    kakaoAddressClientInterface.searchAddress(
                        TEST_KEYWORD,
                        TEST_PAGE_SIZE,
                    )
                } returns addressResponseWrapper

                val addressResponses = addressService.searchAddress(TEST_KEYWORD, TEST_PAGE_SIZE)

                then("주소 검색을 반환한다.") {
                    addressResponses.items.shouldHaveSize(TEST_PAGE_SIZE)
                    val expectedPlaceName =
                        listOf(
                            "주소0",
                            "주소1",
                            "주소2",
                            "주소3",
                            "주소4",
                            "주소5",
                            "주소6",
                            "주소7",
                            "주소8",
                            "주소9",
                        )

                    for ((index, placeName)in expectedPlaceName.withIndex()) {
                        addressResponses.items[index].placeName shouldBe placeName
                    }
                }
            }

            `when`("주소 검색 결과가 없는 경우") {
                val addressResponseWrapper = AddressFixture.loadAddressResponseSize0()

                every {
                    kakaoAddressClientInterface.searchAddress(
                        TEST_KEYWORD,
                        TEST_PAGE_SIZE,
                    )
                } returns addressResponseWrapper

                val addressResponses = addressService.searchAddress(TEST_KEYWORD, TEST_PAGE_SIZE)

                then("빈 리스트를 반환한다.") {
                    addressResponses.items.shouldBeEmpty()
                }
            }
            `when`("keyword 길이가 60자 초과인 경우") {
                val keyword = "a".repeat(61)

                then("SEARCH_KEYWORD_LENGTH_LONG ErrorCode를 던진다.") {
                    val exception =
                        shouldThrow<RoadyFoodyBadRequestException> {
                            addressService.searchAddress(keyword, TEST_PAGE_SIZE)
                        }
                    exception.errorCode shouldBe ErrorCode.SEARCH_KEYWORD_LENGTH_LONG
                }
            }
            `when`("keyword 길이가 2자 미만인 경우") {
                val keyword = "a"

                then("SEARCH_KEYWORD_LENGTH_SHORT ErrorCode를 던진다.") {
                    val exception =
                        shouldThrow<RoadyFoodyBadRequestException> {
                            addressService.searchAddress(keyword, TEST_PAGE_SIZE)
                        }
                    exception.errorCode shouldBe ErrorCode.SEARCH_KEYWORD_LENGTH_SHORT
                }
            }
        }
    })
