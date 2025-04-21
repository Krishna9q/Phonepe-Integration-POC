package com.example.payment_gateway_poc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.payment_gateway_poc.model.Payment;
import com.example.payment_gateway_poc.repo.RefundRepo;
import com.example.payment_gateway_poc.request.PaymentRequest;
import com.example.payment_gateway_poc.response.PaymentStatusCheckResponse;
import com.example.payment_gateway_poc.service.InitiatePaymentService;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Map;

@RestController
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private InitiatePaymentService initiatePaymentService;

    // private RefundServ refundRepo;

    @PostMapping("/phonepe/callback")
    public ResponseEntity<String> handlePhonePeCallback(@RequestBody Map<String, Object> payload,
            HttpServletRequest request) throws IOException {
        logger.info("Received PhonePe callback with payload: {}", payload);

        // Process callback logic here if needed

        logger.info("PhonePe callback processed successfully.");
        return ResponseEntity.ok("Callback received");
    }

    @PostMapping("/pay")
    public ResponseEntity<?> initiate(@RequestBody PaymentRequest paymentRequest) {
        logger.info("Initiating payment with request: {}", paymentRequest);

        try {
            Payment response = initiatePaymentService.initiatePayment(paymentRequest);
            logger.info("Payment initiated successfully: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Payment initiation failed: {}", e.getMessage(), e);
            return new ResponseEntity<>("Payment initiation failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check-payment-status")
    public ResponseEntity<PaymentStatusCheckResponse> checkPaymentStatus(@RequestParam String merchentOrderId) {
        logger.info("Checking payment status for merchantOrderId: {}", merchentOrderId);
        try {
            PaymentStatusCheckResponse response = initiatePaymentService.checkPaymentStatus(merchentOrderId);
            logger.info("Payment status retrieved: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to retrieve payment status: {}", e.getMessage(), e);
            return new ResponseEntity<>(new PaymentStatusCheckResponse(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/process-refund")
    public ResponseEntity<?> processRefund(@RequestParam String originalMerchantOrderId) {
        logger.info("Processing refund for merchantOrderId: {}", originalMerchantOrderId);
        try {
            String refundResponse = initiatePaymentService.initiateRefund(originalMerchantOrderId);
            logger.info("Refund processed successfully: {}", refundResponse);
            return new ResponseEntity<>(refundResponse, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Refund processing failed: {}", e.getMessage(), e);
            return new ResponseEntity<>("Refund processing failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("refund/status/check/{merchantRefundId}")
    public ResponseEntity<?> refunStatusCheck(@PathVariable String merchantRefundId) throws Exception {

        String response = this.initiatePaymentService.refundStatusCheck(merchantRefundId);
        System.out.println(response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
