package com.example.payment_gateway_poc.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.payment_gateway_poc.model.Refund;

@Repository
public interface RefundRepo extends JpaRepository<Refund, String > {
    Refund findByPaymentId(String paymentId);

    Refund findByMerchantRefundId(String merchantRefundId);
}
