package com.example.payment_gateway_poc.response;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PaymentStatusCheckResponse {
    private String status;
    private String reason;
    
}
