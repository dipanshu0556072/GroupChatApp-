package com.GroupChatAppexample.GroupChat.userChat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String messageId;
    private String from;
    private String to;
    private String content;
    private long timestamp;
    private String messageType;
    private String status;
    private boolean isGroupMessage;
    private String groupId;
    private String fileUrl;
}
