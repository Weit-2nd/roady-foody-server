package kr.weit.roadyfoody.common.exception

class RoadyFoodyBadRequestException(
    errorCode: ErrorCode,
) : BaseException(errorCode, errorCode.errorMessage)
