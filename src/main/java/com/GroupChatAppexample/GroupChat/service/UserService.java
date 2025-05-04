package com.GroupChatAppexample.GroupChat.service;

import com.GroupChatAppexample.GroupChat.config.EmailSender;
import com.GroupChatAppexample.GroupChat.model.User;
import com.GroupChatAppexample.GroupChat.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private UserRepo userRepo;

    // send OTP to the user
    public ResponseEntity<?> sendOTP(String emailID) {
        try {
            return emailSender.sendOTPtoEmailID(emailID);
        } catch (Exception e) {
            throw new RuntimeException("Getting some error in sending OTP to the user!");
        }
    }

    // create user account
    public ResponseEntity<?> createUserAccount(String emailID) {
        try {
            if (userRepo.findByEmailId(emailID).isPresent()) {
                return ResponseEntity.status(HttpStatus.IM_USED).body("User already exists with this emailID: " + emailID);
            }
            User user = new User();
            user.setEmailId(emailID);
            userRepo.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created in DB with emailID: " + emailID);
        } catch (Exception e) {
            throw new RuntimeException("Already existing user a/c with emailID: " + emailID);
        }
    }
}
