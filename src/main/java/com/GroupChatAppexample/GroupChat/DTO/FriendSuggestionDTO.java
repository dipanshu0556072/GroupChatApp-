package com.GroupChatAppexample.GroupChat.DTO;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FriendSuggestionDTO
{
    private String userId;
    private String userName;
    private String fullName;
    private String profilePictureUrl;
    private String status;
}
