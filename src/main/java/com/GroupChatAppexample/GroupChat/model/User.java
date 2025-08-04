package com.GroupChatAppexample.GroupChat.model;

import com.GroupChatAppexample.GroupChat.DTO.FriendMetaData;
import com.GroupChatAppexample.GroupChat.config.Roles;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
@Builder
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

    private String userRole;
    private String activeToken;

    //friendList
    private Set<FriendMetaData>friendList=new HashSet<>();
    //send request
    private Set<FriendMetaData>sentRequest=new HashSet<>();
    //inbox request
    private Set<FriendMetaData>inboxRequest=new HashSet<>();
    //block user
    private Set<FriendMetaData>blockUser=new HashSet<>();



}
