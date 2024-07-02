package kr.weit.roadyfoody.global.swagger

import kr.weit.roadyfoody.common.exception.ErrorCode

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiErrorCodeExamples(val value: Array<ErrorCode>)
