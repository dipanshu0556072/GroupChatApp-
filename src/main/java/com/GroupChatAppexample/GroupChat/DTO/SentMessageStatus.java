package com.GroupChatAppexample.GroupChat.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class SentMessageStatus
{
    private String id;
    private String fromId;
    private String toId;
}
