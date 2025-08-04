package com.GroupChatAppexample.GroupChat.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MessageFormat
{
    private String userId;
    private String userName;
    private String message;
    private String time;
}
