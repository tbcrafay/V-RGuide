package com.example.V_RGUIDE.service;

import com.example.V_RGUIDE.model.Appointment;
import com.example.V_RGUIDE.model.Counsellor;
import com.example.V_RGUIDE.model.Student;
import com.example.V_RGUIDE.model.User;
import com.example.V_RGUIDE.repository.UserRepository;
import com.example.V_RGUIDE.repository.AppointmentRepository;
import com.example.V_RGUIDE.exception.UserNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;

    // Standard Logic: Saving a user
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    // Concurrency: This runs on a SEPARATE thread!
    @Async
    public void sendWelcomeNotification(String email) {
        System.out.println(
                "Threading: Sending notification to " + email + " on thread: " + Thread.currentThread().getName());
        // In a real app, this is where your email sending logic would go
    }

    public User findUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User with email " + email + " not found!");
        }
        return user;
    }

    // Admin Method: Approve a Counsellor
    public String approveCounsellor(String email) {
        User user = userRepository.findByEmail(email);
        if (user instanceof Counsellor) {
            ((Counsellor) user).setStatus("APPROVED");
            userRepository.save(user);

            // This is where you trigger the Threaded Notification!
            sendWelcomeNotification(email);
            return "Counsellor approved and notified.";
        }
        throw new UserNotFoundException("Counsellor not found.");
    }

    public Map<String, Long> getAdminStats() {
        List<User> allUsers = userRepository.findAll();

        // Student counts
        long collegeStudents = allUsers.stream()
                .filter(u -> u instanceof Student && "COLLEGE".equalsIgnoreCase(((Student) u).getStudentType()))
                .count();

        long universityStudents = allUsers.stream()
                .filter(u -> u instanceof Student && "UNIVERSITY".equalsIgnoreCase(((Student) u).getStudentType()))
                .count();

        // Counsellor counts by Specialization
        long mentalHealthCounsellors = allUsers.stream()
                .filter(u -> u instanceof Counsellor
                        && "Mental Health".equalsIgnoreCase(((Counsellor) u).getSpecialization()))
                .count();

        long careerCounsellors = allUsers.stream()
                .filter(u -> u instanceof Counsellor
                        && "Career Guidance".equalsIgnoreCase(((Counsellor) u).getSpecialization()))
                .count();

        // Putting it all in the Map (Proper Format)
        Map<String, Long> stats = new HashMap<>();
        stats.put("CollegeStudents", collegeStudents);
        stats.put("UniversityStudents", universityStudents);
        stats.put("MentalHealthCounsellors", mentalHealthCounsellors);
        stats.put("CareerCounsellors", careerCounsellors);

        return stats;
    }

    // Method to find specific counsellors (Demonstrates Filter Logic)
    public List<Counsellor> findCounsellorsBySpecialization(String specialization) {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Counsellor)
                .map(u -> (Counsellor) u)
                .filter(c -> "APPROVED".equals(c.getStatus())) // Only show approved ones
                .filter(c -> c.getSpecialization().equalsIgnoreCase(specialization))
                .toList();
    }

    // UserService.java
    public List<String> getBookingReport() {
        // This demonstrates joining information from different sources (ADTs)
        return appointmentRepository.findAll().stream()
                .map(app -> "Student [" + app.getStudentEmail() + "] has a session with ["
                        + app.getCounsellorEmail() + "] at " + app.getStatus())
                .toList();
    }
    // UserService.java

// ARHAMMMMMMMMMMMMMMMMMM

// Admin Method: Get all students and their detailed info
    public List<Student> getAllStudents() {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .toList();
    }

// Admin Method: Get students filtered by their type (College vs University)
    public List<Student> getStudentsByType(String type) {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Student && type.equalsIgnoreCase(((Student) u).getStudentType()))
                .map(u -> (Student) u)
                .toList();
}
//ARHAMMMMMMMMM
    // public List<Counsellor> getAllCounsellors() {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'getAllCounsellors'");
    // }

    public void deleteUser(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }
    // UserService.java

// Admin Method: Get all counsellors (both PENDING and APPROVED)
public List<Counsellor> getAllCounsellors() {
    return userRepository.findAll().stream()
            .filter(u -> u instanceof Counsellor)
            .map(u -> (Counsellor) u)
            .toList();
}

// Admin Method: Filter counsellors by status (e.g., to see who needs approval)
public List<Counsellor> getCounsellorsByStatus(String status) {
    return userRepository.findAll().stream()
            .filter(u -> u instanceof Counsellor && status.equalsIgnoreCase(((Counsellor) u).getStatus()))
            .map(u -> (Counsellor) u)
            .toList();
}
// UserService.java

// Admin Method: Remove a user (Student or Counsellor) by ID
public String deleteUserById(String id) {
    if (userRepository.existsById(id)) {
        userRepository.deleteById(id);
        return "User with ID " + id + " has been successfully removed.";
    } else {
        // This triggers your custom exception if the ID is wrong
        throw new UserNotFoundException("Delete failed: User ID " + id + " does not exist.");
    }
}
// UserService.java

public String rejectCounsellor(String email) {
    User user = userRepository.findByEmail(email);
    if (user instanceof Counsellor) {
        ((Counsellor) user).setStatus("REJECTED");
        userRepository.save(user);
        return "Counsellor application for " + email + " has been REJECTED.";
    }
    throw new UserNotFoundException("Counsellor not found.");
}
// UserService.java

public User registerCounsellor(Counsellor counsellor) {
    // Check if user already exists
    if (userRepository.findByEmail(counsellor.getEmail()) != null) {
        throw new RuntimeException("Email already registered!");
    }
    
    // Ensure the role and status are strictly set
    counsellor.setRole("COUNSELLOR");
    counsellor.setStatus("PENDING");
    
    return userRepository.save(counsellor);
}
// UserService.java

public String postWeeklySchedule(String email, Map<String, List<String>> newSchedule) {
    User user = userRepository.findByEmail(email);
    
    if (user instanceof Counsellor counsellor) {
        // This takes your JSON and saves it directly to the counsellor's profile
        counsellor.setWeeklySchedule(newSchedule);
        userRepository.save(counsellor);
        return "Weekly schedule posted successfully!";
    }
    return "Counsellor not found.";
}
// UserService.java

@Async // This runs the notification in a separate concurrency thread
public void notifyStudent(String studentEmail, String details) {
    try {
        Thread.sleep(2000); // Simulating email delay
        System.out.println("[ASYNC THREAD] Notification sent to " + studentEmail + ": " + details);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

public String cancelAppointment(String appointmentId) {
    Optional<Appointment> apptOpt = appointmentRepository.findById(appointmentId);
    
    if (apptOpt.isPresent()) {
        Appointment appt = apptOpt.get();
        
        // 1. Restore the slot to the Counsellor
        User user = userRepository.findByEmail(appt.getCounsellorEmail());
        if (user instanceof Counsellor counsellor) {
            Map<String, List<String>> schedule = counsellor.getWeeklySchedule();
            String day = appt.getAppointmentDate();
            
            // Add the slot back to the list for that day
            schedule.computeIfAbsent(day, k -> new ArrayList<>()).add(appt.getTimeSlot());
            userRepository.save(counsellor);
        }

        // 2. Trigger the Notification (Asynchronous)
        notifyStudent(appt.getStudentEmail(), "Your appointment on " + appt.getAppointmentDate() + " has been cancelled.");

        // 3. Delete the record
        appointmentRepository.deleteById(appointmentId);
        
        return "Appointment cancelled and slot restored.";
    }
    return "Appointment not found.";
}

// public String processBooking(String string, String string2, String string3, String string4) {
//     // TODO Auto-generated method stub
//     throw new UnsupportedOperationException("Unimplemented method 'processBooking'");
// }
// UserService.java

public synchronized String processBooking(String student, String counsellorEmail, String day, String slot) {
    User user = userRepository.findByEmail(counsellorEmail);
    
    if (user instanceof Counsellor counsellor) {
        Map<String, List<String>> schedule = counsellor.getWeeklySchedule();
        
        if (schedule != null && schedule.containsKey(day) && schedule.get(day).contains(slot)) {
            
            // 1. Remove the slot from the counsellor
            schedule.get(day).remove(slot);
            userRepository.save(counsellor);

            // 2. Create the Appointment Object
            Appointment appt = new Appointment();
            appt.setStudentEmail(student);
            appt.setCounsellorEmail(counsellorEmail);
            appt.setAppointmentDate(day);
            appt.setTimeSlot(slot);
            appt.setStatus("BOOKED");

            // 3. Save to database
            appointmentRepository.save(appt);
            
            return "Success: Appointment booked for " + day + " at " + slot;
        }
    }
    return "Error: Slot not available.";
}
// UserService.java

public List<Counsellor> findBySpecialization(String spec) {
    List<User> allUsers = userRepository.findAll();
    List<Counsellor> results = new ArrayList<>();

    for (User user : allUsers) {
        if (user instanceof Counsellor counsellor) {
            // Case-insensitive check (e.g., "mental health" matches "Mental Health")
            if (counsellor.getSpecialization() != null && 
                counsellor.getSpecialization().equalsIgnoreCase(spec)) {
                results.add(counsellor);
            }
        }
    }
    return results;
}

    // @GetMapping("/student/my-appointments/{email}")
    // public List<Appointment> getStudentDashboard(@PathVariable String email) {
    //     // Now this will not be null
    //     return appointmentRepository.findByStudentEmail(email);
    // }
    // UserService.java
public String updateStudentPreferences(String email, List<String> slots) {
    User user = userRepository.findByEmail(email);
    if (user != null) {
        user.setPreferredSlots(slots);
        userRepository.save(user);
        return "Preferences updated successfully!";
    }
    return "User not found.";
}
}
