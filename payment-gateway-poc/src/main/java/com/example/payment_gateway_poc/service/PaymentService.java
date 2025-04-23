package com.example.payment_gateway_poc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.payment_gateway_poc.repo.PaymentRepo;
import com.example.payment_gateway_poc.repo.RefundRepo;
import com.example.payment_gateway_poc.response.RefundPaymentResponse;

import java.util.List;
import com.example.payment_gateway_poc.model.Payment;
import com.example.payment_gateway_poc.model.Refund;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private RefundRepo refundRepo;

    public List<Payment> getAllPayments() {
        return paymentRepo.findAllByOrderByCreatedAtDesc();

    }

    

    public Payment getPaymentDetails(String merchentOrderId) {
        Payment payment = paymentRepo.findByMerchentOrderId(merchentOrderId);
        return payment;
    }

    public RefundPaymentResponse getPaymentWithRefundDetails(String paymentId) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        Refund refund = refundRepo.findByPaymentId(paymentId);

        RefundPaymentResponse refundPaymentResponse = new RefundPaymentResponse();
        refundPaymentResponse.setPayment(payment);
        refundPaymentResponse.setRefund(refund);

        System.out.println("Refund Payment Response: " + refundPaymentResponse);

        return refundPaymentResponse;
    }
}
