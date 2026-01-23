package com.example.V_RGUIDE.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.V_RGUIDE.model.Appointment;
import com.example.V_RGUIDE.model.Counsellor;
import com.example.V_RGUIDE.model.Student;
import com.example.V_RGUIDE.repository.AppointmentRepository;
import com.example.V_RGUIDE.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    // --- REGISTRATION & OTP SECTION ---

    @PostMapping("/register-student")
    public String registerStudent(@RequestBody Student student) {
        // We use registerWithOTP so the student gets a verification code
        return userService.registerWithOTP(student);
    }

    @PostMapping("/register-counsellor")
    public String enrollCounsellor(@RequestBody Counsellor counsellor) {
        // We use registerWithOTP so the counsellor gets a verification code
        // and stays in PENDING status
        return userService.registerWithOTP(counsellor);
    }

    @PostMapping("/verify-otp")
    public String verify(@RequestParam String email, @RequestParam String otp) {
        return userService.verifyOTP(email, otp);
    }

    // --- COUNSELLOR ACTIONS ---

    @PutMapping("/counsellor/post-schedule/{email}")
    public String postSchedule(@PathVariable String email, @RequestBody Map<String, List<String>> schedule) {
        return userService.postWeeklySchedule(email, schedule);
    }

    // --- SEARCH & FILTERING ---

    @GetMapping("/search-counsellors")
    public List<Counsellor> search(@RequestParam String specialization) {
        return userService.findCounsellorsBySpecialization(specialization);
    }

    @GetMapping("/counsellors/filter")
    public List<Counsellor> getCounsellorsBySpec(@RequestParam String specialization) {
        return userService.findBySpecialization(specialization);
    }

    // --- BOOKING & DASHBOARD ---

    @PostMapping("/book-session")
    public String bookSession(@RequestBody Map<String, String> details) {
        return userService.processBooking(
                details.get("studentEmail"),
                details.get("counsellorEmail"),
                details.get("appointmentDate"),
                details.get("timeSlot"));
    }

    @GetMapping("/student/my-appointments/{email}")
    public List<Appointment> getStudentDashboard(@PathVariable String email) {
        return appointmentRepository.findByStudentEmail(email);
    }

    @DeleteMapping("/appointments/cancel/{id}")
    public String cancelAppointment(@PathVariable String id) {
        return userService.cancelAppointment(id);
    }

    @PutMapping("/student/update-preferences/{email}")
    public String updatePreferences(@PathVariable String email, @RequestBody List<String> slots) {
        return userService.updateStudentPreferences(email, slots);
    }

    // --- ADMIN ACTIONS (Add these for your Demo) ---

    @PutMapping("/admin/approve/{email}")
    public String approveCounsellor(@PathVariable String email) {
        return userService.approveCounsellor(email);
    }

    @PutMapping("/admin/reject/{email}")
    public String rejectCounsellor(@PathVariable String email) {
        return userService.rejectCounsellor(email);
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        return userService.login(email, password);
    }
}