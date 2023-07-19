package com.jolly.paymentintegrationsystem.extensions

import io.konform.validation.Invalid
import io.konform.validation.Validation
import java.lang.IllegalArgumentException

/**
 * @author jolly
 *
 */
fun <T> Validation<T>.validateAndThrowOnFailure(value: T) {
    val result = validate(value)
    if (result is Invalid<T>) {
        throw IllegalArgumentException(result.errors.toString())
    }
}
