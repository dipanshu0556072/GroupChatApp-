package com.GroupChatAppexample.GroupChat.controller;

import com.GroupChatAppexample.GroupChat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/user")
public class UserController
{
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    //send OTP to the user
    @Operation(summary = "send OTP to the user")
    @PostMapping("/send_email_OTP")
    public ResponseEntity<?>sendOTPtoEmailID(@RequestParam(required = true) String emailID){
        logger.info("send_email_OTP");
        try{
            return userService.sendOTP(emailID);
        }catch (Exception e){
            throw new RuntimeException("Getting some error in sending OTP to the user!");
        }
    }
}
