package com.example.payment_gateway_poc.response;




import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhonePeStatusResponse {
    private String orderId;
    private String state;
    private Long amount;
    private List<PaymentDetail> paymentDetails;

    @Data
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentDetail {
        private String paymentMode;
        private String transactionId;
        private String state;
        private String errorCode;
        private String detailedErrorCode;
        private Long timestamp;
        private Long amount;
    }
}