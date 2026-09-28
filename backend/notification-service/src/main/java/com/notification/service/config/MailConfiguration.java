package com.notification.service.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Builds the SMTP sender used for paid-order notifications. App passwords shown
 * by Google can contain visual grouping spaces; SMTP requires the compact value.
 */
@Configuration
public class MailConfiguration {

    @Bean
    JavaMailSender javaMailSender(
        @Value("${spring.mail.host:localhost}") String host,
        @Value("${spring.mail.port:587}") int port,
        @Value("${spring.mail.username:}") String username,
        @Value("${spring.mail.password:}") String password,
        @Value("${spring.mail.properties.mail.smtp.auth:true}") boolean auth,
        @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") boolean startTls
    ) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password == null ? "" : password.replaceAll("\\s", ""));

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", Boolean.toString(auth));
        properties.put("mail.smtp.starttls.enable", Boolean.toString(startTls));
        properties.put("mail.smtp.starttls.required", Boolean.toString(startTls));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "15000");
        properties.put("mail.smtp.writetimeout", "15000");
        return sender;
    }
}
