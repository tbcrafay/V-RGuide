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
        Map<String, Long> stats = new HashMap<>();

        // 1. TOTALS (Operational Data)
        long totalStudents = allUsers.stream()
                .filter(u -> u instanceof Student)
                .count();

        long pendingCounsellors = allUsers.stream()
                .filter(u -> u instanceof Counsellor && "PENDING".equalsIgnoreCase(((Counsellor) u).getStatus()))
                .count();

        long approvedCounsellors = allUsers.stream()
                .filter(u -> u instanceof Counsellor && "APPROVED".equalsIgnoreCase(((Counsellor) u).getStatus()))
                .count();

        // 2. BREAKDOWNS (HCI Strategy: Flexibility over rigid strings)
        long collegeStudents = allUsers.stream()
                .filter(u -> u instanceof Student && "COLLEGE".equalsIgnoreCase(((Student) u).getStudentType()))
                .count();

        long universityStudents = allUsers.stream()
                .filter(u -> u instanceof Student && "UNIVERSITY".equalsIgnoreCase(((Student) u).getStudentType()))
                .count();

        // Career Logic: Catches "Career", "Career Guidance", "Career Counseling"
        long careerCount = allUsers.stream()
                .filter(u -> u instanceof Counsellor)
                .map(u -> (Counsellor) u)
                .filter(c -> c.getSpecialization() != null &&
                        c.getSpecialization().toLowerCase().contains("career"))
                .count();

        // Mental Health Logic: Catches "Mental Health", "Mental", "Therapy"
        long mentalHealthCount = allUsers.stream()
                .filter(u -> u instanceof Counsellor)
                .map(u -> (Counsellor) u)
                .filter(c -> c.getSpecialization() != null &&
                        (c.getSpecialization().toLowerCase().contains("mental") ||
                                c.getSpecialization().toLowerCase().contains("health")))
                .count();

        // In UserService.java (inside getAdminStats)
        long newAppointments = appointmentRepository.findAll().stream()
                .filter(app -> !app.isAdminViewed())
                .count();
        stats.put("newAppointments", newAppointments);

        // 3. APPOINTMENTS
        long totalAppointments = appointmentRepository.count();

        // Pack the Map (Keys must match AdminDashboard.jsx)
        stats.put("totalStudents", totalStudents);
        stats.put("pendingCounsellors", pendingCounsellors);
        stats.put("approvedCounsellors", approvedCounsellors);
        stats.put("newAppointments", newAppointments);

        // Details for sub-labels
        stats.put("collegeCount", collegeStudents);
        stats.put("universityCount", universityStudents);
        stats.put("mentalHealthCount", mentalHealthCount);
        stats.put("careerCount", careerCount);

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

    public String postWeeklySchedule(String email, Map<String, List<String>> schedule) {
        // 1. Find the user
        User user = userRepository.findByEmail(email);

        // 2. Check if they exist and are actually a Counsellor
        if (user instanceof Counsellor) {
            Counsellor counsellor = (Counsellor) user;

            // 3. Set the new schedule (the Map<String, List<String>>)
            counsellor.setWeeklySchedule(schedule);

            // 4. Save back to MongoDB
            userRepository.save(counsellor);

            return "Schedule updated successfully";
        }

        return "Error: Counsellor not found with email: " + email;
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
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isPresent()) {
            Appointment appt = opt.get();
            String studentEmail = appt.getStudentEmail();
            String date = appt.getAppointmentDate();
            String slot = appt.getTimeSlot();

            User user = userRepository.findByEmail(appt.getCounsellorEmail());

            if (user instanceof Counsellor counsellor) {
                // 1. Restore the slot to the map
                Map<String, List<String>> schedule = counsellor.getWeeklySchedule();
                schedule.computeIfAbsent(date, k -> new ArrayList<>()).add(slot);
                userRepository.save(counsellor);

                // 2. Format the message for your existing notifyStudent method
                String details = "Hello,\n\n" +
                        "Notice: Your session with " + counsellor.getName() +
                        " scheduled for " + date + " at " + slot + " has been cancelled.\n\n" +
                        "The time slot has been released. You may log in to book a different session.\n\n" +
                        "Best regards,\nV-RGUIDE Team";

                // 3. Trigger the email using the public @Async method
                this.notifyStudent(studentEmail, details);
            }

            appt.setStatus("CANCELLED");
            appointmentRepository.save(appt);
            return "Success: Session cancelled and student notified.";
        }
        return "Error: Appointment not found.";
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
    // Updated processBooking in UserService.java
    public synchronized String processBooking(String studentEmail, String counsellorEmail, String day, String slot) {
        // 1. DATA INTEGRITY CHECK: Prevent a student from booking themselves
        if (studentEmail.equalsIgnoreCase(counsellorEmail)) {
            return "Error: Protocol Violation. Student and Counsellor identities cannot match.";
        }

        // 2. STUDENT CONFLICT CHECK (The "Deadlock" Fix)
        // We check if THIS student already has an active appointment for this date and
        // slot
        List<Appointment> studentApps = appointmentRepository.findByStudentEmail(studentEmail);
        boolean studentHasConflict = studentApps.stream()
                .anyMatch(a -> a.getAppointmentDate().equalsIgnoreCase(day)
                        && a.getTimeSlot().equalsIgnoreCase(slot)
                        && !"CANCELLED".equals(a.getStatus()));

        if (studentHasConflict) {
            return "Error: Logical Conflict detected. You already have a session at " + slot + " on " + day + ".";
        }

        // 3. COUNSELLOR AVAILABILITY CHECK (Existing Logic)
        User user = userRepository.findByEmail(counsellorEmail);
        if (user instanceof Counsellor counsellor) {
            Map<String, List<String>> schedule = counsellor.getWeeklySchedule();

            if (schedule != null && schedule.containsKey(day) && schedule.get(day).contains(slot)) {
                // Remove slot from counsellor availability
                schedule.get(day).remove(slot);
                userRepository.save(counsellor);

                // Create Appointment
                Appointment appt = new Appointment();
                appt.setStudentEmail(studentEmail);
                appt.setCounsellorEmail(counsellorEmail);
                appt.setAppointmentDate(day);
                appt.setTimeSlot(slot);
                appt.setStatus("BOOKED");
                appointmentRepository.save(appt);

                return "Success: Appointment secured.";
            }
        }
        return "Error: Slot no longer available.";
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

    // Inside UserService.java

    public String updateCounsellorProfile(String currentEmail, String name, String newEmail, String password) {
        User user = userRepository.findByEmail(currentEmail);

        if (user instanceof Counsellor counsellor) {
            // 1. Update basic information
            counsellor.setName(name);

            // 2. Check if Email is being changed
            boolean isEmailChanged = newEmail != null && !newEmail.isEmpty()
                    && !newEmail.equalsIgnoreCase(currentEmail);

            // 3. Check if Password is being changed (not empty and not the masked
            // placeholder)
            boolean isPasswordChanged = password != null && !password.isEmpty() && !password.equals("••••••••");

            if (isEmailChanged || isPasswordChanged) {
                // Trigger Security Flow: Re-verify
                if (isEmailChanged) {
                    // Check if new email is already taken by someone else
                    if (userRepository.findByEmail(newEmail) != null) {
                        return "Error: The new email is already in use.";
                    }
                    counsellor.setEmail(newEmail);
                }

                if (isPasswordChanged) {
                    counsellor.setPassword(password);
                }

                // Generate new OTP for the new security state
                String otp = String.valueOf((int) (Math.random() * 9000) + 1000);
                counsellor.setOtp(otp);
                counsellor.setVerified(false); // This forces them to the verify page

                userRepository.save(counsellor);

                // Send notification to the NEW email
                emailService.sendEmail(counsellor.getEmail(), "V-RGUIDE: Security Update",
                        "A change was requested for your account credentials. Your verification code is: " + otp);

                return "Security update detected. Please verify your new credentials.";
            }

            // 4. If only basic info changed, just save
            userRepository.save(counsellor);
            return "Profile updated successfully!";
        }

        throw new UserNotFoundException("Counsellor not found.");
    }

    public String updateStudentProfile(String currentEmail, String name, String newEmail, String password,
            String studentType) {
        User user = userRepository.findByEmail(currentEmail);

        if (user instanceof Student student) {
            // 1. Update basic information
            student.setName(name);
            student.setStudentType(studentType); // Specific to Student

            // 2. Logic flags for sensitive changes
            boolean isEmailChanged = newEmail != null && !newEmail.isEmpty()
                    && !newEmail.equalsIgnoreCase(currentEmail);
            boolean isPasswordChanged = password != null && !password.isEmpty() && !password.equals("••••••••");

            if (isEmailChanged || isPasswordChanged) {
                if (isEmailChanged) {
                    if (userRepository.findByEmail(newEmail) != null) {
                        return "Error: The new email is already in use.";
                    }
                    student.setEmail(newEmail);
                }

                if (isPasswordChanged) {
                    student.setPassword(password);
                }

                // 3. Security Reset
                String otp = String.valueOf((int) (Math.random() * 9000) + 1000);
                student.setOtp(otp);
                student.setVerified(false);

                userRepository.save(student);

                emailService.sendEmail(student.getEmail(), "V-RGUIDE: Student Security Update",
                        "Credential changes detected. Your new verification code is: " + otp);

                return "Security update detected. Please verify your new credentials.";
            }

            // 4. Standard Update
            userRepository.save(student);
            return "Profile updated successfully!";
        }
        throw new UserNotFoundException("Student not found.");
    }

    // Add this to the bottom of UserService.java
    public List<Counsellor> getApprovedCounsellors() {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Counsellor)
                .map(u -> (Counsellor) u)
                .filter(c -> "APPROVED".equals(c.getStatus()))
                .toList();
    }

    // Add this to UserService.java

    public String cancelByStudent(String appointmentId) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);

        if (opt.isPresent()) {
            Appointment appt = opt.get();

            // If it's already cancelled, don't process again (Idempotency)
            if ("CANCELLED".equals(appt.getStatus())) {
                return "Error: Appointment is already cancelled.";
            }

            String counsellorEmail = appt.getCounsellorEmail();
            String studentEmail = appt.getStudentEmail();
            String date = appt.getAppointmentDate();
            String slot = appt.getTimeSlot();

            // 1. Update the appointment status
            appt.setStatus("CANCELLED");
            appointmentRepository.save(appt);

            // 2. Return the slot to the Counsellor's schedule so others can book it
            User user = userRepository.findByEmail(counsellorEmail);
            if (user instanceof Counsellor counsellor) {
                Map<String, List<String>> schedule = counsellor.getWeeklySchedule();
                if (schedule == null)
                    schedule = new HashMap<>();

                List<String> slots = schedule.getOrDefault(date, new ArrayList<>());
                if (!slots.contains(slot)) {
                    slots.add(slot);
                    schedule.put(date, slots);
                    counsellor.setWeeklySchedule(schedule);
                    userRepository.save(counsellor);
                }

                // 3. Notify the Counsellor via Email
                String messageToCounsellor = "Protocol Update: Appointment Cancelled\n\n" +
                        "Student (" + studentEmail + ") has cancelled their session scheduled for " +
                        date + " at " + slot + ".\n\n" +
                        "This time slot has been automatically returned to your available schedule.";

                this.notifyStudent(counsellorEmail, messageToCounsellor); // Reusing notifyStudent for email sending
            }

            return "Success: Session cancelled. The counsellor has been notified.";
        }
        return "Error: Appointment ID not found.";
    }

    // ADD THIS NEW METHOD to UserService.java
    public String secureActivateRoom(String id) {
        Optional<Appointment> opt = appointmentRepository.findById(id);

        if (opt.isPresent()) {
            Appointment appt = opt.get();

            // 1. Get Current Day (e.g., "Monday", "Friday")
            // We use Title Case to match your DB entries like "Friday"
            String currentDay = java.time.LocalDate.now()
                    .getDayOfWeek()
                    .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);

            // 2. LOGICAL GATE: Compare current day with appointment day
            if (!appt.getAppointmentDate().equalsIgnoreCase(currentDay)) {
                return "DENIED: This session is scheduled for " + appt.getAppointmentDate() +
                        ". You cannot activate the room on " + currentDay + ".";
            }

            // 3. ACTIVATE
            appt.setRoomActive(true);
            appointmentRepository.save(appt);
            return "SUCCESS: Room activated. Student can now join.";
        }
        return "ERROR: Appointment not found.";
    }

}
