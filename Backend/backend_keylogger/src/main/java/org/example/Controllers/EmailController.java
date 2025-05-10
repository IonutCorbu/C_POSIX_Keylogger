package org.example.Controllers;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.example.DTO.EmailRequest;
import org.example.Services.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmailWithAttachment(@RequestBody EmailRequest request) {
        String filePath = "D:\\Disertatie interfata\\Resurse\\Invoice archive.zip";
        try {
            emailService.sendEmailWithAttachment(request.getTo(), request.getSubject(), request.getBody(), filePath);
            return ResponseEntity.ok("Email sent successfully.");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}