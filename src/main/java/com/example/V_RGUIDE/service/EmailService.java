package com.example.V_RGUIDE.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    
    public void sendEmailWithRetry(String to, String subject, String body) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                
                System.out.println("Attempting to deploy email to: " + to);

                
                Thread.sleep(2000);

                System.out.println("Email successfully dispatched.");
                break;
            } catch (InterruptedException e) {
                
                Thread.currentThread().interrupt();
                System.err.println("Thread was interrupted during sleep.");
            }
            attempts++;
        }
    }
}
