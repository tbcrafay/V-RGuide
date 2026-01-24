package com.example.V_RGUIDE.controller;

import com.example.V_RGUIDE.model.Counsellor;
import com.example.V_RGUIDE.model.Student;
import com.example.V_RGUIDE.repository.UserRepository;
import com.example.V_RGUIDE.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // Endpoint for Admin to see the statistics
    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        return userService.getAdminStats();
    }

    // Endpoint for Admin to approve a counsellor
    @PutMapping("/approve/{email}")
    public String approve(@PathVariable String email) {
        return userService.approveCounsellor(email);
    }

    // Admin can delete any user (CRUD operation)
    @DeleteMapping("/delete/{id}")
    public String deleteUser(@PathVariable String id) {
        // You can implement userRepository.deleteById(id) in service
        return "User deleted successfully";
    }

    // AdminController.java
    @GetMapping("/report")
    public List<String> getFullReport() {
        return userService.getBookingReport();
    }
    // AdminController.java


// ARHAMMMMMMMMMMMMMMMMMMMMMMM


// Get a full list of all registered students
    @GetMapping("/students")
    public List<Student> viewAllStudents() {
        return userService.getAllStudents();
}

// Get specific info based on student type
    @GetMapping("/students/type/{type}")
    public List<Student> viewStudentsByType(@PathVariable String type) {
        return userService.getStudentsByType(type);
}
// Admin can delete a student or counsellor by their ID
    @DeleteMapping("/remove/{id}")
    public String removeUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return "User with ID " + id + " has been successfully removed from the system.";
}
// AdminController.java

// 1. Get all counsellors and their details
@GetMapping("/counsellors")
public List<Counsellor> viewAllCounsellors() {
    return userService.getAllCounsellors();
}

// 2. Get counsellors based on status (PENDING or APPROVED)
@GetMapping("/counsellors/status/{status}")
public List<Counsellor> viewCounsellorsByStatus(@PathVariable String status) {
    return userService.getCounsellorsByStatus(status);
}
// AdminController.java
// ARHAMMMMMMMMMMMMMMMMMMMMMMMMM
// @DeleteMapping("/remove/{id}")
// public String removeEntity(@PathVariable String id) {
//     return userService.deleteUserById(id);
// }
// AdminController.java

// Rejection Path
@PutMapping("/reject-counsellor/{email}")
public String reject(@PathVariable String email) {
    return userService.rejectCounsellor(email);
}
//@Autowired
    //private UserService userService;

  //  @Autowired // This belongs here, above the field
    //private UserService userService;

    // DO NOT put @Autowired here!
    @PutMapping("/approve-counsellor/{email}")
    public String approveCounsellor(@PathVariable String email) {
        return userService.approveCounsellor(email);
    }
}