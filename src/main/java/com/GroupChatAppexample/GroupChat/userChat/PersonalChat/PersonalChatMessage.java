package com.GroupChatAppexample.GroupChat.userChat.PersonalChat;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PersonalChatMessage
{
    private String id;
    private String from;
    private String to;
    private String content;
    private LocalDateTime timestamp;
    private String status; // "sent", "delivered", "seen"
    private Long seenAt;
    private String type;   // "text", "image", "file", etc.
    private String mediaUrl; // Optional if type is media
}
