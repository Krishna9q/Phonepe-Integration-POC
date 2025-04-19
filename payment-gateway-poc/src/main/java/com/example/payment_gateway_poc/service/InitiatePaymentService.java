package com.example.payment_gateway_poc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.example.payment_gateway_poc.model.Payment;
import com.example.payment_gateway_poc.model.Payment.PaymentStatus;
import com.example.payment_gateway_poc.repo.PaymentRepo;
import com.example.payment_gateway_poc.request.PaymentRequest;
import com.example.payment_gateway_poc.response.PhonePeStatusResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class InitiatePaymentService {

    @Autowired
    private PaymentRepo paymentRepo;

    private static final Logger logger = LoggerFactory.getLogger(InitiatePaymentService.class);

    @Value("${spring.payments.phonepe.base-url}")
    private String baseUrl;

    @Value("${spring.payments.phonepe.saltKey}")
    private String saltKey;

    @Value("${spring.payments.phonepe.saltIndex}")
    private String saltIndex;

    @Value("${spring.payments.phonepe.auth-token}")
    private String token;

    RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public Payment initiatePayment(PaymentRequest request) throws Exception {
        logger.info("Initiating payment for mobile number: {}", request.getMobileNumber());
        Payment payment = new Payment();
        payment.setAmountInPaise(request.getAmount() * 100);
        payment.setPhoneNumber(request.getMobileNumber());

        String url = baseUrl + "/v2/pay";
        logger.debug("Payment URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "O-Bearer " + token);
        logger.debug("Request headers: {}", headers);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("merchantOrderId", payment.getMerchentOrderId());
        requestBody.put("orderId", payment.getOrderId());
        requestBody.put("amount", payment.getAmountInPaise());
        requestBody.put("transactionId", payment.getTransactionId());
        requestBody.put("phoneNo", payment.getPhoneNumber());
        logger.debug("Request body (initial): {}", requestBody);

        Map<String, Object> paymentFlow = new HashMap<>();
        paymentFlow.put("type", "PG_CHECKOUT");
        paymentFlow.put("message", "Payment message used for collect requests");

        Map<String, String> merchantUrls = new HashMap<>();
        merchantUrls.put("redirectUrl",
                "https://32eb-2409-40c4-f-fe5c-e1d2-a389-8745-19ce.ngrok-free.app/payment/result?merchentOrderId="
                        + payment.getMerchentOrderId());
        merchantUrls.put("callbackUrl",
                "https://32eb-2409-40c4-f-fe5c-e1d2-a389-8745-19ce.ngrok-free.app/phonepe/callback");
        paymentFlow.put("merchantUrls", merchantUrls);

        requestBody.put("paymentFlow", paymentFlow);
        logger.debug("Request body (final): {}", requestBody);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        logger.debug("HTTP Entity: {}", entity);

        logger.info("Sending payment initiation request...");
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        logger.info("Response received from payment gateway: {}", response.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);

        payment.setRedirectUrl(String.valueOf(responseMap.get("redirectUrl")));
        String state = String.valueOf(responseMap.get("state"));
        logger.info("Payment state received: {}", state);

        if ("SUCCESS".equalsIgnoreCase(state)) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else if ("FAILED".equalsIgnoreCase(state)) {
            payment.setStatus(PaymentStatus.FAILED);
        } else {
            payment.setStatus(PaymentStatus.PENDING);
        }

        Payment savedPayment = paymentRepo.save(payment);

        logger.info("Payment object created: {}", savedPayment);
        return savedPayment;
    }

    public String checkPaymentStatus(String merchantOrderId) {
        logger.info("Checking payment status for merchantOrderId: {}", merchantOrderId);

        Payment payment = paymentRepo.findByMerchentOrderId(merchantOrderId);
        if (payment == null) {
            logger.error("No payment found for merchantOrderId: {}", merchantOrderId);
            return "No such order";
        }

        String url = baseUrl + "/v2/order/" + merchantOrderId + "/status";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.set("Authorization", "O-Bearer " + token);
        logger.debug("Request headers for status check: {}", headers);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        logger.info("Sending payment status check request...");
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();
        logger.info("Response: {}", responseBody);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PhonePeStatusResponse statusResponse = objectMapper.readValue(responseBody, PhonePeStatusResponse.class);
            String state = statusResponse.getState();

            if ("COMPLETED".equalsIgnoreCase(state)) {
                PhonePeStatusResponse.PaymentDetail detail = statusResponse.getPaymentDetails().get(0);
                payment.setStatus(PaymentStatus.SUCCESS); // use enum
                payment.setPhonepeTransactionId(detail.getTransactionId());
                payment.setPaymentMethod(detail.getPaymentMode());
                payment.setUpdatedAt(LocalDateTime.now().toString());
            } else if ("FAILED".equalsIgnoreCase(state)) {
                PhonePeStatusResponse.PaymentDetail detail = statusResponse.getPaymentDetails().get(0);
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorCode(detail.getErrorCode());
                payment.setErrorMessage(detail.getDetailedErrorCode()); // Change if more info is available
                payment.setUpdatedAt(LocalDateTime.now().toString());
            }

            paymentRepo.save(payment);
        } catch (Exception e) {
            logger.error("Error while parsing or updating status", e);
        }
        return response.getBody();
    }
}
