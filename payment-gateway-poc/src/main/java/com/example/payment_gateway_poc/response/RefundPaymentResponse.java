package com.example.payment_gateway_poc.response;

import com.example.payment_gateway_poc.model.Payment;
import com.example.payment_gateway_poc.model.Refund;

import lombok.Data;

@Data
public class RefundPaymentResponse {
    private Payment payment;

    private Refund refund;
}
