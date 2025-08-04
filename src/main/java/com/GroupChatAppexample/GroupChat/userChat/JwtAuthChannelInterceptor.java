// JwtAuthChannelInterceptor.java
package com.GroupChatAppexample.GroupChat.userChat;

import com.GroupChatAppexample.GroupChat.jwt.JwtGenerator;
import com.GroupChatAppexample.GroupChat.model.User;
import com.GroupChatAppexample.GroupChat.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

@Component
public class JwtAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtGenerator jwtTokenProvider;

    @Autowired
    private UserRepo userRepo;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtTokenProvider.extractUserName(token);

                User user=userRepo.findByEmailId(email).orElseThrow(()->new RuntimeException("no user with given emailId:"+email));

                if (email != null) {
                    Principal userPrincipal = ()-> user.getId();
                    accessor.setUser(userPrincipal);

                    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                    if (sessionAttributes != null) {
                        sessionAttributes.put("user", userPrincipal);
                    }

                    System.out.println("🔐 Connected principal: " + email.toLowerCase());
                }
            }
        }

        return message;
    }

    public JwtGenerator getJwtGenerator() {
        return jwtTokenProvider;
    }
}
