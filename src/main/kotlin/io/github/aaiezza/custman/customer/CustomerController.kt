package io.github.aaiezza.custman.customer

import io.github.aaiezza.custman.customer.data.CreateCustomerStatement
import io.github.aaiezza.custman.customer.models.CreateCustomerRequest
import io.github.aaiezza.klogging.error
import io.github.aaiezza.klogging.info
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/customer")
class CustomerController(
    @Autowired private val createCustomerStatement: CreateCustomerStatement,
) {
    @PostMapping
    fun createCustomer(@RequestBody request: CreateCustomerRequest): ResponseEntity<*> =
        runCatching { createCustomerStatement.execute(request) }
            .map { newCustomer ->
                CustomerCreatedLogEvent(newCustomer).info()
                val location = URI.create("/customer/${newCustomer.customerId.uuid}")
                ResponseEntity.created(location).body(newCustomer)
            }
            .recover {
                when (it) {
                    is CustomerAlreadyExistsWithGivenEmailException -> run {
                        CreateCustomerExceptionLogEvent(it, HttpMethod.POST, "/customer").error()
                        ResponseEntity.status(CONFLICT)
                            .errorMessageBody(it)
                    }

                    else -> throw it
                }
            }.getOrThrow()
}

@RestControllerAdvice
class GlobalExceptionHandler {
    // Handle generic unhandled exceptions
    @ExceptionHandler(Exception::class)
    fun handleGenericException(exception: Exception, request: HttpServletRequest): ResponseEntity<*> {
        UnhandledExceptionLogEvent(exception, request.method.let(HttpMethod::valueOf), request.contextPath).error()
        return ResponseEntity
            .status(INTERNAL_SERVER_ERROR)
            .errorMessageBody(exception)
    }

    // Handle bad user input - JSON format issues
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseError(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        MalformedInputExceptionLogEvent(exception, request.method.let(HttpMethod::valueOf), request.contextPath).error()
        return ResponseEntity
            .status(BAD_REQUEST)
            .errorMessageBody(exception)
    }
}

fun ResponseEntity.BodyBuilder.errorMessageBody(ex: Exception): ResponseEntity<*> =
    body(mapOf("error_message" to "${ex.message}"))
