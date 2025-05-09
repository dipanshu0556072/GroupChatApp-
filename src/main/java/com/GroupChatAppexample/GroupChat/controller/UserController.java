package com.GroupChatAppexample.GroupChat.controller;

import com.GroupChatAppexample.GroupChat.service.UserService;
import com.GroupChatAppexample.GroupChat.config.EmailSender;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private EmailSender emailSender;

    @Operation(summary = "Send OTP to user")
    @PostMapping("/send_email_OTP")
    public ResponseEntity<?> sendOTPtoEmailID(@RequestParam String emailID) {
        logger.info("send_email_OTP");
        try {
            return userService.sendOTP(emailID);
        } catch (Exception e) {
            throw new RuntimeException("Error in sending OTP to the user!");
        }
    }

    @Operation(summary = "Verify OTP")
    @PostMapping("/verify-OTP")
    public ResponseEntity<?> verifyOTP(String emailID, String OTP) {
        return emailSender.verifyEmailOTP(emailID, OTP);
    }

    @GetMapping("/login-google")
    public void redirectToGoogleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/showDetails")
    public ResponseEntity<?> showUserDetails(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        userService.createUserAccount(email);
        return ResponseEntity.ok(authentication.getPrincipal());
    }

}
