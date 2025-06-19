package com.GroupChatAppexample.GroupChat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.*;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableAsync
public class GroupChatApplication {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		SpringApplication.run(GroupChatApplication.class, args);
//		KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
//		keyGen.init(256); // Use 256-bit key
//		SecretKey secretKey = keyGen.generateKey();
//		String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
//
//		System.out.println("Generated Secret Key: " + encodedKey);



	}

}
