package com.GroupChatAppexample.GroupChat.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class User {
    private String id;
    private String userName;
    private String fullName;
    private String phoneNumber;
    private String emailId;
    private String profilePicture;
    private String bio;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
