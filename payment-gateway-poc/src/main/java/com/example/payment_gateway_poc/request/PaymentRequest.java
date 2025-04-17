package com.example.payment_gateway_poc.request;

import lombok.Data;

@Data
public class PaymentRequest {

    private Long amount;
    private String mobileNumber;
    
}
