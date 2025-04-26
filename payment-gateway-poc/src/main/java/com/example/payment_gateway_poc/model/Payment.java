package com.example.payment_gateway_poc.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

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
    }

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private String paymentId;

    private String transactionId;

    private String orderId;

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

    private Boolean isRefunded;

    private String signature;

    private Integer statusCheckCounter;

    private String createdAt;

    private String updatedAt;

    public Payment() {
        this.transactionId = UUID.randomUUID().toString();
        this.orderId = "ORDER_" + UUID.randomUUID().toString().substring(0, 8);
        this.status = PaymentStatus.INITIATED;
        this.isRefunded = false;
        this.statusCheckCounter = 0;
        this.merchentOrderId = "M_ORDER_" + UUID.randomUUID().toString().substring(0, 10);
    }

    @PrePersist
    public void onCreate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime indiaTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        this.createdAt = indiaTime.format(formatter);
    }

    @PreUpdate
    public void onUpdate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime indiaTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        this.updatedAt = indiaTime.format(formatter);
    }
}
