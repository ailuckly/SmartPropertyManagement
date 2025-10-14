package com.example.propertymanagement;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordTest {

    @Test
    public void testPassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String rawPassword = "admin123";
        String encodedPassword1 = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhka";
        String encodedPassword2 = "$2a$10$qo06sIjgT3TtGKjop/KVOefbQKDxaxl7d7RhfB70UEAWuYKS9BBn.";
        
        System.out.println("Testing password: " + rawPassword);
        System.out.println();
        
        System.out.println("Hash 1: " + encodedPassword1);
        System.out.println("Hash 1 matches: " + encoder.matches(rawPassword, encodedPassword1));
        System.out.println();
        
        System.out.println("Hash 2: " + encodedPassword2);
        System.out.println("Hash 2 matches: " + encoder.matches(rawPassword, encodedPassword2));
        System.out.println();
        
        // Generate a new one
        String newEncoded = encoder.encode(rawPassword);
        System.out.println("Newly generated: " + newEncoded);
        System.out.println("New matches: " + encoder.matches(rawPassword, newEncoded));
    }
}
