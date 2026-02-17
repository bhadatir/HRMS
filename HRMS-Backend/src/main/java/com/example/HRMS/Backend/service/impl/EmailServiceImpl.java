package com.example.HRMS.Backend.service.impl;

import com.example.HRMS.Backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${URL.path}")
    private String URL;

    @Value("${img.path}")
    private String folderPath;

    @Override
    public void sendEmail(List<String> email, String sub, String content){

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            String[] emails = email.toArray(new String[0]);

            mailMessage.setFrom(sender);
            mailMessage.setTo(emails);
            mailMessage.setText(content);
            mailMessage.setSubject(sub);

            javaMailSender.send(mailMessage);
    }

    @Override
    public void sendEmailWithAttachement(List<String> to, String subject, String text, String path) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            String[] emails = to.toArray(new String[0]);

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(emails);
            helper.setSubject(subject);
            helper.setText(text);

//          ClassPathResource image = new ClassPathResource("static/"+path.substring(22));

            String relativePath = path.replace(URL, "");
            String fullPath = System.getProperty("user.dir") +"/"+ folderPath + relativePath;
            FileSystemResource image = new FileSystemResource(fullPath);
            if (!image.exists()) {
                throw new IOException("Image not found at " + image.getPath());
            }

            helper.addAttachment("data.png", image);

            javaMailSender.send(message);

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}

