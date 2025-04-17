package com.example.payment_gateway_poc.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.stereotype.Component;


@Component
public class PhonePeUtils {

    public String generateXVerify(String payload, String saltKey, String saltIndex) throws Exception {
    String data = payload + "/pg/v1/pay" + saltKey;
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
    
    String sha256hex = bytesToHex(hash); // instead of DatatypeConverter
    return sha256hex + "###" + saltIndex;
}
private String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1)
            hexString.append('0');
        hexString.append(hex);
    }
    return hexString.toString();
}

}