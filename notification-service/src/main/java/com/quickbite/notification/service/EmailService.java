package com.quickbite.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(buildPlainTextBody(subject, body), buildHtmlBody(subject, body));

            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    private String buildPlainTextBody(String subject, String body) {
        return "QuickBite\n\n" + subject + "\n\n" + body + "\n\nThank you for choosing QuickBite.";
    }

    private String buildHtmlBody(String subject, String body) {
        String safeSubject = escapeHtml(subject);
        String safeBody = escapeHtml(body).replace("\n", "<br>");

        return """
                <html>
                  <body style="margin:0; padding:24px; background-color:#f4f7fb; font-family:Arial, sans-serif; color:#1f2937;">
                    <div style="max-width:640px; margin:0 auto; background:#ffffff; border-radius:18px; overflow:hidden; box-shadow:0 8px 24px rgba(15, 23, 42, 0.08);">
                      <div style="background:linear-gradient(135deg, #ff6b35, #ff8c42); padding:28px 32px; color:#ffffff;">
                        <div style="font-size:28px; font-weight:700; letter-spacing:0.4px;">QuickBite</div>
                        <div style="margin-top:8px; font-size:14px; opacity:0.92;">Fresh updates for your food delivery journey</div>
                      </div>

                      <div style="padding:32px;">
                        <div style="display:inline-block; padding:6px 12px; background:#fff1eb; color:#c2410c; border-radius:999px; font-size:12px; font-weight:700; letter-spacing:0.5px; text-transform:uppercase;">
                          Notification
                        </div>

                        <h2 style="margin:18px 0 12px; font-size:24px; line-height:1.3; color:#111827;">%s</h2>

                        <div style="padding:20px; background:#f8fafc; border:1px solid #e5e7eb; border-left:5px solid #ff6b35; border-radius:12px; font-size:15px; line-height:1.7;">
                          %s
                        </div>

                        <div style="margin-top:28px; font-size:14px; line-height:1.7; color:#4b5563;">
                          Thank you for choosing <strong>QuickBite</strong>. We're here to keep every update clear, fast, and reliable.
                        </div>
                      </div>

                      <div style="padding:18px 32px; background:#f9fafb; border-top:1px solid #e5e7eb; font-size:12px; color:#6b7280; text-align:center;">
                        This is an automated QuickBite message. Please do not reply directly to this email.
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(safeSubject, safeBody);
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
