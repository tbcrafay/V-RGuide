package com.example.V_RGUIDE.repository;

import com.example.V_RGUIDE.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByCounsellorEmail(String email);
    List<Appointment> findByStudentEmail(String studentEmail);
}