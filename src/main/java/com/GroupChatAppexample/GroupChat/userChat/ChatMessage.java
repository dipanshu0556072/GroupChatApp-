package com.GroupChatAppexample.GroupChat.userChat;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String messageId;
    private String from;
    private String to;
    private String content;
    private Long timestamp;
    private String messageType;
    private String status;
    private boolean isGroupMessage;
    private String groupId;
    private String fileUrl;
}
