package com.example.V_RGUIDE.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Document(collection = "users")
public abstract class User {
    @Id
    
    @Setter(AccessLevel.PRIVATE) 
    private String id;
    
    private String name;
    @Indexed(unique = true)
    private String email;
    private String password;
    private String role;
    public abstract void setPreferredSlots(List<String> slots);
    private String otp;
    private boolean isVerified = false;
}