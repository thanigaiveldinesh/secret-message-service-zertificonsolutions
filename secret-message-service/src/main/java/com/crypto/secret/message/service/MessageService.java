package com.crypto.secret.message.service;

import com.crypto.secret.message.exception.MessageException;
import com.crypto.secret.message.model.Message;
import com.crypto.secret.message.model.MessageRequest;
import com.crypto.secret.message.model.MessageResponse;
import com.crypto.secret.message.repository.MessageRepository;
import io.nats.client.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final CryptoService cryptoService;
    private final Connection natsConnection;
    private final PasswordDeliveryService passwordDeliveryService;

    @Value("${message.max.tries}")
    private int maxTries;

    @Value("${message.expiry.days}")
    private int expiryDays;

    public MessageResponse createMessage(MessageRequest request) {
        String password = cryptoService.generatePassword();
        String id = UUID.randomUUID().toString();

        Message message = Message.builder()
                .id(id)
                .encryptedContent(cryptoService.encrypt(request.getContent(), password))
                .tries(0)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(expiryDays))
                .build();

        messageRepository.save(message);
        publishToNats("message.created", id);
        passwordDeliveryService.deliverPassword(id, password);

        return MessageResponse.builder()
                .id(id)
                .password(password)
                .build();
    }

    public String getMessage(String id, String password) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageException("Message not found", 404));

        if (message.getTries() >= maxTries) {
            messageRepository.delete(message);
            throw new MessageException("Maximum tries exceeded, message deleted", 410);
        }

        try {
            String decryptedContent = cryptoService.decrypt(message.getEncryptedContent(), password);
            messageRepository.delete(message);
            return decryptedContent;
        } catch (Exception e) {
            message.setTries(message.getTries() + 1);
            messageRepository.save(message);
            throw new MessageException("Invalid password, message deleted if max tries reached", 400);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredMessages() {
        messageRepository.deleteByCreatedAtBefore(LocalDateTime.now().minusDays(expiryDays));
    }

    private void publishToNats(String subject, String message) {
        try {
            natsConnection.publish(subject, message.getBytes());
        } catch (Exception e) {
            log.error("Failed to publish to NATS", e);
        }
    }
}
