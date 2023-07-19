package com.jolly.paymentintegrationsystem.extensions

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.MissingClaimException
import com.auth0.jwt.interfaces.Claim
import com.jolly.paymentintegrationsystem.BaseResponse
import org.json.simple.JSONObject
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpOutputMessage
import org.springframework.web.client.RestClientException
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.util.function.Consumer

/**
 * @author jolly
 */
fun <T> T.createToken(secretKey: String): String {
    try {
        val algo = Algorithm.HMAC256(secretKey)
        val tokenBuilder = JWT.create()
        requireNotNull(this)
        this.let {
            it::class.java.declaredFields.forEach { field ->
                field.isAccessible = true
                val value = field.get(this)
                tokenBuilder.addToTokenBuilder(field.name, value)
            }
        }
        return tokenBuilder.sign(algo)
    } catch (up: Exception) {
        when (up) {
            is IllegalArgumentException, is IllegalAccessException, is JWTCreationException -> {
                //TODO: throw custom exception
                throw up
            }
            else -> throw up
        }
    }
}

fun JWTCreator.Builder.addToTokenBuilder(fieldName: String, value: Any?) {
    when (value) {
        is String -> this.withClaim(fieldName, value)
        is BigDecimal -> this.withClaim(fieldName, value.toDouble())
        is Boolean -> {
            if (fieldName == "request3DS") {
                this.withClaim(fieldName, if (value) "Y" else "N")
            } else {
                this.withClaim(fieldName, value.toString())
            }
        }
    }
}

fun Map<String, Claim>.getClaimAsString(claimKey: String): String {
    return getClaimAs(claimKey) { it.asString() }
}

fun Map<String, Claim>.getClaimAsCharacter(claimKey: String): Char {
    return getClaimAs(claimKey) { it.`as`(Char::class.java) }
}

fun Map<String, Claim>.getClaimAsLong(claimKey: String): Long {
    return getClaimAs(claimKey) { it.asLong() }
}

fun Map<String, Claim>.getClaimAsInt(claimKey: String): Int {
    return getClaimAs(claimKey) { it.asInt() }
}

private fun <T> Map<String, Claim>.getClaimAs(claimKey: String, claimMapper: (Claim) -> T): T {
    return this[claimKey]?.let { claimMapper(it) } ?: throw MissingClaimException("Missing claim: $claimKey")
}

suspend inline fun <reified R: BaseResponse> WebClient.exchangeToken(request: Any, secretKey: String, url: String, responseType: Class<R>): R {
    val token = request.createToken(secretKey)
    val webClient = this.mutate().defaultHeaders(createHeaders()).build()
    val payloadJson = getPayloadJson(token)

    return webClient.post()
        .uri(url)
        .body(Mono.just(payloadJson), String::class.java)
        .awaitExchange { responseEntity ->
            val responseBody = responseEntity.awaitBody<R>()
            if (responseEntity.statusCode().isError) {
                throw RestClientException("JWT request failed with status code: ${responseEntity.statusCode()}")
            } else if (responseBody.payload.isNullOrEmpty()) {
                throw RestClientException("Payload received is null or empty, respCode: ${responseBody.respCode}, respDesc: ${responseBody.respDesc}")
            } else {
                verifyPayload(responseBody.payload!!, secretKey)
                responseBody
            }
        }
}

fun verifyPayload(payload: String, secretKey: String) {
    try {
        val algo = Algorithm.HMAC256(secretKey)
        val verifier: JWTVerifier = JWT.require(algo).build()
        verifier.verify(payload)
    } catch (e: JWTVerificationException) {
        throw JWTVerificationException("Failed to verify token: [${payload}]", e)
    }
}

fun getPayloadJson(token: String): String {
    val requestData = JSONObject()
    requestData["payload"] = token
    return requestData.toString()
}

fun createHeaders(): Consumer<HttpHeaders> = Consumer { headers ->
    headers.contentType = MediaType.APPLICATION_JSON
    headers.accept = listOf(MediaType.TEXT_PLAIN)
}
