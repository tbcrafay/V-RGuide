package com.example.V_RGUIDE;



import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.V_RGUIDE.model.Appointment;
import com.example.V_RGUIDE.repository.AppointmentRepository;
import com.example.V_RGUIDE.service.AppointmentService;

@SpringBootTest
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    public void testSelfBookingPrevention() {
        // Create an appointment where student and counsellor are the same
        Appointment appt = new Appointment();
        appt.setStudentEmail("rafayinfotechnies@gmail.com");
        appt.setCounsellorEmail("rafayinfotechnies@gmail.com");
        appt.setAppointmentDate("Friday");
        appt.setTimeSlot("10:00 - 11:00");

        // Execute the service method
        String result = appointmentService.bookAppointment(appt);

        // Verify the logic returns the specific Protocol Violation error
        assertEquals("Error: Protocol Violation. You cannot book a session with yourself.", result);
    }
} 
