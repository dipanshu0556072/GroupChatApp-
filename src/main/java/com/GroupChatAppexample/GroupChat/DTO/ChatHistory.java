package com.GroupChatAppexample.GroupChat.DTO;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory
{
    private String id;
    private String name;
    private String message;
    private String count;
    private String image;
}
