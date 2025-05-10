package org.example;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.Token.TokenService;
import org.example.Users.UsersService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.example.Users.User;
import java.io.UnsupportedEncodingException;

@Service
public class EmailService {
    private static final CustomLogger logger = new CustomLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final UsersService usersService;
    private final TokenService tokenService;

    public EmailService(JavaMailSender mailSender,UsersService usersService, TokenService tokenService) {
        this.mailSender = mailSender;
        this.usersService = usersService;
        this.tokenService = tokenService;
        logger.setVisibility(false);
    }

    public String sendEmailVerification(String toEmail, String username) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("support@mathgenerator.com", "MathGenerator Support");
            helper.setTo(toEmail);
            helper.setSubject("Welcome " + username + "! Confirm Your MathGenerator Account");

            String token=tokenService.generateAndStoreVerifyToken();
            String verificationLink = Constants.FRONTEND_PATH + "/verify/verify-email?token=" + token;
            String plainText = "Hello " + username + ",\n\n" +
                    "Thank you for joining MathGenerator! Please verify your email by clicking the link below:\n" +
                    verificationLink + "\n\n" +
                    "If you didn't request this, you can safely ignore this email.\n\n" +
                    "– The MathGenerator Team";

            String htmlBody = "<!DOCTYPE html>" +
                    "<html>" +
                    "<body style='font-family: Arial, sans-serif; text-align: center;'>" +
                    "<h2>Hello " + username + ",</h2>" +
                    "<p>Thank you for joining MathGenerator! Please verify your email by clicking the button below:</p>" +

                    "<table cellspacing='0' cellpadding='0'>" +
                    "  <tr>" +
                    "    <td align='center' bgcolor='#28a745' style='border-radius: 5px;'>" +
                    "      <a href='" + verificationLink + "' target='_blank' " +
                    "         style='display: inline-block; padding: 15px 30px; font-size: 16px; color: #ffffff; " +
                    "                text-decoration: none; border-radius: 5px;'>Verify Email</a>" +
                    "    </td>" +
                    "  </tr>" +
                    "</table>" +

                    "<p>If the button doesn't work, copy and paste this link into your browser:</p>" +
                    "<p><a href='" + verificationLink + "' target='_blank'>" + verificationLink + "</a></p>" +
                    "<hr style='margin-top:20px;'>" +
                    "<p>If you didn't request this, you can safely ignore this email.</p>" +
                    "<p>– The MathGenerator Team</p>" +
                    "</body>" +
                    "</html>";

            helper.setText(plainText, htmlBody);

            mailSender.send(mimeMessage);
            return token;
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public void sendAlertMultipleLoginAttempts(String username) {
        try {
            User user= usersService.getUser(username);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("support@mathgenerator.com", "MathGenerator Support");
            helper.setTo(user.getEmail());
            helper.setSubject("Security Alert: Multiple Login Attempts Detected");

            String plainText = "Hello " + username + ",\n\n" +
                    "We have noticed multiple attempts to access your account. " +
                    "If this wasn't you, please reset your password immediately.\n\n" +
                    "– The MathGenerator Team";

            String htmlBody = "<!DOCTYPE html><html><body style='font-family: Arial, sans-serif; text-align: center;'>" +
                    "<h2>Hello " + username + ",</h2>" +
                    "<p>We have noticed multiple attempts to access your account. " +
                    "If this wasn’t you, please reset your password immediately.</p>" +
                    "<p>– The MathGenerator Team</p>" +
                    "</body></html>";

            helper.setText(plainText, htmlBody);

            mailSender.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
    }
    public boolean forgotPassword(String username) {
        try {
            User user = usersService.getUser(username);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("support@mathgenerator.com", "MathGenerator Support");
            helper.setTo(user.getEmail());
            helper.setSubject("Password Reset Request");

            String token = tokenService.generateAndStoreResetToken(user.getUser_id());
            String resetPasswordLink = Constants.FRONTEND_PATH + "/reset-password?token=" + token;

            String plainText = "Hello " + username + ",\n\n" +
                    "We received a request to reset your password. Click the link below to set a new password:\n" +
                    resetPasswordLink + "\n\n" +
                    "If you didn't request this, please ignore this email.\n\n" +
                    "– The MathGenerator Team";

            String htmlBody = "<!DOCTYPE html>" +
                    "<html><body style='font-family: Arial, sans-serif; text-align: center;'>" +
                    "<h2>Hello " + username + ",</h2>" +
                    "<p>We received a request to reset your password. Click the button below to set a new password:</p>" +
                    "<a href='" + resetPasswordLink + "' style='padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;'>Reset Password</a>" +
                    "<p>If the button doesn't work, copy and paste this link into your browser:</p>" +
                    "<p><a href='" + resetPasswordLink + "'>" + resetPasswordLink + "</a></p>" +
                    "<hr><p>If you didn't request this, you can safely ignore this email.</p>" +
                    "<p style='font-size: 12px; color: gray;'>This is an automated message. Please do not reply.</p>" +
                    "</body></html>";

            helper.setText(plainText, htmlBody);
            mailSender.send(mimeMessage);

            logger.info("Password reset email sent to: {}", user.getEmail());
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Failed to send password reset email to {}: {}", username, e.getMessage());
        }
        return false;
    }


}
