package com.GroupChatAppexample.GroupChat.config;

import com.GroupChatAppexample.GroupChat.jwt.JwtGenerator;
import com.GroupChatAppexample.GroupChat.model.User;
import com.GroupChatAppexample.GroupChat.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtGenerator jwtGenerator;
    @Autowired
    private UserRepo userRepo;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connectWithSocket")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler wsHandler,
                                                      Map<String, Object> attributes) {

                        String query = request.getURI().getQuery(); // e.g. "token=eyJhbGciOiJIUzI1Ni..."
                        System.out.println("Query: " + query);

                        if (query != null && query.startsWith("token=")) {
                            String token = query.substring(6); // remove "token="
                            System.out.println("Token from query: " + token);
                            try {
                                String email = jwtGenerator.extractUserName(token);
                                User user=userRepo.findByEmailId(email).orElseThrow(()->new RuntimeException("no user with given emailId:"+email));
                                return () -> user.getId(); // ✅ should return userId as Principal
                            } catch (Exception e) {
                                System.out.println("❌ Error parsing token: " + e.getMessage());
                            }
                        }

                        return () -> "anonymous";
                    }

                })
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // No interceptor
    }
}
