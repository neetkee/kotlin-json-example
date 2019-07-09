package com.neetkee.example

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
class ItemController {

    @PostMapping("/items")
    fun addItem(@RequestBody @Valid item: Item) {
        println("Item added: $item")
    }
}

@RestControllerAdvice
class ItemControllerAdvice {

    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handleMethodArgumentNotValid(exception: MethodArgumentNotValidException): ValidationError {
        val violations = exception.bindingResult.allErrors
                .mapNotNull { error ->
                    when (error) {
                        is FieldError -> Violation(error.field, error.defaultMessage ?: "")
                        is ObjectError -> Violation(error.objectName, error.defaultMessage ?: "")
                        else -> null
                    }
                }
                .toList()
        return ValidationError(violations)
    }

    @ExceptionHandler(value = [MissingKotlinParameterException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingKotlinParameter(exception: MissingKotlinParameterException): ValidationError {
        val fieldName = exception.path.joinToString(separator = ".") { it.fieldName }
        val violation = Violation(fieldName, "must not be null")
        return ValidationError(listOf(violation))
    }
}