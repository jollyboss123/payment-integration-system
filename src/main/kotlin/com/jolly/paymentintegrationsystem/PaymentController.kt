package com.jolly.paymentintegrationsystem

import com.jolly.paymentintegrationsystem.domain.PaymentRequest
import com.jolly.paymentintegrationsystem.domain.PaymentResponse
import com.jolly.paymentintegrationsystem.inquiry.PaymentInquiryService
import com.jolly.paymentintegrationsystem.inquiry.domain.PaymentInquiryRequest
import com.jolly.paymentintegrationsystem.inquiry.domain.PaymentInquiryResponse
import com.jolly.paymentintegrationsystem.payment.PaymentService
import com.jolly.paymentintegrationsystem.payment.domain.*
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import kotlinx.coroutines.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

/**
 * @author jolly
 */
@RestController
@RequestMapping("/api/jolly/v1/payment/integration")
class PaymentController(
    private val paymentClient: PaymentClient,
    private val paymentService: PaymentService,
    private val paymentInquiryService: PaymentInquiryService
) {
    companion object {
        const val PAYMENT_CIRCUIT_BREAKER_NAME = "payment"
    }

    @CircuitBreaker(name = PAYMENT_CIRCUIT_BREAKER_NAME)
    @PostMapping("/2c2p")
    suspend fun non3DSPayment(@RequestBody paymentRequest: PaymentRequest): PaymentResponse {
        val request = paymentRequest.validate()
        return paymentClient.doNon3DSPayment(request)
    }

//    @CircuitBreaker(name = PAYMENT_CIRCUIT_BREAKER_NAME)
    @PostMapping("/2c2p/bulk")
    suspend fun bulkNon3DSPayment(@RequestBody requests: PaymentRequest): PaymentRequest {
//        val paymentRequests = requests.map { it.validate() }
        val paymentRequests = requests.validate()
        paymentClient.doBulkNon3DSPayment(paymentRequests)
        return paymentRequests
    }

    @CircuitBreaker(name = PAYMENT_CIRCUIT_BREAKER_NAME)
    @PostMapping("/2c2p/new")
    suspend fun doNon3DSPayment(@RequestBody paymentRequest: PaymentRequest): PaymentResponse {
        try {
            val request = paymentRequest.validate()

            return supervisorScope {
                try {
                    val dataModel = getDataModel(request, this)
                    getResponse(dataModel)
                } finally {
                    this.coroutineContext.cancelChildren()
                }
            }
        } catch (e: Throwable) {
            throw getErrorStatus(e)
        }
    }

    suspend fun getDataModel(request: PaymentRequest, coroutineScope: CoroutineScope): Non3DSPaymentDataModel {
        return coroutineScope.run {
            val paymentToken = async(start = CoroutineStart.LAZY) {
                Result.runCatching {
                    paymentService.generatePaymentToken(PaymentTokenRequest(
                        request.merchantID,
                        request.invoiceNo,
                        request.description,
                        request.amount,
                        request.currencyCode,
                        request.paymentChannel,
                        request.request3DS
                    ))
                }
            }

            val payment = async(start = CoroutineStart.LAZY) {
                paymentToken.awaitResult().mapCatching {
                    if (it.paymentToken.isNullOrBlank()) {
                        throw IllegalStateException("Payment token does not have value")
                    }

                    paymentService.doPayment(PaymentRequestParams(
                        it.paymentToken,
                        PaymentParams(
                            PaymentCode(
                                "CC"
                            ),
                            PaymentData(
                                cardNo = request.cardNo,
                                expiryMonth = request.expiryMonth,
                                expiryYear = request.expiryYear
                            )
                        ),
                        "en"
                    ))
                }
            }

            val paymentInquiry = async(start = CoroutineStart.LAZY) {
                payment.awaitResult().mapCatching {
                    paymentInquiryService.doPaymentInquiry(PaymentInquiryRequest(
                        merchantID = request.merchantID,
                        invoiceNo = it.invoiceNo.takeIf { !it.isNullOrBlank() } ?: throw
                        IllegalStateException("Invoice no is mandatory"),
                        locale = "en"
                    ))
                }
            }

            Non3DSPaymentDataModel(paymentToken, payment, paymentInquiry)
        }
    }

    suspend fun getResponse(dataModel: Non3DSPaymentDataModel): PaymentResponse {
        return dataModel.paymentInquiry.awaitResult().getOrThrow().let { paymentInquiry ->
            PaymentResponse(
                merchantID = paymentInquiry.merchantID,
                invoiceNo = paymentInquiry.invoiceNo,
                amount = paymentInquiry.amount,
                currencyCode = paymentInquiry.currencyCode,
                transactionDateTime = paymentInquiry.transactionDateTime,
                agentCode = paymentInquiry.agentCode,
                channelCode = paymentInquiry.channelCode,
                referenceNo = paymentInquiry.referenceNo,
                cardNo = paymentInquiry.cardNo,
                issuerCountry = paymentInquiry.issuerCountry,
                issuerBank = paymentInquiry.issuerBank,
                eci = paymentInquiry.eci,
                paymentScheme = paymentInquiry.paymentScheme,
                respCode = paymentInquiry.respCode,
                respDesc = paymentInquiry.respDesc
            )
        }
    }

    fun getErrorStatus(e: Throwable): ResponseStatusException {
        return when(e) {
            is IllegalArgumentException -> {
                ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom error description", e)
            }
            else -> {
                ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
            }
        }
    }
}

suspend fun <T> Deferred<Result<T>>.awaitResult(): Result<T> {
    return try {
        this.await()
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

data class Non3DSPaymentDataModel(
    val paymentToken: Deferred<Result<PaymentTokenResponse>>,
    val payment: Deferred<Result<PaymentResponseParams>>,
    val paymentInquiry: Deferred<Result<PaymentInquiryResponse>>
)

