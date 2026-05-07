package com.estapar.parking.shared.error

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.Instant

data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: Instant = Instant.now(),
)

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ApplicationException::class)
    fun handleApplicationException(e: ApplicationException): ResponseEntity<ErrorResponse> {
        logger.warn("Application error [{}]: {}", e.errorCode, e.message)
        return ResponseEntity
            .status(e.errorCode.httpStatus)
            .body(ErrorResponse(code = e.errorCode.name, message = e.message ?: ""))
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLock(e: ObjectOptimisticLockingFailureException): ResponseEntity<ErrorResponse> {
        logger.warn("Optimistic lock conflict: {}", e.message)
        return ResponseEntity
            .status(ErrorCode.OPTIMISTIC_LOCK_CONFLICT.httpStatus)
            .body(
                ErrorResponse(
                    code = ErrorCode.OPTIMISTIC_LOCK_CONFLICT.name,
                    message = "Concurrent modification conflict — retry the request",
                ),
            )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(): ResponseEntity<Unit> = ResponseEntity.notFound().build()

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", e)
        return ResponseEntity
            .internalServerError()
            .body(ErrorResponse(code = "INTERNAL_ERROR", message = "An unexpected error occurred"))
    }
}
