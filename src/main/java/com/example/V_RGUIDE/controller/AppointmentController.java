package com.example.V_RGUIDE.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.V_RGUIDE.model.Appointment;
import com.example.V_RGUIDE.service.AppointmentService;

// import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:5173")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // Endpoint to book a session
    @PostMapping("/book")
    public String book(@RequestBody Appointment appointment) {
        // This triggers the synchronized logic to prevent double-booking
        return appointmentService.bookAppointment(appointment);
    }
}