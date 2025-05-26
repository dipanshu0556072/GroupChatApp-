package com.GroupChatAppexample.GroupChat.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtGenerator
{
   @Value("${jwtToken.secretKey}")
   private String SECRET_KEY;

   @Value("${jwtToken.Validity}")
   private String tokenValidity;

   public String generateToken(String emailId,String role){
       return Jwts.builder()
               .setSubject(emailId)
               .claim("role",role)
               .setIssuedAt(new Date())
               .setExpiration(new Date(System.currentTimeMillis()+1000*60*60*60*24*30*12))
               .signWith(SignatureAlgorithm.HS256,jwtDecodedSignKey())
               .compact();
   }

   public Key jwtDecodedSignKey(){
       return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
   }

   public String extractUserRole(String token){
       Claims claims=Jwts.parser().setSigningKey(jwtDecodedSignKey()).parseClaimsJws(token).getBody();
       return claims.get("role",String.class);
   }


   //check isToken Expired
    public boolean isTokenExpired(String token){
       Claims claims=Jwts.parser().setSigningKey(jwtDecodedSignKey()).parseClaimsJws(token).getBody();
       return claims.getExpiration().before(new Date());
    }

    //extract userName from the token
    public String extractUserName(String token){
       return Jwts.parser().setSigningKey(jwtDecodedSignKey()).parseClaimsJws(token).getBody().getSubject();
    }

    //validate the token
    public boolean validateUserAndToken(String inputUserName, String token){
          Claims claims=Jwts.parser().setSigningKey(jwtDecodedSignKey()).parseClaimsJws(token).getBody();
          return claims.getSubject().equals(inputUserName) && !isTokenExpired(token);
    }




}
