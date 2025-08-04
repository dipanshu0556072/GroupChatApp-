package com.GroupChatAppexample.GroupChat.userChat.PersonalChat;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "user-chatHistory")
public class UserChatHistory
{
    private String id;
    //detail of other user with respect to current user
    private UserInfo chatWith;

    //chatHistory
    List<PersonalChatMessage> messages=new ArrayList<>();
}
