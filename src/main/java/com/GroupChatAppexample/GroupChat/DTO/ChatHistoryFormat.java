package com.GroupChatAppexample.GroupChat.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatHistoryFormat
{
    private String userId;
    private String userName;
    private String status;
}
