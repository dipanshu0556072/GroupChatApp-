package com.GroupChatAppexample.GroupChat.controller;

import com.GroupChatAppexample.GroupChat.jwt.JwtGenerator;
import com.GroupChatAppexample.GroupChat.model.FriendShipStatus;
import com.GroupChatAppexample.GroupChat.model.User;
import com.GroupChatAppexample.GroupChat.repo.FriendShipStatusRepo;
import com.GroupChatAppexample.GroupChat.repo.UserRepo;
import com.GroupChatAppexample.GroupChat.service.FriendShipStatusService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
public class FriendShipStatusController
{
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private FriendShipStatusRepo friendShipStatusRepo;
    @Autowired
    private FriendShipStatusService friendShipStatusService;
    @Autowired
    private JwtGenerator jwtGenerator;



    // send friendRequest
    @Operation(summary = "Send a friend request")
    @PostMapping("/send-request")
    public ResponseEntity<?> sendRequest(@RequestParam String fromId, @RequestParam String toId) {
        return friendShipStatusService.sendFriendRequest(fromId, toId);
    }

    @Operation(summary = "Accept a friend request")
    @PostMapping("/accept-request")
    public ResponseEntity<?> acceptRequest(@RequestParam String fromId, @RequestParam String toId) {
        return friendShipStatusService.acceptRequest(fromId, toId);
    }

    @Operation(summary = "Fetch list of sent friend requests")
    @GetMapping("/sent-requests/{status}")
    public List<FriendShipStatus> fetchSentRequests(HttpServletRequest httpServletRequest, @PathVariable(required = true) String status) {

        String authHeader = httpServletRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String currentUserEmailId = jwtGenerator.extractUserName(authHeader.substring(7));
            User user = userRepo.findByEmailId(currentUserEmailId)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmailId));

            return friendShipStatusService.fetchSendRequest(user.getId(), status);
        }
        return null;
    }

    @Operation(summary = "Fetch list of received friend requests")
    @GetMapping("/received-requests/{status}")
    public List<FriendShipStatus>fetchReceivedRequests(HttpServletRequest httpServletRequest, @PathVariable(required = true) String status){
        String authHeader = httpServletRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String currentUserEmailId = jwtGenerator.extractUserName(authHeader.substring(7));
            User user = userRepo.findByEmailId(currentUserEmailId)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmailId));

           return friendShipStatusService.receiveRequest(user.getId(), status);
        }
        return null;
    }


    @Operation(summary = "Fetch friend list of a user")
    @GetMapping("/friends/{status}")
    public List<FriendShipStatus>fetchFriendsList(HttpServletRequest httpServletRequest, @PathVariable(required = true) String status){
        String authHeader = httpServletRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String currentUserEmailId = jwtGenerator.extractUserName(authHeader.substring(7));
            User user = userRepo.findByEmailId(currentUserEmailId)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + currentUserEmailId));

            return friendShipStatusService.fetchFriendList(user.getId(), status);
        }
        return null;
    }


}
