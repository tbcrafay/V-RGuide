package com.example.V_RGUIDE.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Counsellor extends User {
    private String specialization; // e.g., "Career", "Mental Health"
    private List<String> availableSlots;
    private String status = "PENDING";

    public Counsellor() {
        this.setRole("COUNSELLOR");
        this.setStatus("PENDING");
    }

// ARHAM NE AVAILABILITY KA MAP LGAYA HAIN YAHAN
 // Counsellor.java

// Change from List<String> to this:
private Map<String, List<String>> weeklySchedule = new HashMap<>();

// Getter and Setter
public Map<String, List<String>> getWeeklySchedule() {
    return weeklySchedule;
}

public void setWeeklySchedule(Map<String, List<String>> weeklySchedule) {
    this.weeklySchedule = weeklySchedule;
}

@Override
public void setPreferredSlots(List<String> slots) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setPreferredSlots'");
}
}