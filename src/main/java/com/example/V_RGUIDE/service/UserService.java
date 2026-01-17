package com.example.V_RGUIDE.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.V_RGUIDE.exception.UserNotFoundException;
import com.example.V_RGUIDE.model.Appointment;
import com.example.V_RGUIDE.model.Counsellor;
import com.example.V_RGUIDE.model.Student;
import com.example.V_RGUIDE.model.User;
import com.example.V_RGUIDE.repository.AppointmentRepository;
import com.example.V_RGUIDE.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private EmailService emailService;

    // Standard Logic: Saving a user
    public String registerWithOTP(User user) {
        String otp = String.valueOf((int) (Math.random() * 9000) + 1000);
        user.setOtp(otp);
        user.setVerified(false);
        userRepository.save(user);

        emailService.sendEmail(user.getEmail(), "V-RGUIDE Verification Code",
                "Your verification code is: " + otp);

        return "OTP sent to your email. Please verify to complete registration.";
    }

    public String verifyOTP(String email, String submittedOtp) {
        User user = userRepository.findByEmail(email);

        if (user != null) {
            // Force the stored OTP to be a String for comparison
            String storedOtp = String.valueOf(user.getOtp());

            if (storedOtp.equals(submittedOtp)) {
                user.setVerified(true);
                user.setOtp(null); // Optional: Clear OTP after successful verification
                userRepository.save(user);
                return "Email verified successfully! You can now log in.";
            }
        }
        return "Invalid OTP.";
    }

    // Concurrency: This runs on a SEPARATE thread!
    @Async
    public void sendWelcomeNotification(String email) {
        System.out.println(
                "Threading: Sending notification to " + email + " on thread: " + Thread.currentThread().getName());

        try {
            // We call the real email service here
            emailService.sendEmail(
                    email,
                    "Welcome to V-RGUIDE!",
                    "Thank you for registering. Your account has been created successfully.");
        } catch (Exception e) {
            System.err.println("Async Email Error: " + e.getMessage());
        }
    }

    public User findUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User with email " + email + " not found!");
        }
        return user;
    }

    // For Counsellor Approval
    public String approveCounsellor(String email) {
        User user = userRepository.findByEmail(email);

        if (user instanceof Counsellor counsellor) {
            counsellor.setStatus("APPROVED");
            userRepository.save(counsellor);

            // This triggers the real email notification
            emailService.sendEmail(
                    email,
                    "V-RGUIDE: Application Approved!",
                    "Congratulations " + counsellor.getName() + ",\n\n" +
                            "Your application to join V-RGUIDE has been approved by the administrator. " +
                            "You can now log in and set your weekly schedule for students to book sessions.");

            return "Counsellor " + email + " has been APPROVED and notified via email.";
        }
        throw new UserNotFoundException("Counsellor with email " + email + " not found.");
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
    // ARHAMMMMMMMMM
    // public List<Counsellor> getAllCounsellors() {
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method
    // 'getAllCounsellors'");
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
        if (user instanceof Counsellor counsellor) {
            counsellor.setStatus("REJECTED");
            userRepository.save(counsellor);

            // This sends the actual email notification
            emailService.sendEmail(
                    email,
                    "V-RGUIDE: Application Status Update",
                    "Dear Counsellor, we regret to inform you that your application has been rejected. Please contact the administrator for more details.");

            return "Counsellor application for " + email + " has been REJECTED and notified.";
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

    @Async
    public void notifyStudent(String studentEmail, String details) {
        System.out.println("[ASYNC THREAD] Preparing to send email notification to: " + studentEmail);
        try {
            emailService.sendEmail(
                    studentEmail,
                    "V-RGUIDE: Appointment Update",
                    details); // This 'details' now includes the Jitsi link from the method above
            System.out.println("[ASYNC THREAD] Real email sent successfully to: " + studentEmail);
        } catch (Exception e) {
            System.err.println("[ASYNC THREAD] Failed to send email: " + e.getMessage());
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
            // Improved notification call
            notifyStudent(appt.getStudentEmail(),
                    "Your appointment on " + appt.getAppointmentDate() + " at " + appt.getTimeSlot()
                            + " has been cancelled.");

            // 3. Delete the record
            appointmentRepository.deleteById(appointmentId);

            return "Appointment cancelled and slot restored.";
        }
        return "Appointment not found.";
    }

    // public String processBooking(String string, String string2, String string3,
    // String string4) {
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method
    // 'processBooking'");
    // }
    // UserService.java

    // Keep this exact name as requested
    // Main Booking Logic
    public synchronized String processBooking(String student, String counsellorEmail, String day, String slot) {
        User user = userRepository.findByEmail(counsellorEmail);

        if (user instanceof Counsellor counsellor) {
            Map<String, List<String>> schedule = counsellor.getWeeklySchedule();

            if (schedule != null && schedule.containsKey(day) && schedule.get(day).contains(slot)) {

                // 1. Remove the slot from the counsellor
                schedule.get(day).remove(slot);
                userRepository.save(counsellor);

                // 2. Generate Jitsi Link
                String roomName = "VRGUIDE-" + counsellor.getName().replace(" ", "-") + "-"
                        + System.currentTimeMillis();
                String jitsiUrl = "https://meet.jit.si/" + roomName;

                // 3. Create and Save Appointment
                Appointment appt = new Appointment();
                appt.setStudentEmail(student);
                appt.setCounsellorEmail(counsellorEmail);
                appt.setAppointmentDate(day);
                appt.setTimeSlot(slot);
                appt.setStatus("BOOKED");
                appt.setMeetingLink(jitsiUrl);
                appointmentRepository.save(appt);

                // 4. Trigger the notification (Fixed method name call)
                sendBookingEmail(student, jitsiUrl, day, slot);

                return "Success: Appointment booked for " + day + " at " + slot + ". Meeting Link: " + jitsiUrl;
            }
        }
        return "Error: Slot not available.";
    }

    // Helper method to fix the 'method not found' error
    @Async
    private void sendBookingEmail(String email, String link, String date, String time) {
        String message = "Your counseling session is confirmed!\n\n" +
                "Date: " + date + "\n" +
                "Time: " + time + "\n" +
                "Join Live Session: " + link;

        emailService.sendEmail(email, "V-RGUIDE: Session Confirmed", message);
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

    // Add this to UserService.java
    public List<Appointment> getStudentAppointments(String email) {
        return appointmentRepository.findAll().stream()
                .filter(a -> a.getStudentEmail().equals(email))
                .toList();
    }

    // @GetMapping("/student/my-appointments/{email}")
    // public List<Appointment> getStudentDashboard(@PathVariable String email) {
    // // Now this will not be null
    // return appointmentRepository.findByStudentEmail(email);
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

    // Inside UserService.java
    // login part
    public String login(String email, String password) {
        // --- 1. Singular Admin Identity Check ---
        if ("admin@vguide.com".equals(email) && "admin123".equals(password)) {
            return "Login successful! Welcome Admin. (Role: ADMIN)";
        }

        // --- 2. Standard User Database Lookup ---
        User user = userRepository.findByEmail(email);

        if (user != null) {
            // Check password
            if (user.getPassword().equals(password)) {

                // Check OTP Verification
                if (!user.isVerified()) {
                    return "Login failed: Please verify your email using the OTP sent to you first.";
                }

                // Special checks for Counsellors (Approval Status)
                if (user instanceof Counsellor counsellor) {
                    if ("PENDING".equals(counsellor.getStatus())) {
                        return "Login successful! Welcome " + user.getName()
                                + ". Note: Your profile is still pending admin approval.";
                    } else if ("REJECTED".equals(counsellor.getStatus())) {
                        return "Login denied: Your counsellor application has been rejected.";
                    }
                }

                return "Login successful! Welcome " + user.getName() + " (Role: " + user.getRole() + ")";
            } else {
                return "Login failed: Incorrect password.";
            }
        }

        return "Login failed: User with this email does not exist.";
    }

}
