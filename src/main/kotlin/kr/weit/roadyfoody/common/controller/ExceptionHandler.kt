package kr.weit.roadyfoody.common.controller

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.validation.ConstraintViolationException
import kr.weit.roadyfoody.common.exception.BaseException
import kr.weit.roadyfoody.common.exception.ErrorCode
import kr.weit.roadyfoody.common.exception.ErrorResponse
import kr.weit.roadyfoody.global.log.TraceManager
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException
import org.apache.tomcat.util.http.fileupload.impl.SizeException
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ExceptionHandler(
    private val traceManager: TraceManager,
) : ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        traceManager.doErrorLog(ex)
        return getInvalidRequestResponse(ex.messages().joinToString())
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val errorMessage =
            when (val cause = ex.cause) {
                is InvalidFormatException -> "${cause.path.joinToString(separator = ".") { it?.fieldName.orEmpty() }}: ${ex.message}"
                is MismatchedInputException -> {
                    "${cause.path.joinToString(separator = ".") { it?.fieldName.orEmpty() }}: ${ex.message}"
                }
                else -> "유효하지 않은 요청입니다"
            }
        traceManager.doErrorLog(ex)
        return getInvalidRequestResponse(errorMessage)
    }

    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        traceManager.doErrorLog(ex)
        return getInvalidRequestResponse(ex.message)
    }

    override fun handleMissingServletRequestPart(
        ex: MissingServletRequestPartException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        traceManager.doErrorLog(ex)
        return getInvalidRequestResponse(ex.message)
    }

    override fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        traceManager.doErrorLog(ex)
        return getInvalidRequestResponse(ex.message)
    }

    override fun handleMissingPathVariable(
        ex: MissingPathVariableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        traceManager.doErrorLog(ex)
        return getInvalidRequestResponse(ex.message)
    }

    override fun handleHttpMediaTypeNotSupported(
        ex: HttpMediaTypeNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        traceManager.doErrorLog(ex)
        return getInvalidRequestResponse(ex.message, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    override fun handleHandlerMethodValidationException(
        ex: HandlerMethodValidationException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? = getInvalidRequestResponse(ex.messages().joinToString())

    @ExceptionHandler(
        IllegalArgumentException::class,
        IllegalStateException::class,
        ConstraintViolationException::class,
    )
    fun invalidRequestException(ex: RuntimeException): ResponseEntity<Any>? {
        traceManager.doErrorLog(ex)
        return getInvalidRequestResponse(ex.message)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun noSuchElementException(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        traceManager.doErrorLog(ex)
        val noSuchElementErrorCode = ErrorCode.NO_SUCH_ELEMENT
        return ResponseEntity.status(noSuchElementErrorCode.httpStatus)
            .body(ErrorResponse.of(noSuchElementErrorCode, ex.message))
    }

    @ExceptionHandler(SizeLimitExceededException::class, FileSizeLimitExceededException::class)
    fun sizeLimitExceededException(ex: SizeException): ResponseEntity<ErrorResponse> {
        traceManager.doErrorLog(ex)
        val payloadTooLargeErrorCode = ErrorCode.PAYLOAD_TOO_LARGE
        return ResponseEntity.status(payloadTooLargeErrorCode.httpStatus)
            .body(ErrorResponse.of(payloadTooLargeErrorCode, ex.message))
    }

    @ExceptionHandler(BaseException::class)
    fun baseException(ex: BaseException): ResponseEntity<ErrorResponse> {
        traceManager.doErrorLog(ex)
        return ResponseEntity.status(ex.errorCode.httpStatus)
            .body(ErrorResponse.of(ex.errorCode, ex.message))
    }

    @ExceptionHandler(Exception::class)
    fun exception(ex: Exception): ResponseEntity<ErrorResponse> {
        traceManager.doErrorLog(ex)
        val internalServerErrorCode = ErrorCode.INTERNAL_SERVER_ERROR
        return ResponseEntity.status(internalServerErrorCode.httpStatus)
            .body(ErrorResponse.of(internalServerErrorCode, ex.message))
    }

    private fun MethodArgumentNotValidException.messages(): List<String> {
        return bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage.orEmpty()}" }
    }

    private fun HandlerMethodValidationException.messages(): List<String> =
        this.allErrors.map {
            it.defaultMessage.orEmpty()
        }

    private fun getInvalidRequestResponse(
        errorMessage: String?,
        httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
    ): ResponseEntity<Any> {
        val invalidRequestErrorCode = ErrorCode.INVALID_REQUEST
        return ResponseEntity.status(httpStatus)
            .body(ErrorResponse.of(invalidRequestErrorCode, errorMessage))
    }
}
