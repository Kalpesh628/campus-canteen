package com.canteen.servlet;

import com.canteen.dao.UserDAO;
import com.canteen.model.User;
import com.canteen.util.EmailUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Forgot password, step 1: GET /forgot shows the email form, POST issues a
 * 6-digit reset code (15-minute expiry) and emails it.
 *
 * Security notes:
 * - Passwords are salted SHA-256 hashes: they can never be read back out of
 *   the DB, so "forgot password" always means *reset*, never retrieval.
 * - The response is identical whether or not the email exists, so the
 *   endpoint cannot be used to enumerate registered accounts.
 * - The admin account (admin@canteen.local) has no real inbox and is
 *   excluded; admins rotate passwords via /admin/password instead.
 */
@WebServlet("/forgot")
public class ForgotServlet extends HttpServlet {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_TTL_MINUTES = 15;

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/forgot.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = trim(req.getParameter("email"));
        if (email != null) email = email.toLowerCase();

        try {
            if (email != null && !email.isEmpty()) {
                User user = userDAO.findByEmail(email);
                if (user != null && !"ADMIN".equalsIgnoreCase(user.getRole())) {
                    String code = String.format("%06d", RANDOM.nextInt(1_000_000));
                    userDAO.setResetCode(email, code,
                            LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES));
                    EmailUtil.sendPasswordResetCode(email, code);
                }
            }
        } catch (Exception e) {
            // Deliberately generic: never reveal whether the email exists.
        }
        // Same destination either way - no account enumeration.
        resp.sendRedirect(req.getContextPath() + "/reset?email="
                + java.net.URLEncoder.encode(email == null ? "" : email,
                        java.nio.charset.StandardCharsets.UTF_8));
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
