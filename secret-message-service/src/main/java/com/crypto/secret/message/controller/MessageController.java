package com.crypto.secret.message.controller;


import com.crypto.secret.message.service.MessageService;
import com.crypto.secret.message.model.MessageRequest;
import com.crypto.secret.message.model.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> createMessage(@RequestBody MessageRequest request) {
        try {
            return ResponseEntity.ok(messageService.createMessage(request));
        } catch (Exception e) {
            log.error("Failed to create message", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getMessage(
            @PathVariable String id,
            @RequestParam String password) {
        String content = messageService.getMessage(id, password);
        return ResponseEntity.ok(content);
    }
}
