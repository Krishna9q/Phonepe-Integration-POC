package com.example.payment_gateway_poc.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;

@Data
@Getter
@Setter
@Entity
public class Payment {

    public enum PaymentStatus {
        INITIATED,
        PENDING,
        SUCCESS,
        FAILED,
        // CANCELLED,
        // REFUNDED
    }
    
    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long paymentId;
    // Auto-generated internal transaction ID (UUID)
    private String transactionId;

    // Auto-generated order ID (e.g., ORDER_<UUID>)
    private String orderId;

    // Auto-generated merchantOrderId (e.g., M_ORDER_<UUID>)
    private String merchentOrderId;

    
    private PaymentStatus status;

    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String redirectUrl;

    private String phonepeTransactionId;

    private String paymentMethod;

    private Long amountInPaise;

    private String currency = "INR";

    private String phoneNumber;

    private String callbackURL;

    private String errorCode;

    private String errorMessage;

    private String signature;

    private final String createdAt;

    private String updatedAt;

    public Payment() {
        this.transactionId = UUID.randomUUID().toString();
        this.orderId = "ORDER_" + UUID.randomUUID().toString().substring(0, 8);
        this.status = PaymentStatus.INITIATED;
        this.merchentOrderId = "M_ORDER_" + UUID.randomUUID().toString().substring(0, 10);
        this.createdAt = LocalDateTime.now().toString();
        this.updatedAt = LocalDateTime.now().toString();
    }
}
