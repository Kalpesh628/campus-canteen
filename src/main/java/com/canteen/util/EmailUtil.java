package com.canteen.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Sends the email-verification code via SMTP.
 *
 * Configuration (environment variables, e.g. Railway service variables):
 *   SMTP_HOST  - smtp host (unset = demo mode, no email is sent)
 *   SMTP_PORT  - default 587
 *   SMTP_USER  - login username
 *   SMTP_PASS  - login password (use a Gmail "app password", not the real password)
 *   SMTP_FROM  - From: address (defaults to SMTP_USER)
 *
 * Demo mode: when SMTP_HOST is not set, the code is logged to stdout and the
 * caller may show it on screen (the verify page does this, clearly labelled).
 * Set the variables to switch to real email delivery - no code change needed.
 */
public class EmailUtil {

    private static final Logger LOG = Logger.getLogger(EmailUtil.class.getName());

    public static boolean isConfigured() {
        String host = System.getenv("SMTP_HOST");
        return host != null && !host.isBlank();
    }

    /** Sends the 6-digit code. Returns true if an email was actually sent. */
    public static boolean sendVerificationCode(String toEmail, String code) {
        if (!isConfigured()) {
            LOG.info("SMTP not configured - demo mode. Verification code for "
                    + toEmail + " is: " + code);
            return false;
        }
        String host = System.getenv("SMTP_HOST");
        int port = parsePort(System.getenv("SMTP_PORT"), 587);
        String user = System.getenv("SMTP_USER");
        String pass = System.getenv("SMTP_PASS");
        String from = firstNonBlank(System.getenv("SMTP_FROM"), user);

        Properties p = new Properties();
        p.put("mail.smtp.host", host);
        p.put("mail.smtp.port", String.valueOf(port));
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.connectiontimeout", "10000");
        p.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(p, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from, "Campus Canteen"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject("Your Campus Canteen verification code");
            msg.setText("Hi!\n\nYour Campus Canteen verification code is: " + code
                    + "\n\nIt expires in 15 minutes. If you did not register, just ignore this email.\n\n"
                    + "- Campus Canteen");
            Transport.send(msg);
            LOG.info("Verification email sent to " + toEmail);
            return true;
        } catch (Exception e) {
            LOG.severe("Failed to send verification email to " + toEmail + ": " + e.getMessage());
            return false;
        }
    }

    /** Sends the 6-digit password-reset code. Returns true if an email was actually sent. */
    public static boolean sendPasswordResetCode(String toEmail, String code) {
        if (!isConfigured()) {
            LOG.info("SMTP not configured - demo mode. Password-reset code for "
                    + toEmail + " is: " + code);
            return false;
        }
        String host = System.getenv("SMTP_HOST");
        int port = parsePort(System.getenv("SMTP_PORT"), 587);
        String user = System.getenv("SMTP_USER");
        String pass = System.getenv("SMTP_PASS");
        String from = firstNonBlank(System.getenv("SMTP_FROM"), user);

        Properties p = new Properties();
        p.put("mail.smtp.host", host);
        p.put("mail.smtp.port", String.valueOf(port));
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.connectiontimeout", "10000");
        p.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(p, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from, "Campus Canteen"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject("Reset your Campus Canteen password");
            msg.setText("Hi!\n\nYour Campus Canteen password-reset code is: " + code
                    + "\n\nIt expires in 15 minutes. If you did not request this, just ignore this email -\n"
                    + "your password stays unchanged.\n\n- Campus Canteen");
            Transport.send(msg);
            LOG.info("Password-reset email sent to " + toEmail);
            return true;
        } catch (Exception e) {
            LOG.severe("Failed to send password-reset email to " + toEmail + ": " + e.getMessage());
            return false;
        }
    }

    private static int parsePort(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }
}
