package com.jolly.paymentintegrationsystem

/**
 * @author jolly
 */
interface BaseResponse {
    val payload: String?
    val respCode: String
    val respDesc: String
}

