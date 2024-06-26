package kr.weit.roadyfoody.search.tourism.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class ResponseWrapper(
    @JsonProperty("response") val response: Response,
)

data class Response(
    @JsonProperty("header") val header: Header,
    @JsonProperty("body") val body: Body,
)

data class Header(
    @JsonProperty("resultCode") val resultCode: String,
    @JsonProperty("resultMsg") val resultMsg: String,
)

data class Body(
    @JsonProperty("items") @JsonDeserialize(using = ItemsDeserializer::class) val items: Items,
    @JsonProperty("numOfRows") val numOfRows: Int,
    @JsonProperty("pageNo") val pageNo: Int,
    @JsonProperty("totalCount") val totalCount: Int,
)

data class Items(
    @JsonProperty("item") val item: List<TourismItem> = emptyList(),
)

data class TourismItem(
    @JsonProperty("addr1") val addr1: String,
    @JsonProperty("addr2") val addr2: String,
    @JsonProperty("areacode") val areaCode: String,
    @JsonProperty("booktour") val bookTour: String,
    @JsonProperty("cat1") val cat1: String,
    @JsonProperty("cat2") val cat2: String,
    @JsonProperty("cat3") val cat3: String,
    @JsonProperty("contentid") val contentId: String,
    @JsonProperty("contenttypeid") val contentTypeId: String,
    @JsonProperty("createdtime") val createdTime: String,
    @JsonProperty("firstimage") val firstImage: String,
    @JsonProperty("firstimage2") val firstImage2: String,
    @JsonProperty("cpyrhtDivCd") val cpyrhtDivCd: String,
    @JsonProperty("mapx") val mapX: Double,
    @JsonProperty("mapy") val mapY: Double,
    @JsonProperty("mlevel") val mLevel: String,
    @JsonProperty("modifiedtime") val modifiedTime: String,
    @JsonProperty("sigungucode") val sigunguCode: String,
    @JsonProperty("tel") val tel: String,
    @JsonProperty("title") val title: String,
)
