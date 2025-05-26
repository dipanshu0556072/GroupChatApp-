package com.GroupChatAppexample.GroupChat.jwt;

import com.GroupChatAppexample.GroupChat.model.User;
import com.GroupChatAppexample.GroupChat.repo.UserRepo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtValidate extends OncePerRequestFilter {

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private UserRepo userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtGenerator.extractUserName(token);
            String role = jwtGenerator.extractUserRole(token);

            User user = userRepo.findByEmailId(email).orElse(null);

            //refresh the token
            if (user != null && jwtGenerator.isTokenExpired(token)) {
                String newToken = jwtGenerator.generateToken(email, role);
                user.setActiveToken(newToken);
                userRepo.save(user);
                response.setHeader("Authorization", "Bearer " + newToken);
                token = newToken;
            }

            if (user != null && token.equals(user.getActiveToken())) {
                if (jwtGenerator.validateUserAndToken(email, token)) {
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        List<GrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + role));
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(email, null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
