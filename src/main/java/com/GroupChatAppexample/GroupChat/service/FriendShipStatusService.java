package com.GroupChatAppexample.GroupChat.service;

import com.GroupChatAppexample.GroupChat.DTO.FriendSuggestionDTO;
import com.GroupChatAppexample.GroupChat.config.RelationshipStatus;
import com.GroupChatAppexample.GroupChat.model.FriendShipStatus;
import com.GroupChatAppexample.GroupChat.model.User;
import com.GroupChatAppexample.GroupChat.repo.FriendShipStatusRepo;
import com.GroupChatAppexample.GroupChat.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FriendShipStatusService
{
      @Autowired
      private FriendShipStatusRepo friendShipStatusRepo;

      @Autowired
      private UserRepo userRepo;


    //send Request
     public ResponseEntity<?> sendFriendRequest(String currentUserId,String otherUserId){
          if(currentUserId.equalsIgnoreCase(otherUserId)){
              throw  new IllegalArgumentException("can't send request to yourself!");
          }
          User user1=userRepo.findById(currentUserId).orElseThrow(()->new RuntimeException("User not found:"+currentUserId));
          User user2=userRepo.findById(otherUserId).orElseThrow(()->new RuntimeException("User not found:"+otherUserId));

          //check connection request already sent
         boolean friendShipStatus1=friendShipStatusRepo.existsByFromUserIdAndToUserId(currentUserId,otherUserId);
         boolean friendShipStatus2=friendShipStatusRepo.existsByFromUserIdAndToUserId(otherUserId,currentUserId);

         if(friendShipStatus1){
            FriendShipStatus friendShipStatus=friendShipStatusRepo.findByFromUserIdAndToUserId(currentUserId,otherUserId).orElseThrow(() -> new RuntimeException("Friend request not found"));
            friendShipStatusRepo.delete(friendShipStatus);
             return ResponseEntity.ok("Friend request cancelled to: " + user2.getUserName());
         }

         if(friendShipStatus2){
             return acceptRequest(currentUserId, otherUserId);
         }

         FriendShipStatus friendShip=new FriendShipStatus();
         friendShip.setFromUserId(currentUserId);
         friendShip.setToUserId(otherUserId);
         friendShip.setStatus(String.valueOf(RelationshipStatus.REQUESTED));
         friendShip.setTimeStamp(LocalDateTime.now());
         friendShipStatusRepo.save(friendShip);

         return ResponseEntity.status(HttpStatus.CREATED).body("request sent to:"+user2.getUserName());
     }

    //accept request
    public ResponseEntity<?> acceptRequest(String currentUserId,String otherUserId){
        if(currentUserId.equalsIgnoreCase(otherUserId)){
            throw  new IllegalArgumentException("can't send request to yourself!");
        }
        User user1=userRepo.findById(currentUserId).orElseThrow(()->new RuntimeException("User not found:"+currentUserId));
        User user2=userRepo.findById(otherUserId).orElseThrow(()->new RuntimeException("User not found:"+otherUserId));

        FriendShipStatus friendShipStatus1=friendShipStatusRepo.findByFromUserIdAndToUserId(otherUserId,currentUserId).orElseThrow(() -> new RuntimeException("Friend request not found"));;

        //update user1->user2
        if(friendShipStatus1!=null){
           friendShipStatus1.setStatus(String.valueOf(RelationshipStatus.MUTUAL));
           friendShipStatus1.setTimeStamp(LocalDateTime.now());
            friendShipStatusRepo.save(friendShipStatus1);
        }

        return ResponseEntity.ok("request accepted!");
    }

    //decline request
    public ResponseEntity<?> declineRequest(String currentUserId,String otherUserId){
        if(currentUserId.equalsIgnoreCase(otherUserId)){
            throw  new IllegalArgumentException("can't send request to yourself!");
        }
        User user1=userRepo.findById(currentUserId).orElseThrow(()->new RuntimeException("User not found:"+currentUserId));
        User user2=userRepo.findById(otherUserId).orElseThrow(()->new RuntimeException("User not found:"+otherUserId));
        FriendShipStatus friendShipStatus=friendShipStatusRepo.findByFromUserIdAndToUserId(otherUserId,currentUserId).orElseThrow(() -> new RuntimeException("Friend request not found"));;

        if(friendShipStatus!=null){
            friendShipStatusRepo.delete(friendShipStatus);
        }
        return ResponseEntity.ok("Friend request declined and deleted.");
    }
    //fetch sendRequest
    public List<FriendShipStatus> fetchSendRequest(String currentUserId, String status)
    {
        User user=userRepo.findById(currentUserId).orElseThrow(()->new RuntimeException("User not found:"+currentUserId));
        return friendShipStatusRepo.findByFromUserIdAndStatus(currentUserId,status);
    }
    //fetch receivedRequest
    public List<FriendShipStatus>receiveRequest(String currentUserId,String status){
        User user=userRepo.findById(currentUserId).orElseThrow(()->new RuntimeException("User not found:"+currentUserId));
         return friendShipStatusRepo.findByToUserIdAndStatus(currentUserId,status);
    }

    //fetch friendsList
    public List<FriendShipStatus>fetchFriendList(String currentUserId,String status){
        User user=userRepo.findById(currentUserId).orElseThrow(()->new RuntimeException("User not found:"+currentUserId));
        List<FriendShipStatus>sentRequest=friendShipStatusRepo.findByFromUserIdAndStatus(currentUserId,status);
        List<FriendShipStatus>acceptedRequest=friendShipStatusRepo.findByToUserIdAndStatus(currentUserId,status);
        List<FriendShipStatus>Friends=new ArrayList<>();
        Friends.addAll(sentRequest);
        Friends.addAll(acceptedRequest);

        return Friends;
    }


    //suggest friends
    public List<FriendSuggestionDTO>friendSuggestList(String currentUserId){
        User user=userRepo.findById(currentUserId).orElseThrow(()->new RuntimeException("User not found:"+currentUserId));
        //fetch current user friends
        List<FriendShipStatus>currentUserFriends=friendShipStatusRepo.findByFromUserIdAndStatus(currentUserId,String.valueOf(RelationshipStatus.MUTUAL));

        //now fetch the current user friends
        Set<String> currentUserFriendIds=new HashSet<>();
        for(FriendShipStatus friend:currentUserFriends){
            currentUserFriendIds.add(friend.getFromUserId().equals(currentUserId)?friend.getToUserId():friend.getFromUserId());
        }

        //now fetch the friends of friends also setting the priority of the suggested friends
        Map<String,Integer>suggestPriority=new HashMap<>();
        for(String friendId:currentUserFriendIds){
            //fetch the current userFriends of friends
            List<FriendShipStatus>FriendsOfFriends=friendShipStatusRepo.findByFromUserIdAndStatus(friendId,String.valueOf(RelationshipStatus.MUTUAL));
            for(FriendShipStatus friend:FriendsOfFriends){
                String suggestId=friend.getFromUserId().equals(friendId)?friend.getToUserId():friend.getFromUserId();
                if(!suggestId.equals(currentUserId) && !currentUserFriendIds.contains(suggestId)){
                    suggestPriority.put(suggestId, suggestPriority.getOrDefault(suggestId,0)+1);
                }
            }
        }

        //sort the high mutual suggestId in descending order
        List<Map.Entry<String,Integer>>suggestList=new ArrayList<>(suggestPriority.entrySet());
        suggestList.sort((x,y)->y.getValue()-x.getValue());
        Map<String,Integer>sortedSuggestPriority=new LinkedHashMap<>();

        for(Map.Entry<String,Integer>entry:suggestList){
            sortedSuggestPriority.put(entry.getKey(),entry.getValue());
        }

        //suggestion List
        List<FriendSuggestionDTO>suggestionList=new ArrayList<>();
        for(Map.Entry<String,Integer>entry:suggestList){
            User userData=userRepo.findById(entry.getKey()).orElse(null);
            if(userData!=null){
                FriendSuggestionDTO friendSuggestionDTO=new FriendSuggestionDTO();
                friendSuggestionDTO.setUserId(userData.getId());
                friendSuggestionDTO.setUserName(userData.getUserName());
                friendSuggestionDTO.setFullName(userData.getFullName());
                friendSuggestionDTO.setStatus(String.valueOf(RelationshipStatus.NOT_FOLLOWING));
                friendSuggestionDTO.setProfilePictureUrl("/images/profile"+userData.getId());
                suggestionList.add(friendSuggestionDTO);
            }
        }


        return suggestionList;
    }
}
