package com.example.payment_gateway_poc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.payment_gateway_poc.model.Payment;
import com.example.payment_gateway_poc.request.PaymentRequest;
import com.example.payment_gateway_poc.service.InitiatePaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Map;

@RestController
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private InitiatePaymentService initiatePaymentService;

    @PostMapping("/phonepe/callback")
    public ResponseEntity<String> handlePhonePeCallback(@RequestBody Map<String, Object> payload,
            HttpServletRequest request) throws IOException {
        logger.info("Received PhonePe callback with payload: {}", payload);

        // Additional processing can be logged here if needed

        logger.info("Callback processing completed successfully.");
        return ResponseEntity.ok("Callback received");
    }

    @PostMapping("/pay")
    public ResponseEntity<?> initiate(@RequestBody PaymentRequest paymentRequest) {
        logger.info("Received payment initiation request: {}", paymentRequest);

        Payment responseMap;
        try {
            responseMap = initiatePaymentService.initiatePayment(paymentRequest);
            logger.info("Payment initiation successful with response: {}", responseMap);
        } catch (Exception e) {

            logger.error("Error during payment initiation: {}", e.getMessage(), e);
            System.out.println(e);
            return new ResponseEntity<>("Error during payment initiation: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);

        }

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/check-payment-status")
    public ResponseEntity<String> checkPaymentStatus(@RequestParam String merchentOrderId) {
        logger.info("Checking payment status for merchantOrderId: {}", merchentOrderId);
        try {
            String response = initiatePaymentService.checkPaymentStatus(merchentOrderId);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            Object amount = responseMap.get("amount");
            if (amount instanceof Number) {
                amount = ((Number) amount).doubleValue() / 100;
            }

            logger.info("Payment amount: {}", amount);

            logger.info("Payment status retrieved successfully: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while checking payment status: {}", e.getMessage(), e);
            return new ResponseEntity<>("Error while checking payment status", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
