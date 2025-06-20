package com.GroupChatAppexample.GroupChat.service;

import com.GroupChatAppexample.GroupChat.DTO.UserDTO;
import com.GroupChatAppexample.GroupChat.config.EmailSender;
import com.GroupChatAppexample.GroupChat.config.Roles;
import com.GroupChatAppexample.GroupChat.model.User;
import com.GroupChatAppexample.GroupChat.repo.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Optional;

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

            User user = User.builder()
                    .emailId(emailID)
                    .userRole(Roles.User.toString())
                    .build();

            userRepo.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created in DB with emailID: " + emailID);
        } catch (Exception e) {
            throw new RuntimeException("Already existing user a/c with emailID: " + emailID);
        }
    }

    //update profile
    public ResponseEntity<?> updateUserProfile (String userJSON, String emailId, MultipartFile imageFile)
    {
     try{
           User user=userRepo.findByEmailId(emailId).orElseThrow(()->new RuntimeException("User not found with given emailId:"+emailId));
           ObjectMapper objectMapper=new ObjectMapper();
           UserDTO userDTO=objectMapper.readValue(userJSON,UserDTO.class);
           System.out.printf("Image size (MB): %.2f MB%n", imageFile.getSize()/(1024.0*1024.0));

           if(!imageFile.getContentType().startsWith("image/")) {
               return ResponseEntity.badRequest().body("Only image files allowed");
           }

           String imgInBase64= Base64.getEncoder().encodeToString(imageFile.getBytes());

           Optional<User>userExitinuser=userRepo.findByUserName(userDTO.getUserName());
           if(userExitinuser.isPresent() && !userExitinuser.get().getEmailId().equalsIgnoreCase(emailId)){
             throw new RuntimeException("This userName name is already used by someone else!");
           }

           user.setProfilePicture(imgInBase64);
         user.setUserName(userDTO.getUserName());
         user.setFullName(userDTO.getFullName());
         user.setPhoneNumber(userDTO.getPhoneNumber());
         user.setBio(userDTO.getBio());

         userRepo.save(user);

         return ResponseEntity.ok("Profile picture updated successfully");


     }catch (Exception e){
         throw  new RuntimeException(e.getMessage());
     }
    }

    //send Request
    public ResponseEntity<?> sendRequest(String fromId, String toId){
         try{
            if(fromId.equals(toId)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not send request to yourself.");
            }
           User userFrom=userRepo.findById(fromId).orElseThrow(()->new RuntimeException("Sender not found!"));
             User userTo=userRepo.findById(toId).orElseThrow(()->new RuntimeException("Receiver not found!"));

             userFrom.getSendRequest().add(toId);
             userTo.getInboxRequest().add(fromId);
             userRepo.save(userFrom);
             userRepo.save(userTo);
             return ResponseEntity.status(HttpStatus.CREATED).body("Friend request sent");
         }catch (Exception e){
             throw new RuntimeException("Encountering error while sending request:"+e.getMessage());
         }
    }

    //accept request
    public ResponseEntity<?> acceptRequest(String fromId, String toId){
        try{
            if(fromId.equals(toId)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not accept request to yourself.");
            }
            User userFrom=userRepo.findById(fromId).orElseThrow(()->new RuntimeException("Sender not found!"));
            User userTo=userRepo.findById(toId).orElseThrow(()->new RuntimeException("Receiver not found!"));

            userFrom.getFriendList().add(toId);
            userTo.getFriendList().add(fromId);
            userRepo.save(userFrom);
            userRepo.save(userTo);
            return ResponseEntity.status(HttpStatus.CREATED).body("Friend request sent");
        }catch (Exception e){
            throw new RuntimeException("Encountering error while accepting request:"+e.getMessage());
        }
    }
    //decline request
    public ResponseEntity<?> declineRequest(String fromId, String toId){
        try{
            if(fromId.equals(toId)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not decline request to yourself.");
            }
            User userFrom=userRepo.findById(fromId).orElseThrow(()->new RuntimeException("Sender not found!"));
            User userTo=userRepo.findById(toId).orElseThrow(()->new RuntimeException("Receiver not found!"));


            userFrom.getSendRequest().remove(toId);
            userTo.getInboxRequest().remove(fromId);
            userRepo.save(userFrom);
            userRepo.save(userTo);
            return ResponseEntity.status(HttpStatus.CREATED).body("Friend request declined");
        }catch (Exception e){
            throw new RuntimeException("Encountering error while declining request:"+e.getMessage());
        }
    }
    //fetch sendRequest
    public ResponseEntity<?>fetchSendRequest(String userId){
        try{
            User userFrom=userRepo.findById(userId).orElseThrow(()->new RuntimeException("userId not found!"));
            return ResponseEntity.status(HttpStatus.OK).body(userFrom.getSendRequest());
        }catch (Exception e){
            throw new RuntimeException("Encountering error while fetching send request:"+e.getMessage());
        }
    }
    //fetch receivedRequest
    public ResponseEntity<?>fetchReceivedRequest(String userId){
        try{
            User userFrom=userRepo.findById(userId).orElseThrow(()->new RuntimeException("userId not found!"));
            return ResponseEntity.status(HttpStatus.OK).body(userFrom.getInboxRequest());
        }catch (Exception e){
            throw new RuntimeException("Encountering error while fetching received request:"+e.getMessage());
        }
    }
    //fetch declinedRequest
    public ResponseEntity<?>fetchDeclinedRequest(String userId){
        try{
            User userFrom=userRepo.findById(userId).orElseThrow(()->new RuntimeException("userId not found!"));
            return ResponseEntity.status(HttpStatus.OK).body(userFrom.getDeclinedRequest());
        }catch (Exception e){
            throw new RuntimeException("Encountering error while fetching declined request:"+e.getMessage());
        }
    }
    //fetch friendsList
    public ResponseEntity<?>fetchFriendsList(String userId){
        try{
            User userFrom=userRepo.findById(userId).orElseThrow(()->new RuntimeException("userId not found!"));
            return ResponseEntity.status(HttpStatus.OK).body(userFrom.getFriendList());
        }catch (Exception e){
            throw new RuntimeException("Encountering error while fetching friendsList:"+e.getMessage());
        }
    }


}
