package com.jolly.paymentintegrationsystem.payment

/**
 * @author jolly
 */
data class PaymentData(
    val name: String? = null,
    val email: String? = null,
    val mobileNo: String? = null,
    val accountNo: String? = null,
    val securePayToken: String? = null,
    val cardNo: String? = null,
    val expiryMonth: String? = null,
    val expiryYear: String? = null
)
