package com.example.V_RGUIDE.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.V_RGUIDE.model.Appointment;
import com.example.V_RGUIDE.repository.AppointmentRepository;
import com.example.V_RGUIDE.repository.UserRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    public synchronized String bookAppointment(Appointment appointment) {
        // 1. NORMALIZE DATA (Software Construction: Input Sanitization)
        String targetDate = appointment.getAppointmentDate().trim();
        String targetSlot = appointment.getTimeSlot().trim();
        String studentEmail = appointment.getStudentEmail().trim();
        String counsellorEmail = appointment.getCounsellorEmail().trim();

        // 2. PREVENT SELF-BOOKING (HCI & Logic Safety)
        if (studentEmail.equalsIgnoreCase(counsellorEmail)) {
            return "Error: Protocol Violation. You cannot book a session with yourself.";
        }

        // 3. CHECK COUNSELLOR AVAILABILITY
        boolean counsellorBusy = appointmentRepository.findByCounsellorEmail(counsellorEmail)
                .stream()
                .anyMatch(a -> a.getAppointmentDate().equalsIgnoreCase(targetDate)
                        && a.getTimeSlot().equalsIgnoreCase(targetSlot)
                        && !"CANCELLED".equals(a.getStatus()));

        if (counsellorBusy) {
            return "Error: This slot is already booked for this counsellor!";
        }

        // 4. PREVENT STUDENT DOUBLE-BOOKING (The Deadlock Preventer)
        boolean studentBusy = appointmentRepository.findByStudentEmail(studentEmail)
                .stream()
                .anyMatch(a -> a.getAppointmentDate().equalsIgnoreCase(targetDate)
                        && a.getTimeSlot().equalsIgnoreCase(targetSlot)
                        && !"CANCELLED".equals(a.getStatus()));

        if (studentBusy) {
            System.out.println(
                    "[CONFLICT] Student " + studentEmail + " is already busy on " + targetDate + " at " + targetSlot);
            return "Error: Protocol Conflict! You already have another session scheduled for this time slot.";
        }

        appointmentRepository.save(appointment);
        return "Appointment secured successfully.";
    }
}