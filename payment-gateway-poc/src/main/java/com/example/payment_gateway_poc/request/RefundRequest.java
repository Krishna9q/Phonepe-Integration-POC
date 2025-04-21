package com.example.payment_gateway_poc.request;

import lombok.Data;

@Data
public class RefundRequest {
    private String merchantRefundId;
    private String originalMerchantOrderId;
    private Long amount;
}
