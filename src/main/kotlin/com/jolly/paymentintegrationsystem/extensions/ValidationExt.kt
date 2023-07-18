package com.jolly.paymentintegrationsystem.extensions

import io.konform.validation.Invalid
import io.konform.validation.Validation
import java.lang.IllegalArgumentException

/**
 * @author jolly
 *
 * <a href:"https://medium.com/nerd-for-tech/object-validation-in-kotlin-c7e02b5dabc"/>
 */
fun <T> Validation<T>.validateAndThrowOnFailure(value: T) {
    val result = validate(value)
    if (result is Invalid<T>) {
        throw IllegalArgumentException(result.errors.toString())
    }
}
