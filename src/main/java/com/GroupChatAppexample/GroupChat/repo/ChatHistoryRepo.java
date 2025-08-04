package com.GroupChatAppexample.GroupChat.repo;


import com.GroupChatAppexample.GroupChat.userChat.PersonalChat.UserChatHistory;
import com.GroupChatAppexample.GroupChat.userChat.PersonalChat.UserInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChatHistoryRepo extends MongoRepository<UserChatHistory,String>
{
   Optional<UserChatHistory>findById(String key);
}
