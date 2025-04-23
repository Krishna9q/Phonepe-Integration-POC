package com.example.payment_gateway_poc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.print.DocFlavor.READER;
import com.example.payment_gateway_poc.model.Payment;
import com.example.payment_gateway_poc.model.Refund;
import com.example.payment_gateway_poc.model.Payment.PaymentStatus;
import com.example.payment_gateway_poc.repo.PaymentRepo;
import com.example.payment_gateway_poc.repo.RefundRepo;
import com.example.payment_gateway_poc.request.PaymentRequest;
import com.example.payment_gateway_poc.response.PaymentStatusCheckResponse;
import com.example.payment_gateway_poc.response.PhonePeStatusResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class InitiatePaymentService {

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private RefundRepo refundRepo;

    private static final Logger logger = LoggerFactory.getLogger(InitiatePaymentService.class);

    @Value("${spring.payments.phonepe.base-url}")
    private String BASE_URL;

    @Value("${spring.payments.phonepe.saltKey}")
    private String saltKey;

    @Value("${spring.payments.phonepe.saltIndex}")
    private String saltIndex;

    private String token;

    RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "*/30 * * * * *")
    public void fetchPhonePeToken() {
        String url = "https://api-preprod.phonepe.com/apis/pg-sandbox/v1/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", "TEST-M228Z7F565MHQ_25041");
        formData.add("client_version", "1");
        formData.add("client_secret", "Y2Q3ZmNiZmItZTBlNy00MjY3LWE3NmMtOGIwZDAyMjk0MjMw");
        formData.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            String responseBody = response.getBody();
            if (responseBody != null) {
                // Assuming the response is in JSON format
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

                String accessToken = objectMapper.readTree(responseBody).path("access_token").asText();
                token = accessToken;
                System.out.println("PhonePe Token Set: " + accessToken);
            } else {
                System.out.println("Response body is null");
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch token: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Payment initiatePayment(PaymentRequest request) throws Exception {
        logger.info("Initiating payment for mobile number: {}", request.getMobileNumber());
        Payment payment = new Payment();
        payment.setAmountInPaise(request.getAmount() * 100);
        payment.setPhoneNumber(request.getMobileNumber());

        String url = BASE_URL + "/v2/pay";
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
                "https://475f-2409-40c4-2e-a684-b833-9954-b4d7-c70d.ngrok-free.app/payment/result?merchentOrderId="
                        + payment.getMerchentOrderId());
        merchantUrls.put("callbackUrl",
                "https://4b39-2409-40c4-270-9477-1f9f-a521-ae3d-933.ngrok-free.app/phonepe/callback");
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

        payment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepo.save(payment);

        logger.info("Payment object created: {}", savedPayment);
        return savedPayment;
    }

    public PaymentStatusCheckResponse checkPaymentStatus(String merchantOrderId) throws RuntimeException {
        logger.info("Checking payment status for merchantOrderId: {}", merchantOrderId);
        PaymentStatusCheckResponse paymentStatusCheckResponse = new PaymentStatusCheckResponse();
        Payment payment = paymentRepo.findByMerchentOrderId(merchantOrderId);
        if (payment == null) {
            logger.error("No payment found for merchantOrderId: {}", merchantOrderId);
            paymentStatusCheckResponse.setStatus("FAILED");
            paymentStatusCheckResponse.setReason("payment not found");
            return paymentStatusCheckResponse;
        }

        if (payment.getStatusCheckCounter() >= 20 && payment.getStatus().equals(PaymentStatus.PENDING)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(payment);
            logger.warn("Max status check attempts reached for merchantOrderId: {}", merchantOrderId);
            paymentStatusCheckResponse.setStatus("FAILED");
            paymentStatusCheckResponse.setReason("TIMEOUT");
            return paymentStatusCheckResponse;
        }

        // Increment status check counter before processing
        payment.setStatusCheckCounter(payment.getStatusCheckCounter() + 1);

        String url = BASE_URL + "/v2/order/" + merchantOrderId + "/status";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "O-Bearer " + token);

        logger.debug("Sending request to: {}", url);
        logger.debug("Request headers: {}", headers);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();
            logger.info("Received response: {}", responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            PhonePeStatusResponse statusResponse = objectMapper.readValue(responseBody, PhonePeStatusResponse.class);

            String state = statusResponse.getState();
            PhonePeStatusResponse.PaymentDetail detail = statusResponse.getPaymentDetails().get(0);

            if ("COMPLETED".equalsIgnoreCase(state)) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPhonepeTransactionId(detail.getTransactionId());
                payment.setPaymentMethod(detail.getPaymentMode());
                paymentStatusCheckResponse.setStatus(state);
                paymentStatusCheckResponse.setReason("PAYMENT SUCCESSFULLY");
                paymentRepo.save(payment);
            } else if ("FAILED".equalsIgnoreCase(state)) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorCode(detail.getErrorCode());
                payment.setErrorMessage(detail.getDetailedErrorCode());
                paymentStatusCheckResponse.setStatus(state);
                paymentStatusCheckResponse.setReason(detail.getDetailedErrorCode());
                paymentRepo.save(payment);
            } else {
                // payment.setStatus(PaymentStatus.PENDING);
                payment.setUpdatedAt(LocalDateTime.now().toString());
                paymentRepo.save(payment);
                paymentStatusCheckResponse.setStatus(state);
                paymentStatusCheckResponse.setReason("PENDING");
            }

        } catch (Exception e) {
            logger.error("Exception while checking payment status for order {}: {}", merchantOrderId, e.getMessage(),
                    e);
            // Save even if parsing fails (counter is updated)
        }
        return paymentStatusCheckResponse;

    }

    @SuppressWarnings({ "unused", "unchecked" })
    public String initiateRefund(String originalMerchantOrderId) {
        Payment payment = paymentRepo.findByMerchentOrderId(originalMerchantOrderId);

        if (payment == null) {
             
                throw new RuntimeException("Payment Not Found with Merchant Order ID: " + originalMerchantOrderId);
            }
        if(payment.getIsRefunded()){throw new RuntimeException("Refund Intitated Already");}    

            String API_URL = "https://api-preprod.phonepe.com/apis/pg-sandbox/payments/v2/refund";

            Refund refund = new Refund();
            refund.setMerchantRefundId("Refund-" + UUID.randomUUID());
            refund.setAmountInPaise(payment.getAmountInPaise());
            refund.setCurrency(payment.getCurrency());
            refund.setOriginalMerchantOrderId(originalMerchantOrderId);
            refund.setPaymentId(payment.getPaymentId());
            refund.setInitiatedBy("ADMIN");

            // Create request payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchantRefundId", refund.getMerchantRefundId());
            requestBody.put("originalMerchantOrderId", originalMerchantOrderId);
            requestBody.put("amount", refund.getAmountInPaise());

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "O-Bearer " + token);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);

                String refundId = String.valueOf(responseMap.get("refundId"));
                refund.setPhonePeRefundId(refundId);
                refundRepo.save(refund);
                
                logger.info("Thread is going to sleep for 5 seconds to wait for refund status update.");
                Thread.sleep(100);
                String checkStatusResponse = refundStatusCheck(refund.getMerchantRefundId());

                
                refund.setStatus(checkStatusResponse);
                payment.setIsRefunded(true);
                refundRepo.save(refund);

                System.out.println("Refund API Response: " + response);

                return checkStatusResponse;

            } catch (Exception e) {
                e.printStackTrace();
                return "Error While Initiating Refund: " + e.getMessage();
            }
        

    }

    public String refundStatusCheck(String merchantRefundId) {
        Refund refund = refundRepo.findByMerchantRefundId(merchantRefundId);
        String API_URL = "https://api-preprod.phonepe.com/apis/pg-sandbox/payments/v2/refund/" + merchantRefundId
                + "/status";

        if (refund == null) {
            throw new RuntimeException("NO REFUND FOUND");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "O-Bearer " + token); // Make sure 'token' is correct

            HttpEntity<String> entity = new HttpEntity<>(headers); // attach headers here

            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.GET,
                    entity,
                    String.class);
            // Parse response body
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
            // refund.setGatewayResponseJson(response.toString());
            String state = String.valueOf(responseMap.get("state"));
            refund.setStatus(state);
            // refund.setPhonePeRefundId(responseMap.get("refundId").toString());
            refundRepo.save(refund);
            return state;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
