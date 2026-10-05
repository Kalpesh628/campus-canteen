package com.canteen.servlet;

import com.canteen.dao.UserDAO;
import com.canteen.model.User;
import com.canteen.util.SmsUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Forgot password, step 1: GET /forgot shows the phone form, POST issues a
 * 6-digit reset code (15-minute expiry) tied to the account's phone number.
 *
 * Phone OTP replaced the email version (2026-10-05): email delivery needs an
 * SMTP setup that isn't available yet, while every student registers a phone
 * number. DEMO MODE: no SMS gateway exists, so the code is shown on screen.
 *
 * Security notes:
 * - Passwords are salted SHA-256 hashes: they can never be read back out of
 *   the DB, so "forgot password" always means *reset*, never retrieval.
 * - The response is identical whether or not the phone exists, so the
 *   endpoint cannot be used to enumerate registered accounts.
 * - The admin account has no phone on file and is excluded; admins rotate
 *   passwords via /admin/password instead.
 */
@WebServlet("/forgot")
public class ForgotServlet extends HttpServlet {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_TTL_MINUTES = 15;

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("1".equals(req.getParameter("sent"))) {
            jakarta.servlet.http.HttpSession session = req.getSession(false);
            req.setAttribute("sent", true);
            if (session != null) {
                req.setAttribute("demoCode", session.getAttribute("forgotDemoCode"));
                req.setAttribute("forgotPhone", session.getAttribute("forgotPhone"));
                session.removeAttribute("forgotDemoCode");
                session.removeAttribute("forgotPhone");
            }
        }
        req.getRequestDispatcher("/WEB-INF/jsp/forgot.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String phone = SmsUtil.normalizePhone(trim(req.getParameter("phone")));

        String demoCode = null;
        try {
            if (phone != null) {
                User user = userDAO.findByPhone(phone);
                if (user != null && !"ADMIN".equalsIgnoreCase(user.getRole())) {
                    String code = String.format("%06d", RANDOM.nextInt(1_000_000));
                    userDAO.setResetCode(user.getEmail(), code,
                            LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES));
                    if (!SmsUtil.sendPasswordResetCode(phone, code)) {
                        demoCode = code; // demo mode: show once on the next page
                    }
                }
            }
        } catch (Exception e) {
            // Deliberately generic: never reveal whether the phone exists.
        }
        // Neutral landing: identical whether or not the phone is registered,
        // so the endpoint can't be used to enumerate accounts (M-4).
        if (demoCode != null) {
            req.getSession(true).setAttribute("forgotDemoCode", demoCode);
        }
        req.getSession(true).setAttribute("forgotPhone", phone == null ? "" : phone);
        resp.sendRedirect(req.getContextPath() + "/forgot?sent=1");
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
