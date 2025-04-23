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
   ![Payment Initiation](![image](https://github.com/user-attachments/assets/82f38d5e-1e91-4081-b548-7d2ae0b0c5d8)
)

2. **PhonePe Interface Simulation**  
   ![PhonePe Interface](https://github.com/user-attachments/assets/4d362a52-837a-44c2-a385-2ba7552d104e)


3. **Payment Result Page**  
   ![Payment Result](https://github.com/user-attachments/assets/c2b19fa6-ebe3-41ca-a4df-4a8ac6c75325)


### Admin Dashboard Screens:
1. **Payment Stats Overview**  
   ![Payment Stats](https://github.com/user-attachments/assets/8f19a195-a737-4c57-99b7-c13486a4be3a)


2. **Refund Management Panel**  
   ![Refund Interface](https://github.com/user-attachments/assets/a7f657a5-f675-4b8b-89a0-c1283d110977)

