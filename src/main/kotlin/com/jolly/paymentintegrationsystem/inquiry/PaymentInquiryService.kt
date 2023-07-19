package com.jolly.paymentintegrationsystem.inquiry

import com.jolly.paymentintegrationsystem.inquiry.domain.PaymentInquiryRequest
import com.jolly.paymentintegrationsystem.inquiry.domain.PaymentInquiryResponse

/**
 * @author jolly
 */
interface PaymentInquiryService {
    suspend fun doPaymentInquiry(request: PaymentInquiryRequest): PaymentInquiryResponse
}
