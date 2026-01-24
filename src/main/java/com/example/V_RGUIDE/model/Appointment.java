package com.example.V_RGUIDE.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appointments")
public class Appointment {

    @Id
    private String id;

    private String studentEmail;
    private String counsellorEmail;

    private String appointmentDate;
    private String timeSlot;

    private String status; // BOOKED, CANCELLED, COMPLETED

    private LocalDateTime createdAt = LocalDateTime.now();

    // Helper method remains fine
    public void setBookingDetails(String date, String slot) {
        this.appointmentDate = date;
        this.timeSlot = slot;
    }

    // DELETE the getEndTime() method entirely!

    private String meetingLink;

    // Add Getter and Setter
    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    // Add this field to Appointment.java
    private boolean adminViewed = false;
}