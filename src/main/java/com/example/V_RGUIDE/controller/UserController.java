package com.example.V_RGUIDE.controller;

import com.example.V_RGUIDE.model.Counsellor;
import com.example.V_RGUIDE.model.Student;
import com.example.V_RGUIDE.model.User;
import com.example.V_RGUIDE.service.UserService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController // Make sure this is here!
@RequestMapping("/api/users") // This is the base path
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register-student") // This matches the end of your URL
    public String registerStudent(@RequestBody Student student) {
        userService.registerUser(student);
        userService.sendWelcomeNotification(student.getEmail());
        return "Student registered successfully!";
    }
    // ADD THIS METHOD for the Counsellor

    
// Commentd by ARHAMMMMMMMMMMMMMMM bcuz the 'enrollCounsellor' method if it has the same @PostMapping
    

// @PostMapping("/register-counsellor")
    // public String registerCounsellor(@RequestBody Counsellor counsellor) {
    //     // By default, they are PENDING
    //     counsellor.setStatus("PENDING"); 
    //     userService.registerUser(counsellor);
    //     return "Counsellor registration submitted! Waiting for Admin approval.";
    // }
// New Endpoint to search for Counsellors by specialization
@GetMapping("/search-counsellors")
public List<Counsellor> search(@RequestParam String specialization) {
    return userService.findCounsellorsBySpecialization(specialization);
}
// UserController.java
//ARHAMMMMMMMMMMMMMMM
@PostMapping("/register-counsellor")
public User enrollCounsellor(@RequestBody Counsellor counsellor) {
    return userService.registerCounsellor(counsellor);
}
// UserController.java or CounsellorController.java

@PutMapping("/counsellor/post-schedule/{email}")
public String postSchedule(@PathVariable String email, @RequestBody Map<String, List<String>> schedule) {
    return userService.postWeeklySchedule(email, schedule);
}
// UserController.java

@DeleteMapping("/appointments/cancel/{id}")
public String cancelAppointment(@PathVariable String id) {
    // This calls the logic we wrote in the UserService
    return userService.cancelAppointment(id);
}
@PostMapping("/book-session") // Ensure this matches Postman exactly
    public String bookSession(@RequestBody Map<String, String> details) {
        return userService.processBooking(
            details.get("studentEmail"),
            details.get("counsellorEmail"),
            details.get("appointmentDate"),
            details.get("timeSlot")
        );
    }
    // UserController.java

@GetMapping("/counsellors/filter")
public List<Counsellor> getCounsellorsBySpec(@RequestParam String specialization) {
    return userService.findBySpecialization(specialization);
}
// UserController.java
@PutMapping("/student/update-preferences/{email}")
public String updatePreferences(@PathVariable String email, @RequestBody List<String> slots) {
    return userService.updateStudentPreferences(email, slots);
}
}