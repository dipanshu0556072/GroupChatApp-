package com.GroupChatAppexample.GroupChat.userChat.PersonalChat;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserInfo {
  private String userId;
  private String userName;
  private String fullName;
  private String bio;
  private String profilePictureUrl;
  private boolean isOnline;
  private LocalDateTime lastSeen;

  // Getters & Setters
}

