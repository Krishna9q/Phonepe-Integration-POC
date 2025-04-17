package com.example.payment_gateway_poc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/payment/result")
    public String paymentResult(@RequestParam String merchentOrderId, Model model ) {
        model.addAttribute("merchentOrderId", merchentOrderId);
        return "payment_result"; // Must be inside `templates/`
    }

    
}