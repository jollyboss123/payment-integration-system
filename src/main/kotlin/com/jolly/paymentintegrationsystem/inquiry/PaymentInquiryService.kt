package com.jolly.paymentintegrationsystem.inquiry

/**
 * @author jolly
 */
interface PaymentInquiryService {
    suspend fun doPaymentInquiry(request: PaymentInquiryRequest): PaymentInquiryResponse
}
