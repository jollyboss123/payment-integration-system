package com.jolly.paymentintegrationsystem.payment

/**
 * @author jolly
 */
data class PaymentData(
    val name: String,
    val email: String,
    val mobileNo: String,
    val accountNo: String,
    val securePayToken: String,
    val cardNo: String,
    val expiryMonth: String,
    val expiryYear: String
)
