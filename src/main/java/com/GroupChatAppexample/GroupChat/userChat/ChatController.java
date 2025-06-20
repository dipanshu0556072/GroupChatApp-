package com.GroupChatAppexample.GroupChat.userChat;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private final Map<String,List<ChatMessage>>chatHistory=new ConcurrentHashMap<>();

    @MessageMapping("/sendMessage")
    @SendTo("/topic/message")
    public ChatMessage sendMessageUser_A_to_B(ChatMessage message){
         String key=createKey(message.getFrom(),message.getTo());
         chatHistory.computeIfAbsent(key,k->new ArrayList<>()).add(message);
         return message;
    }

    private String createKey(String a,String b){
      List<String>user=Arrays.asList(a,b);
      Collections.sort(user);
      return user.get(0) + ":" + user.get(1);
    }

    @GetMapping("/chatHistory/{userA}/{userB}")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@PathVariable String userA,@PathVariable String userB){
        return chatHistory.getOrDefault(createKey(userA,userB),new ArrayList<>());
    }
}
