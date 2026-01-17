package com.example.V_RGUIDE.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.V_RGUIDE.model.Appointment;
import com.example.V_RGUIDE.model.Counsellor;
import com.example.V_RGUIDE.model.User;
import com.example.V_RGUIDE.repository.AppointmentRepository;
import com.example.V_RGUIDE.repository.UserRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    // Use this for simple booking without notifications if needed
    public synchronized String bookAppointment(Appointment appointment) {
        User counsellor = userRepository.findByEmail(appointment.getCounsellorEmail());

        if (counsellor instanceof Counsellor && !"APPROVED".equals(((Counsellor) counsellor).getStatus())) {
            return "Error: This counsellor is not yet approved by Admin!";
        }

        boolean exists = appointmentRepository.findByCounsellorEmail(appointment.getCounsellorEmail())
                .stream()
                .anyMatch(a -> a.getAppointmentDate().equals(appointment.getAppointmentDate())
                        && a.getTimeSlot().equals(appointment.getTimeSlot()));

        if (exists) {
            return "Error: This slot is already booked!";
        }

        // We removed the sendBookingEmail error from here.
        appointmentRepository.save(appointment);
        return "Appointment saved successfully.";
    }
}