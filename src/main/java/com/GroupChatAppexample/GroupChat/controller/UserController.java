package com.GroupChatAppexample.GroupChat.controller;


import com.GroupChatAppexample.GroupChat.jwt.JwtGenerator;
import com.GroupChatAppexample.GroupChat.model.User;
import com.GroupChatAppexample.GroupChat.repo.UserRepo;
import com.GroupChatAppexample.GroupChat.service.UserService;
import com.GroupChatAppexample.GroupChat.config.EmailSender;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private JwtGenerator jwtGenerator;


    @Operation(summary = "signIn/SignUp to user")
    @PostMapping("/login")
    public ResponseEntity<?> sendOTPtoEmailID(@RequestParam(required = true) String emailId, @RequestParam(required = false) String OTP) {
        try {
            if(OTP==null||OTP.isEmpty()){
                return userService.sendOTP(emailId);
            }else{
                User user=userRepo.findByEmailId(emailId).orElseThrow(()->new RuntimeException("User not found with emailId:"+emailId));
                if(emailSender.verifyEmailOTP(emailId,OTP).getStatusCode().equals(HttpStatus.OK)){
                    String token = jwtGenerator.generateToken(emailId,user.getUserRole().toString());
                    user.setActiveToken(token);
                    userRepo.save(user);
                    System.out.println("Bearer " + token);
                    // ✅ Return the token in the response
                    return ResponseEntity.status(HttpStatus.OK).body(
                            Map.of(
                                    "message", "User login successful!",
                                    "token", "Bearer " + token,
                                    "email", user.getEmailId(),
                                    "role", user.getUserRole()
                            )
                    );
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error in sending OTP to the user!");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User login failed!");
    }

    @GetMapping("/login-google")
    public void redirectToGoogleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @PostMapping("/logOut")
    public ResponseEntity<String>logOut(HttpServletResponse response){
        String authHeader=response.getHeader("Authorization");
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            String token=authHeader.substring(7);
            String email=jwtGenerator.extractUserName(token);
            User user=userRepo.findByEmailId(email).orElseThrow(()->new RuntimeException("User not found with given id:"+email));
            user.setActiveToken(null);
            userRepo.save(user);
            return  ResponseEntity.ok("Logout successful.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No token provided.");
    }

    // 💬 Friend Request APIs
    @Operation(summary = "Send a friend request")
    @PostMapping("/send-request")
    public ResponseEntity<?> sendRequest(@RequestParam String fromId, @RequestParam String toId) {
        return userService.sendRequest(fromId, toId);
    }

    @Operation(summary = "Accept a friend request")
    @PostMapping("/accept-request")
    public ResponseEntity<?> acceptRequest(@RequestParam String fromId, @RequestParam String toId) {
        return userService.acceptRequest(fromId, toId);
    }

    @Operation(summary = "Decline a friend request")
    @PostMapping("/decline-request")
    public ResponseEntity<?> declineRequest(@RequestParam String fromId, @RequestParam String toId) {
        return userService.declineRequest(fromId, toId);
    }

    @Operation(summary = "Fetch list of sent friend requests")
    @GetMapping("/sent-requests/{userId}")
    public ResponseEntity<?> fetchSentRequests(@PathVariable String userId) {
        return userService.fetchSendRequest(userId);
    }

    @PreAuthorize("hasRole('user')")
    @Operation(summary = "Fetch list of received friend requests")
    @GetMapping("/received-requests/{userId}")
    public ResponseEntity<?> fetchReceivedRequests(@PathVariable String userId) {
        return userService.fetchReceivedRequest(userId);
    }

    @Operation(summary = "Fetch list of declined friend requests")
    @GetMapping("/declined-requests/{userId}")
    public ResponseEntity<?> fetchDeclinedRequests(@PathVariable String userId) {
        return userService.fetchDeclinedRequest(userId);
    }

    @Operation(summary = "Fetch friend list of a user")
    @GetMapping("/friends/{userId}")
    public ResponseEntity<?> fetchFriendsList(@PathVariable String userId) {
        return userService.fetchFriendsList(userId);
    }

}
