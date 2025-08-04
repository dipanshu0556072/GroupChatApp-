package com.GroupChatAppexample.GroupChat.repo;

import com.GroupChatAppexample.GroupChat.model.FriendShipStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FriendShipStatusRepo  extends MongoRepository<FriendShipStatus,String>
{

    Optional<FriendShipStatus>findByFromUserIdAndToUserId(String FromUserId,String toUserId);
    boolean existsByFromUserIdAndToUserId(String FromUserId,String toUserId);
    //check sent request for currentUser
    List<FriendShipStatus>findByFromUserIdAndStatus(String fromUserId,String status);
    //check received request for currentUser
    List<FriendShipStatus>findByToUserIdAndStatus(String currentUserId,String status);
}
