package com.GroupChatAppexample.GroupChat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("FriendShipStatus")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendShipStatus
{
    @Id
    private String id;
    private String fromUserId;
    private String toUserId;
    private String status;
    private LocalDateTime timeStamp;
}
