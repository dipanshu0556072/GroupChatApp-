package com.GroupChatAppexample.GroupChat.service;

import com.GroupChatAppexample.GroupChat.DTO.FriendMetaData;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
            user.setCreatedTime(LocalDateTime.now());
            user.setUpdatedTime(LocalDateTime.now());
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

            System.out.println("imageFile == null: " + (imageFile == null));
            System.out.println("imageFile isEmpty: " + (imageFile != null && imageFile.isEmpty()));

            if(imageFile!=null  && !imageFile.isEmpty()){

                // if user profile already exist
                String oldProfile=user.getProfilePicture();
                if(oldProfile!=null){
                    File oldDp=new File("."+oldProfile);
                    if(oldDp.exists()){
                        oldDp.delete();
                    }
                }
                System.out.printf("Image size (MB): %.2f MB%n", imageFile.getSize()/(1024.0*1024.0));
                if(!imageFile.getContentType().startsWith("image/")) {
                    return ResponseEntity.badRequest().body("Only image files allowed");
                }

                //create folder
                File uploadDirectory=new File("uploads/profileImages/");
                if(!uploadDirectory.exists()){
                    uploadDirectory.mkdirs();
                }

                //now create unique file name
                String fileName="profile_"+user.getId()+"_"+System.currentTimeMillis()+".jpg";
                File destinationPath=new File(uploadDirectory,fileName);
                System.out.println("Working Dir: " + System.getProperty("user.dir"));
                System.out.println("Destination Path: " + destinationPath.getAbsolutePath());
                try (InputStream inputStream = imageFile.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(destinationPath)) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                user.setProfilePicture("/uploads/profileImages/" + fileName);
            }


            Optional<User>userExitinuser=userRepo.findByUserName(userDTO.getUserName());
            if(userExitinuser.isPresent() && !userExitinuser.get().getEmailId().equalsIgnoreCase(emailId)){
                throw new RuntimeException("This userName name is already used by someone else!");
            }


            user.setUserName(userDTO.getUserName());
            user.setFullName(userDTO.getFullName());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setBio(userDTO.getBio());
            user.setUpdatedTime(LocalDateTime.now());

            userRepo.save(user);

            return ResponseEntity.ok("Profile picture updated successfully");


        }catch (Exception e){
            throw  new RuntimeException(e.getMessage());
        }
    }

    //helper function for friendRequest status
    public FriendMetaData findByUserId(Set<FriendMetaData>userFrom,String toId){
        return userFrom.stream().filter(u->u.getUserId().equals(toId)).findFirst().orElse(null);
    }

    //send Request
    public ResponseEntity<?> sendRequest(String fromId, String toId){
        try{
            if(fromId.equals(toId)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not send request to yourself.");
            }
            User userFrom=userRepo.findById(fromId).orElseThrow(()->new RuntimeException("Sender not found!"));
            User userTo=userRepo.findById(toId).orElseThrow(()->new RuntimeException("Receiver not found!"));

            if(findByUserId(userFrom.getSentRequest(),toId)!=null){
                return declineRequest(fromId,toId);
            }
            if(findByUserId(userFrom.getInboxRequest(),toId)!=null){
                return acceptRequest(toId,fromId);
            }
            //sender
            FriendMetaData f1=new FriendMetaData();
            f1.setUserId(toId);
            f1.setUserName(userTo.getUserName());
            f1.setStatus("sent");
            f1.setTimeStamp(LocalDateTime.now());
            //receiver
            FriendMetaData f2=new FriendMetaData();
            f2.setUserId(fromId);
            f2.setUserName(userFrom.getUserName());
            f2.setStatus("inbox");
            f2.setTimeStamp(LocalDateTime.now());

            userFrom.getSentRequest().add(f1);
            userTo.getInboxRequest().add(f2);

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

            if(findByUserId(userFrom.getFriendList(),toId)!=null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("friendRequest already accepted!");
            }
            //sender
            FriendMetaData f1=findByUserId(userFrom.getSentRequest(),toId);
            FriendMetaData f2=findByUserId(userTo.getInboxRequest(),fromId);

            if(f1!=null && f2!=null){
                userFrom.getSentRequest().remove(f1);
                userTo.getInboxRequest().remove(f2);
                //add in friend-list
                FriendMetaData f3=new FriendMetaData();
                FriendMetaData f4=new FriendMetaData();

                f3.setUserId(toId);
                f3.setUserName(userTo.getUserName());
                f3.setStatus("friend");
                f3.setTimeStamp(LocalDateTime.now());
                userFrom.getFriendList().add(f3);

                f4.setUserId(fromId);
                f4.setUserName(userFrom.getUserName());
                f4.setStatus("friend");
                f4.setTimeStamp(LocalDateTime.now());
                userTo.getFriendList().add(f4);
            }

            userRepo.save(userFrom);
            userRepo.save(userTo);
            return ResponseEntity.status(HttpStatus.CREATED).body("Friend request accepted!");
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

            //remove from sent friendRequest of sender
            FriendMetaData f1=findByUserId(userFrom.getSentRequest(),toId);
            FriendMetaData f2=findByUserId(userTo.getInboxRequest(),fromId);

            if(f1!=null && f2!=null){
                userFrom.getSentRequest().remove(f1);
                userTo.getInboxRequest().remove(f2);
            }
            userRepo.save(userFrom);
            userRepo.save(userTo);
            return ResponseEntity.status(HttpStatus.CREATED).body("Friend request declined");
        }catch (Exception e){
            throw new RuntimeException("Encountering error while declining request:"+e.getMessage());
        }
    }
    //fetch sendRequest
    public ResponseEntity<?>fetchSentRequest(String userId){
        try{
            User userFrom=userRepo.findById(userId).orElseThrow(()->new RuntimeException("userId not found!"));
            return ResponseEntity.status(HttpStatus.OK).body(userFrom.getSentRequest());
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
            return null;
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

    //friend suggestion to user
    public List<Map<String, String>> friendSuggestList(String userId) {
        try {
            User currentUser = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found!"));

            List<User> allUsers = userRepo.findAll();
            allUsers.remove(currentUser);

            return allUsers.stream().map(user -> {
                Map<String, String> mp = new HashMap<>();
                mp.put("userId", user.getId());
                mp.put("userName", user.getUserName());
                mp.put("fullName", user.getFullName());
                mp.put("profilePictureUrl", "/images/profile/" + user.getId());
                String status;
                if(findByUserId(currentUser.getFriendList(),user.getId())!=null){
                    status="friend";
                }else if(findByUserId(currentUser.getSentRequest(),user.getId())!=null){
                    status="requested";
                }else{
                    status="follow";
                }
                mp.put("status",status);
                return mp;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error while showing suggestions: " + e.getMessage());
        }
    }

    //
    public List<Map<String, String>> fetchUserByUserId(String userId){
        try{
            User user=userRepo.findById(userId).orElseThrow(()->new RuntimeException("userId not found!"));
            Map<String, String> mp = new HashMap<>();
            mp.put("userId", user.getId());
            mp.put("userName", user.getUserName());
            mp.put("fullName", user.getFullName());
            mp.put("profilePictureUrl", "/images/profile/" + user.getId());
            // ✅ Wrap map into a list
            List<Map<String, String>> responseList = new ArrayList<>();
            responseList.add(mp);

            return responseList;
        }catch (Exception e){
            throw  new RuntimeException("encountering error in fetching user by userId:"+e.getMessage());
        }
    }

    // Fetch other user's data with their friendship status relative to the current user
    public List<Map<String,String>> checkFriendShipStatus(String loginInUserId,String otherUserId){
        try{
            User user1=userRepo.findById(loginInUserId).orElseThrow(()->new RuntimeException("userId not found!"));
            User user2=userRepo.findById(otherUserId).orElseThrow(()->new RuntimeException("userId not found!"));

            Map<String, String> mp = new HashMap<>();
            mp.put("userId", user2.getId());
            mp.put("userName", user2.getUserName());
            mp.put("fullName", user2.getFullName());
            mp.put("profilePictureUrl", "/images/profile/" + user2.getId());
            mp.put("bio", user2.getBio());
            String status;
            if(findByUserId(user1.getFriendList(),otherUserId)!=null){
                status="friend";
            }else if(findByUserId(user1.getSentRequest(),otherUserId)!=null){
                status="requested";
            }else{
                status="follow";
            }
            mp.put("status",status);
            // ✅ Wrap map into a list
            List<Map<String, String>> responseList = new ArrayList<>();
            responseList.add(mp);
            return responseList;
        }catch (Exception e){
            throw  new RuntimeException("encountering error in fetching user by userId:"+e.getMessage());
        }
    }




}
