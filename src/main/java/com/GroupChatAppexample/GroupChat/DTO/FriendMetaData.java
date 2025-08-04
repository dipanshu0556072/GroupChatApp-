package com.GroupChatAppexample.GroupChat.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FriendMetaData
{
    private String userId;
    private String userName;
    private String status;
    private LocalDateTime timeStamp;
}
