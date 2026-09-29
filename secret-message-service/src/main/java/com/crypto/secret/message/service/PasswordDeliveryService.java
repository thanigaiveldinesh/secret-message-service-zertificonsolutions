package com.crypto.secret.message.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PasswordDeliveryService {

    public void deliverPassword(String messageId, String password) {
        log.info("Password for message {} would be delivered securely: {}", messageId, password);
    }
}