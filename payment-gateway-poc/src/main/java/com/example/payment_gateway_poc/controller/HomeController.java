package com.example.payment_gateway_poc.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.payment_gateway_poc.model.Payment;
import com.example.payment_gateway_poc.model.Payment.PaymentStatus;
import com.example.payment_gateway_poc.repo.PaymentRepo;
import com.example.payment_gateway_poc.response.RefundPaymentResponse;
import com.example.payment_gateway_poc.service.PaymentService;

@Controller
public class HomeController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepo paymentRepo;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String viewPayments(Model model) {

       
        List<Payment> payments = paymentService.getAllPayments();

        // System.out.println("In HomeContoller All Payments : " + payments);
        
        model.addAttribute("payments", payments);
        return "dashboard"; // Thymeleaf template name
    }

    @GetMapping("/payment/result")
    public String paymentResult(@RequestParam String merchentOrderId, Model model) {
        model.addAttribute("merchentOrderId", merchentOrderId);
        return "payment_result"; // Must be inside `templates/`
    }

    @GetMapping("/refund")
    public String showRefundPage(
        @RequestParam("paymentId") String paymentId,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));
            
            if (!payment.getStatus().equals(PaymentStatus.SUCCESS)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Only successful payments can be refunded. Current status: " + payment.getStatus());
                return "redirect:/dashboard";
            }
            
            if (payment.getIsRefunded()) {
                redirectAttributes.addFlashAttribute("error", 
                    "This payment has already been refunded");
                return "redirect:/refund-details?paymentId="+ paymentId;
            }
            
            model.addAttribute("payment", payment);
            return "refund-page";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "An error occurred while processing your request");
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/refund-details")
    public String getRefundDetails(@RequestParam String paymentId, Model model) {
        RefundPaymentResponse response = paymentService.getPaymentWithRefundDetails(paymentId);
        System.out.println("111111111111111111111111"+response.getRefund());
        model.addAttribute("payment", response.getPayment());
        model.addAttribute("refund", response.getRefund());
        return "refund-details";
    }
}


