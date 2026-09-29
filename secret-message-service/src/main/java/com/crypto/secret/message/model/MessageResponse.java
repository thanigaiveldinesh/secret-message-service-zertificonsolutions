package com.crypto.secret.message.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {
    private String id;
    private String password;
}
