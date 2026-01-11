package com.example.V_RGUIDE.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Admin extends User {
    
    public Admin() {
        this.setRole("ADMIN");
    }
    // You can add admin-specific fields here later, like "adminLevel"

    @Override
    public void setPreferredSlots(List<String> slots) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPreferredSlots'");
    }
}