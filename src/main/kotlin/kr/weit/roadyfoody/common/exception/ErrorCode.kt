package kr.weit.roadyfoody.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val httpStatus: HttpStatus, val code: Int, val errorMessage: String) {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, -10000, "Invalid request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, -10001, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, -10002, "Forbidden"),
    NO_SUCH_ELEMENT(HttpStatus.NOT_FOUND, -10003, "No such element"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, -10004, "Internal server error"),
    EXIST_RESOURCE(HttpStatus.CONFLICT, -10005, "Exist resource"),
    NOT_FOUND_DEFAULT_RESOURCE(HttpStatus.INTERNAL_SERVER_ERROR, -10007, "Not found default resource"),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, -10008, "Payload too large"),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, -10009, "Not found user"),
    NOT_FOUND_TERM(HttpStatus.NOT_FOUND, -10010, "Not found term"),

    // external API error 11000대

    REST_CLIENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, -11000, "외부 API 호출 중 에러 발생"),
    RETRIES_EXCEEDED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, -11001, "외부 API 호출 재시도 횟수 초과"),
}
