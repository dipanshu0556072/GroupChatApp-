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
public class EmailSender
{
  @Autowired
  private JavaMailSender javaMailSender;
  @Autowired
  private StringRedisTemplate redisTemplate;
  @Lazy
  @Autowired
  private UserService userService;

  //send OTP to the userEmailID
  public ResponseEntity<?> sendOTPtoEmailID(String emailID){
      try{
          Random random=new Random();
          int OTP = 100000+ random.nextInt(90000);
          SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
          simpleMailMessage.setTo(emailID);
          simpleMailMessage.setSubject("Thank you for signing up: GroupChatApp");
          simpleMailMessage.setText("Your OTP is: " + OTP);
          javaMailSender.send(simpleMailMessage);
          // Store OTP in Redis with 5-minute expiry
          String key = "OTP:" + emailID;
          redisTemplate.opsForValue().set(key,String.valueOf(OTP), Duration.ofMinutes(5L));
          return ResponseEntity.status(HttpStatus.CREATED).body("OTP sent successfully!");
      }catch (Exception e){
          throw new RuntimeException("Encountering error while sending OTP to:"+emailID);
      }
  }

    //verify userOTP
    public ResponseEntity<?>verifyEmailOTP(String emailID){
        try{
            String key = "OTP:" + emailID;
            String storedOTP = redisTemplate.opsForValue().get(key);
            // OTP not found or expired
            if(storedOTP==null){
              return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body("OTP has expired. Please request a new one.");
            }
            // Incorrect OTP
            if(!storedOTP.equals(storedOTP)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect OTP entered");
            }
            // Optionally delete OTP after successful verification
            redisTemplate.delete(key);
            userService.createUserAccount(emailID);
            return ResponseEntity.status(HttpStatus.OK).body("OTP verified successfully!");
        }catch(Exception e){ throw new RuntimeException("Encountering error while verifying OTP to:"+emailID);}
    }


}
