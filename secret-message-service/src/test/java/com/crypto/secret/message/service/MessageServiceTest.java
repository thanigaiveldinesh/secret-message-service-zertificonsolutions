package com.crypto.secret.message.service;

import com.crypto.secret.message.model.MessageRequest;
import com.crypto.secret.message.model.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private CryptoService cryptoService;

    @Test
    void testCreateAndRetrieveMessage() {
        MessageRequest request = new MessageRequest();
        request.setContent("secret content");

        MessageResponse response = messageService.createMessage(request);

        String content = messageService.getMessage(response.getId(), response.getPassword());
        assertEquals("secret content", content);
    }

    @Test
    void testMaxTriesExceeded() {
        String password = "testPassword";
        String message = "Secret Message";
        String encryptedMessage = cryptoService.encrypt(message, password);
        String decryptedMessage = cryptoService.decrypt(encryptedMessage, password);
        assertEquals(message, decryptedMessage);
    }
    @Test
    void testMaxTriesExceededInMessageService() {
        MessageRequest request = new MessageRequest();
        request.setContent("secret content");
        MessageResponse response = messageService.createMessage(request);
        assertThrows(RuntimeException.class, () -> messageService.getMessage(response.getId(), "wrong1"));
        assertThrows(RuntimeException.class, () -> messageService.getMessage(response.getId(), "wrong2"));
        assertThrows(RuntimeException.class, () -> messageService.getMessage(response.getId(), "wrong3"));
        assertThrows(RuntimeException.class, () -> messageService.getMessage(response.getId(), response.getPassword()));
    }
}
