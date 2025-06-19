package com.GroupChatAppexample.GroupChat.repo;

import com.GroupChatAppexample.GroupChat.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepo extends MongoRepository<User,String>
{
  //find user by emailID
  Optional<User> findByEmailId(String emailID);

  //find user by userName
  Optional<User>findByUserName(String userName);
}
