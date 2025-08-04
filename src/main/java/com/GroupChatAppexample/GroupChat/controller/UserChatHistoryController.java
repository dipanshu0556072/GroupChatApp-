package com.GroupChatAppexample.GroupChat.controller;

import com.GroupChatAppexample.GroupChat.DTO.ChatHistory;
import com.GroupChatAppexample.GroupChat.DTO.ChatHistoryFormat;
import com.GroupChatAppexample.GroupChat.DTO.FriendMetaData;
import com.GroupChatAppexample.GroupChat.jwt.JwtGenerator;
import com.GroupChatAppexample.GroupChat.model.User;
import com.GroupChatAppexample.GroupChat.repo.UserRepo;
import com.GroupChatAppexample.GroupChat.service.PersonalChatService;
import com.GroupChatAppexample.GroupChat.userChat.PersonalChat.PersonalChatMessage;
import com.GroupChatAppexample.GroupChat.userChat.PersonalChat.UserChatHistory;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.kafka.shaded.io.opentelemetry.proto.metrics.v1.Summary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
public class UserChatHistoryController
{
    @Autowired
    private PersonalChatService personalChatService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserController userController;

    @Autowired
    private JwtGenerator jwtGenerator;
    @Operation(summary = "store personal chat message")
    @PostMapping("/storePersonalChat")
    public ResponseEntity<?> storePersonalChat(@RequestBody PersonalChatMessage personalChatMessage, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String currentUserEmailId = jwtGenerator.extractUserName(authHeader.substring(7));
            User currentUser = userRepo.findByEmailId(currentUserEmailId)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmailId));

            // Use currentUser.getId() as sender ID
            return personalChatService.storePrivateChatInDB(
                    currentUser.getId(),                          // ✅ Use JWT verified sender
                    personalChatMessage.getTo(),
                    personalChatMessage.getContent()
            );
        }

        return ResponseEntity.badRequest().body("Invalid token");
    }


    @Operation(summary = "fetch-personal-chatHistory")
    @GetMapping("/fetchPersonalChat")
    public List<UserChatHistory> fetchPersonalChatHistory(HttpServletRequest request, String otherUserId){
        //fetch current login user
        String authHeader = request.getHeader("Authorization");
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            String currentUserEmailId=jwtGenerator.extractUserName(authHeader.substring(7));
            User user=userRepo.findByEmailId(currentUserEmailId).orElseThrow(()->new RuntimeException("user not found with given emailId:"+currentUserEmailId));

            return personalChatService.fetchChatHistory(user.getId(),otherUserId);
        }
        throw new RuntimeException("Authorization header missing or invalid while fetching personal chat");
    }

    //delete chat history with other user
    @Operation(summary="delete-private-chat")
    @PutMapping("/deletePrivateChat")
    public ResponseEntity<?>deleteChat(HttpServletRequest request,String otherUserId){
        String authHeader = request.getHeader("Authorization");
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            String currentUserEmailId=jwtGenerator.extractUserName(authHeader.substring(7));
            User user=userRepo.findByEmailId(currentUserEmailId).orElseThrow(()->new RuntimeException("user not found with given emailId:"+currentUserEmailId));
            return personalChatService.deletePrivateChat(user.getId(),otherUserId);
        }
        throw new RuntimeException("Authorization header missing or invalid while fetching personal chat");
    }


    //fetch user chat history
    @Operation(summary = "fetch current UserChatHistory")
    @GetMapping("/currentFetchChatHistory")
    public List<ChatHistory>  fetchChat(HttpServletRequest request){
        //fetch current login user
        String authHeader=request.getHeader("Authorization");
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            String currentUserEmailId= jwtGenerator.extractUserName(authHeader.substring(7));
            User user=userRepo.findByEmailId(currentUserEmailId).orElseThrow(()->new RuntimeException("user not found with given emailId:"+currentUserEmailId));
            return personalChatService.fetchUserChatHistory(user.getId());
        }
        return null;
    }
}
