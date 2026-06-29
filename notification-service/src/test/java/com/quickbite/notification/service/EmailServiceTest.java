package com.quickbite.notification.service;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EmailServiceTest {

    private EmailService emailService;
    private RecordingMailSender mailSender;

    @BeforeEach
    void setUp() throws Exception {
        emailService = new EmailService();
        mailSender = new RecordingMailSender();

        injectField(emailService, "mailSender", mailSender);
        injectField(emailService, "fromEmail", "sender@test.com");
    }

    @Test
    void testSendEmailSuccess() throws Exception {
        emailService.sendEmail("recipient@test.com", "Subject", "Body Text");

        assertNotNull(mailSender.sentMessage);
        assertEquals("Subject", mailSender.sentMessage.getSubject());

        Address[] recipients = mailSender.sentMessage.getRecipients(Message.RecipientType.TO);
        assertNotNull(recipients);
        assertEquals(1, recipients.length);
        assertEquals("recipient@test.com", ((InternetAddress) recipients[0]).getAddress());

        assertNotNull(mailSender.sentMessage.getContent());
    }

    @Test
    void testSendEmailThrowsException() {
        mailSender.throwOnSend = true;

        assertDoesNotThrow(() -> emailService.sendEmail("recipient@test.com", "Subject", "Body Text"));
        assertNotNull(mailSender.createdMessage);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class RecordingMailSender implements JavaMailSender {
        private MimeMessage createdMessage;
        private MimeMessage sentMessage;
        private boolean throwOnSend;

        @Override
        public MimeMessage createMimeMessage() {
            createdMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
            return createdMessage;
        }

        @Override
        public MimeMessage createMimeMessage(java.io.InputStream contentStream) {
            throw new UnsupportedOperationException("Not needed for this test");
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            if (throwOnSend) {
                throw new org.springframework.mail.MailSendException("Mail server down");
            }
            sentMessage = mimeMessage;
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            if (mimeMessages != null && mimeMessages.length > 0) {
                send(mimeMessages[0]);
            }
        }

        @Override
        public void send(org.springframework.mail.SimpleMailMessage simpleMessage) throws MailException {
            throw new UnsupportedOperationException("Simple mail not used");
        }

        @Override
        public void send(org.springframework.mail.SimpleMailMessage... simpleMessages) throws MailException {
            throw new UnsupportedOperationException("Simple mail not used");
        }
    }
}
