package com.jolly.paymentintegrationsystem.domain

/**
 * @author jolly
 */
interface BaseResponse {
    val payload: String?
    val respCode: String?
    val respDesc: String?
}

