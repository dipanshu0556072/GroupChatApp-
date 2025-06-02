package com.GroupChatAppexample.GroupChat.controller;


import com.GroupChatAppexample.GroupChat.jwt.JwtGenerator;
import com.GroupChatAppexample.GroupChat.model.User;
import com.GroupChatAppexample.GroupChat.repo.UserRepo;
import com.GroupChatAppexample.GroupChat.service.UserService;
import com.GroupChatAppexample.GroupChat.config.EmailSender;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<?> sendOTPtoEmailID(@RequestParam(required = true) String emailId) {
        try {
            Optional<User> user = userRepo.findByEmailId(emailId);
//            if (user.isPresent()) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exist with given id:" + emailId);
//            }

            return emailSender.sendOTPtoEmailID(emailId.trim().toLowerCase());
        } catch (Exception e) {
            throw new RuntimeException("Error in sending OTP to the user!");
        }
    }

    @Operation(summary = "verifyOTP")
    @PostMapping("/otp_verify")
    public ResponseEntity<?> verifyOTP(@RequestParam(required = true) String emailId,
                                       @RequestParam(required = true) String OTP) {
        // ✅ Normalize email
        String emailID = emailId.trim().toLowerCase();

        try {
            // ✅ Verify OTP
            if (emailSender.verifyEmailOTP(emailID, OTP).getStatusCode().equals(HttpStatus.OK)) {
                User user = userRepo.findByEmailId(emailID).orElseThrow(() ->
                        new RuntimeException("User not found with email: " + emailID));

                System.out.println("This is the user Role: " + user.getUserRole());

                // ✅ Generate JWT and set it
                String token = jwtGenerator.generateToken(emailID, user.getUserRole().toString());

                user.setActiveToken(token);
                userRepo.save(user);

                System.out.println("Bearer " + token);

                // ✅ Return success response
                return ResponseEntity.status(HttpStatus.OK).body(
                        Map.of(
                                "message", "User login successful!",
                                "token", "Bearer " + token,
                                "email", user.getEmailId(),
                                "role", user.getUserRole()
                        )
                );
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error in verifying OTP: " + e.getMessage());
        }
    }


    // Optional: For manual redirect button
    @GetMapping("/login-google")
    public void redirectToGoogleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<?> oauth2Success(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        return ResponseEntity.status(HttpStatus.GONE).body(email+" "+name);
    }

    //update profile
    @PutMapping(value = "/update-profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePicture(
            @RequestPart("userJSON") String userJSON,
            @RequestPart(value = "image",required = false) MultipartFile imageFile,HttpServletRequest httpServletRequest) {
        try {
            String authHeader=httpServletRequest.getHeader("Authorization");
            String emailId=jwtGenerator.extractUserName(authHeader.substring(7));
            Optional<User> userData=userRepo.findByEmailId(emailId);
            if(userData.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no user registered with given emailId:"+emailId);
            }
            User user=userData.get();
          return userService.updateUserProfile(userJSON,emailId,imageFile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
        }
    }
    //remove userDP from the profile
    @PutMapping("/remove-profileDP")
    ResponseEntity<?> removeProfileDP(HttpServletRequest httpServletRequest){
        try{
            String authHeader=httpServletRequest.getHeader("Authorization");
            String emailId=jwtGenerator.extractUserName(authHeader.substring(7));
            Optional<User> userData=userRepo.findByEmailId(emailId);
            if(userData.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("no user registered with given emailId:"+emailId);
            }
            User user=userData.get();
            user.setProfilePicture(null);
            userRepo.save(user);
         return ResponseEntity.status(HttpStatus.ACCEPTED).body("user Dp removed from profile successfully!");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove dp-image");
        }
    }


    // Fetch user profile with image base64 string
    @GetMapping("/fetchUserProfile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest httpServletRequest) {
        String authHeader = httpServletRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String emailId = jwtGenerator.extractUserName(authHeader.substring(7));
        Optional<User> userData = userRepo.findByEmailId(emailId);

        if (userData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No user registered with given emailId: " + emailId);
        }

        return ResponseEntity.ok(userData.get());
    }



    @PostMapping("/logOut")
    public ResponseEntity<String> logOut(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtGenerator.extractUserName(token);
            User user = userRepo.findByEmailId(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            user.setActiveToken(null);
            userRepo.save(user);
            return ResponseEntity.ok("Logout successful.");
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
