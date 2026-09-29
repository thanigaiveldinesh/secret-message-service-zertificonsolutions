package com.crypto.secret.message.repository;


import com.crypto.secret.message.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface MessageRepository extends JpaRepository<Message, String> {
    void deleteByCreatedAtBefore(LocalDateTime date);
}
