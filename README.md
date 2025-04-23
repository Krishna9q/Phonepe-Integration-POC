# PhonePe Payment Gateway Integration POC

This project is a Proof of Concept (POC) for integrating the PhonePe Payment Gateway. 
It demonstrates the integration process, simulates payments, and provides an admin dashboard to manage payment statuses and refunds.

---

## 🔹 Features

### User Flow (Demo):
1. **Add Mobile Number and Amount**  
   Users enter their mobile number and payment amount.

2. **Payment Initiation**  
   Clicking the "Pay" button generates a payment initiation request, calling an API to create the payment.

3. **PhonePe Interface**  
   Users are redirected to the PhonePe interface to select a payment method.

4. **Simulate Payment Response**  
   Users can simulate different payment outcomes: `SUCCESS`, `PENDING`, or `FAILED`.

5. **Redirect to Payment Result Page**  
   After the simulation, users are redirected to a result page displaying the payment status.

---

### Admin Dashboard Features:
1. **Payment Stats**  
   View detailed stats for all payment attempts.

2. **Refund Management**  
   Admins can initiate refunds and view refund details for demo transactions.

---

## 🔧 Tech Stack

- **Backend**: Java Spring Boot
- **Database**: PostgreSQL
- **Containerization**: Docker
- **Templates**: Thymeleaf

---

## 📸 Screenshots

### User Flow Screens:
1. **Payment Initiation Screen**  
   ![Payment Initiation](screenshots/payment_initiation.png)

2. **PhonePe Interface Simulation**  
   ![PhonePe Interface](screenshots/phonepe_interface.png)

3. **Payment Result Page**  
   ![Payment Result](screenshots/payment_result.png)

### Admin Dashboard Screens:
1. **Payment Stats Overview**  
   ![Payment Stats](screenshots/payment_stats.png)

2. **Refund Management Panel**  
   ![Refund Management](screenshots/refund_management.png)
