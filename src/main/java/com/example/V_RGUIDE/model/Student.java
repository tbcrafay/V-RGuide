package com.example.V_RGUIDE.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Student extends User {
    private String studentType; // "COLLEGE" or "UNIVERSITY"
    private String major;

    public Student() {
        this.setRole("STUDENT");
    }
    // Student.java (or User.java)
private List<String> preferredSlots; // Example: ["Monday 10:00 AM", "Friday 02:00 PM"]

// Standard Getter and Setter (if not using @Data)
public List<String> getPreferredSlots() {
    return preferredSlots;
}

public void setPreferredSlots(List<String> preferredSlots) {
    this.preferredSlots = preferredSlots;
}
}