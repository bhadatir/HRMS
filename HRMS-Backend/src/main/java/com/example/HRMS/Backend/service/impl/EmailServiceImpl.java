package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${URL.path}")
    private String url;

    @Value("${img.path}")
    private String folderPath;

    @Override
    public void sendEmail(List<String> email, String sub, String content){

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            for (String email1 : email) {
                if (!isValidEmail(email1)) {
                    throw new IllegalArgumentException("Invalid email format: " + email1);
                }
            }
            String[] emails = email.toArray(new String[0]);

            mailMessage.setFrom(sender);
            mailMessage.setTo(emails);
            mailMessage.setText(content);
            mailMessage.setSubject(sub);

            javaMailSender.send(mailMessage);
    }

    @Override
    public void sendEmailWithAttachment(List<String> to, String subject, String text, String path) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            for (String email1 : to) {
                if (!isValidEmail(email1)) {
                    throw new IllegalArgumentException("Invalid email format: " + to);
                }
            }
            String[] emails = to.toArray(new String[0]);

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(emails);
            helper.setSubject(subject);
            helper.setText(text, true);

            String relativePath = path.replace(url, "");
            String fullPath = System.getProperty("user.dir") +"/"+ folderPath + relativePath;
            FileSystemResource fileToAttach = new FileSystemResource(fullPath);
            if (!fileToAttach.exists()) {
                throw new IOException("Image not found at " + fileToAttach.getPath());
            }

            String fileName = fileToAttach.getFilename();
            helper.addAttachment(fileName, fileToAttach);

            javaMailSender.send(message);

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}

