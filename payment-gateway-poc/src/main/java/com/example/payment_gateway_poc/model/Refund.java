package com.example.payment_gateway_poc.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private  String id;
    
    private String merchantRefundId; // Unique refund ID you generate (REF123)

    private String originalMerchantOrderId; // TXN ID of the original payment
    
    private String paymentId; // Link to your Payment entity (if needed)`

    private Long amountInPaise; // Amount refunded, in paise (e.g., 10000 for ₹100)

    private String phonePeRefundId ;

    private String status; // PENDING, SUCCESS, FAILED

    private String reason; // Optional reason for refund

    private String errorCode; // if refund failed
    private String errorMessage;

    private String currency; // e.g., INR

    private String initiatedBy; // userId/adminId


    private String initiatedAt;

    private String updatedAt;

    private String gatewayResponseJson; // Save entire response (optional for debugging)

    // Optional: webhook received flag
    private Boolean webhookReceived = false;
    public Refund() {
        
    }

       @PrePersist
    public void onCreate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.initiatedAt = String.valueOf(LocalDateTime.now().format(formatter));
    }

    @PreUpdate
    public void onUpdate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.updatedAt = String.valueOf(LocalDateTime.now().format(formatter));
    }
}
