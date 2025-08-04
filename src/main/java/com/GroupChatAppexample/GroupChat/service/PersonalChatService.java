package com.GroupChatAppexample.GroupChat.service;

import com.GroupChatAppexample.GroupChat.DTO.ChatHistory;
import com.GroupChatAppexample.GroupChat.DTO.ChatHistoryFormat;
import com.GroupChatAppexample.GroupChat.DTO.FriendMetaData;
import com.GroupChatAppexample.GroupChat.model.User;
import com.GroupChatAppexample.GroupChat.repo.ChatHistoryRepo;
import com.GroupChatAppexample.GroupChat.repo.UserRepo;
import com.GroupChatAppexample.GroupChat.userChat.PersonalChat.PersonalChatMessage;
import com.GroupChatAppexample.GroupChat.userChat.PersonalChat.UserChatHistory;
import com.GroupChatAppexample.GroupChat.userChat.PersonalChat.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PersonalChatService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatHistoryRepo chatHistoryRepo;

    // store private chatHistory in DB
    public ResponseEntity<?> storePrivateChatInDB(String currentUserId, String otherUserId, String content) {
        try {

            String key = currentUserId.compareTo(otherUserId) < 0
                    ? currentUserId + "-" + otherUserId
                    : otherUserId + "-" + currentUserId;

            User currentUser = userRepo.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + currentUserId));

            User otherUser = userRepo.findById(otherUserId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + otherUserId));

            if (currentUser != null && otherUser != null) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(otherUser.getId());
                userInfo.setUserName(otherUser.getUserName());
                userInfo.setFullName(otherUser.getFullName());
                userInfo.setProfilePictureUrl("/images/profile/" + otherUser.getId());

                // ✅ Fix #1: Correct sender and receiver
                PersonalChatMessage personalChatMessage = new PersonalChatMessage();
                personalChatMessage.setFrom(currentUserId);
                personalChatMessage.setTo(otherUserId);
                personalChatMessage.setContent(content);
                personalChatMessage.setId(UUID.randomUUID().toString());
                personalChatMessage.setTimestamp(LocalDateTime.now());
                personalChatMessage.setStatus("sent");
                personalChatMessage.setType("string");
                personalChatMessage.setMediaUrl(null);

                UserChatHistory userChatHistory = chatHistoryRepo.findById(key).orElse(null);
                if (userChatHistory == null) {
                    userChatHistory = new UserChatHistory();
                    userChatHistory.setId(key);
                    userChatHistory.setChatWith(userInfo);
                    userChatHistory.setMessages(new ArrayList<>()); // ✅ Fix #4: Initialize messages
                }

                // ✅ Null check for safety (in case it already exists but message list is null)
                if (userChatHistory.getMessages() == null) {
                    userChatHistory.setMessages(new ArrayList<>());
                }

                userChatHistory.getMessages().add(personalChatMessage);
                chatHistoryRepo.save(userChatHistory);

                return ResponseEntity.ok("Chat message stored successfully");
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user data");

        } catch (Exception e) {
            throw new RuntimeException("Error while storing private chat in DB: " + e.getMessage());
        }
    }

    // Fetch chat history between two users
    public List<UserChatHistory> fetchChatHistory(String currentUserId, String otherUserId) {
        if (currentUserId.equalsIgnoreCase(otherUserId)) {
            throw new RuntimeException("Both userId can't be the same: " + currentUserId);
        }

        // Use consistent key
        String key = currentUserId.compareTo(otherUserId) < 0
                ? currentUserId + "-" + otherUserId
                : otherUserId + "-" + currentUserId;

        User currentUser = userRepo.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserId));
        User otherUser = userRepo.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + otherUserId));

        UserChatHistory chat = chatHistoryRepo.findById(key).orElse(null);

        if (chat == null) {
            chat = new UserChatHistory();
            chat.setId(key);
            chat.setMessages(new ArrayList<>());
        }

        // Set chatWith as the *other* user (relative to whoever is logged in)
        User chatWithUser = currentUserId.equals(otherUser.getId()) ? currentUser : otherUser;

        UserInfo chatWith = new UserInfo();
        chatWith.setUserId(chatWithUser.getId());
        chatWith.setUserName(chatWithUser.getUserName());
        chatWith.setFullName(chatWithUser.getFullName());
        chatWith.setBio(chatWithUser.getBio());
        chatWith.setProfilePictureUrl("/images/profile/" + chatWithUser.getId());

        chat.setChatWith(chatWith);

        return List.of(chat);
    }

    //fetch current user chat with all users
    public   List<ChatHistory>  fetchUserChatHistory(String currentUserId) {
        try {
            User userFrom=userRepo.findById(currentUserId).orElseThrow(()->new RuntimeException("userId not found!"));
            Set<FriendMetaData>currentUserFriends=userFrom.getFriendList();

            List<ChatHistory> chatHistoryList = new ArrayList<>();

            ChatHistory chatData=new ChatHistory();
            for(FriendMetaData userChat:currentUserFriends){
                UserChatHistory userChatData=new UserChatHistory();
                //fetch the chat of current user with other user
                List<UserChatHistory> userChatHistory=fetchChatHistory(currentUserId,userChat.getUserId());
                 for(UserChatHistory chatHistory:userChatHistory){
                         //fetch lastMessage of the chat
                     List<PersonalChatMessage> messages = chatHistory.getMessages();
                     PersonalChatMessage lastMessage = messages.get(messages.size() - 1);
                         chatData.setMessage(lastMessage.getContent());
                         userChatData.setMessages(List.of(lastMessage));
                         break;
                 }
                userChatData.setId(UUID.randomUUID().toString());

                chatData.setId(userFrom.getId());
                chatData.setName(userFrom.getFullName());
                chatData.setImage("/images/profile/"+userFrom.getId());

                chatHistoryList.add(chatData);
                break;
            }


            return chatHistoryList;


        } catch (Exception e) {
            throw new RuntimeException("Error while fetching chat history: " + e.getMessage());
        }
    }


    //delete chat history with other user
    public ResponseEntity<?>deletePrivateChat(String currentUserId,String otherUserId){
        try{
            String key = currentUserId.compareTo(otherUserId) < 0
                    ? currentUserId + "-" + otherUserId
                    : otherUserId + "-" + currentUserId;

            User currentUser = userRepo.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + currentUserId));

            User otherUser = userRepo.findById(otherUserId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + otherUserId));
            UserChatHistory userChatHistory = chatHistoryRepo.findById(key)
                    .orElseThrow(() -> new RuntimeException("No chat history found for key: " + key));

            userChatHistory.getMessages().clear();
            chatHistoryRepo.save(userChatHistory);
            return ResponseEntity.ok("Chat history deleted successfully.");

        }
        catch (Exception e){
            throw new RuntimeException("encountering error while deleteing chat: " + e.getMessage());
        }
    }

    //update delivered msg status
    public void deliveredMsgStatus(String currentUserId, String otherUserId, String msgId) {
        try {
            String key = currentUserId.compareTo(otherUserId) < 0 ? currentUserId + "-" + otherUserId : otherUserId + "-" + currentUserId;
            System.out.println("💡 Delivered status for key: " + key);
            System.out.println("Looking for message ID: " + msgId);

            UserChatHistory userChatHistory = chatHistoryRepo.findById(key)
                    .orElseThrow(() -> new RuntimeException("No chat history found for key: " + key));

            for (PersonalChatMessage message : userChatHistory.getMessages()) {
                System.out.println("Message ID in loop: " + message.getId());
                if (message.getId().equalsIgnoreCase(msgId)) {
                    message.setStatus("seen");
                    message.setSeenAt(System.currentTimeMillis());
                    System.out.println("✅ Marked as seen: " + message.getId());
                    break;
                }
            }

            chatHistoryRepo.save(userChatHistory);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }



}
