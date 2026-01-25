package com.example.V_RGUIDE.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.V_RGUIDE.model.Appointment;
import com.example.V_RGUIDE.repository.AppointmentRepository;
import com.example.V_RGUIDE.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Endpoint to book a session
    @PostMapping("/book")
    public String book(@RequestBody Appointment appointment) {
        return appointmentService.bookAppointment(appointment);
    }

    /**
     * NEW: Update Appointment Status (Fixes the "Clear Record" error)
     * This handles the PUT request from your StudentAppointments.jsx
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable String id, 
            @RequestParam String newStatus) {
        
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setStatus(newStatus);
            appointmentRepository.save(appointment);
            return ResponseEntity.ok("Protocol Updated: Status is now " + newStatus);
        }).orElse(ResponseEntity.notFound().build());
    }
}