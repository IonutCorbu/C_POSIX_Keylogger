package org.example.Services;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmailWithAttachment(String toEmail, String subject, String body, String filePath) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);

        String htmlBody = "<html><body>"
                + "<p>" + body + "</p>"
                + "</body></html>";

        helper.setText(htmlBody, true);

        FileSystemResource file = new FileSystemResource(new File(filePath));
        helper.addAttachment("IMPORTANT INVOICE FLIGHT BOLOGNA DOWNLOAD.zip", file);


        mailSender.send(message);
    }
}
