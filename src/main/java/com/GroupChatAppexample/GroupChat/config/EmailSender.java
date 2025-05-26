package com.GroupChatAppexample.GroupChat.config;

import com.GroupChatAppexample.GroupChat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Random;

@Component
public class EmailSender {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Lazy
    @Autowired
    private UserService userService;

    // Send OTP to the user's email ID
    public ResponseEntity<?> sendOTPtoEmailID(String emailID) {
        try {
            String normalizedEmail = emailID.trim().toLowerCase();

            Random random = new Random();
            int OTP = 100000 + random.nextInt(900000); // Generates 6-digit OTP

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(normalizedEmail);
            message.setSubject("Thank you for signing up: GroupChatApp");
            message.setText("Your OTP is: " + OTP);

            javaMailSender.send(message);

            String key = "OTP:" + normalizedEmail;
            redisTemplate.opsForValue().set(key, String.valueOf(OTP), Duration.ofMinutes(5));

            System.out.println("OTP stored in Redis for " + normalizedEmail + ": " + OTP);

            return ResponseEntity.status(HttpStatus.CREATED).body("OTP sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send OTP to " + emailID);
        }
    }

    // Verify OTP entered by the user
    public ResponseEntity<?> verifyEmailOTP(String emailID, String userEnteredOTP) {
        try {
            String normalizedEmail = emailID.trim().toLowerCase();
            String key = "OTP:" + normalizedEmail;

            String storedOTP = redisTemplate.opsForValue().get(key);

            System.out.println("Stored OTP for " + normalizedEmail + ": " + storedOTP);

            if (storedOTP == null || storedOTP.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("OTP has expired or is invalid. Please request a new one.");
            }

            if (!storedOTP.equals(userEnteredOTP)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP.");
            }

            redisTemplate.delete(key);
            userService.createUserAccount(normalizedEmail);

            return ResponseEntity.ok("OTP verified successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error verifying OTP for: " + emailID);
        }
    }
}
