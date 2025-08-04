package com.GroupChatAppexample.GroupChat.userChat;

import com.GroupChatAppexample.GroupChat.DTO.SentMessageStatus;
import com.GroupChatAppexample.GroupChat.service.PersonalChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PersonalChatService personalChatService;

    @MessageMapping("/sendMessage")
    public void sendMessage(ChatMessage message, Principal principal) {
        if (principal == null) {
            System.out.println("Principal is null");
            return;
        }

        String sender = principal.getName();
        message.setFrom(sender);
        message.setTimestamp(System.currentTimeMillis());

        System.out.println("Sender: " + sender);
        System.out.println("Message to: " + message.getTo());

        messagingTemplate.convertAndSendToUser(message.getTo().toLowerCase(), "/queue/messages", message);
    }


    //update delivered msg status
    @MessageMapping("/messageSeen")
    public void messageSeen(SentMessageStatus event) {
        personalChatService.deliveredMsgStatus(event.getFromId(), event.getToId(), event.getId());
    }



}
