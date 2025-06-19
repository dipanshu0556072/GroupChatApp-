package com.GroupChatAppexample.GroupChat.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserDTO
{
    private String id;
    private String userName;
    private String fullName;
    private String phoneNumber;
    private String profilePicture;
    private String bio;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;



    //friendList
    private Set<String> friendList=new HashSet<>();
    //send request
    private Set<String>sendRequest=new HashSet<>();
    //inbox request
    private Set<String>inboxRequest=new HashSet<>();
    //declined request
    private Set<String>declinedRequest=new HashSet<>();
}
