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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Forgot password, step 2: GET /reset?email=... shows the code + new-password
 * form, POST verifies the code and sets the new password (fresh salt + hash).
 * The reset code is single-use: it is cleared on success.
 */
@WebServlet("/reset")
public class ResetServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = trim(req.getParameter("email"));
        req.setAttribute("email", email == null ? "" : email.toLowerCase());
        // Demo mode: show the code on screen when no SMTP is configured.
        if (!EmailUtil.isConfigured() && email != null && !email.isEmpty()) {
            try {
                req.setAttribute("demoCode",
                        userDAO.getLiveResetCode(email.toLowerCase()));
            } catch (Exception ignored) { /* never break the page */ }
        }
        req.getRequestDispatcher("/WEB-INF/jsp/reset.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = trim(req.getParameter("email"));
        String code = trim(req.getParameter("code"));
        String pw1 = req.getParameter("newPassword");
        String pw2 = req.getParameter("confirmPassword");
        if (email != null) email = email.toLowerCase();

        String error = null;
        try {
            if (email == null || email.isEmpty()) {
                error = "Missing email address. Start again from the forgot-password page.";
            } else if (pw1 == null || pw1.length() < 6) {
                error = "Password must be at least 6 characters.";
            } else if (!pw1.equals(pw2)) {
                error = "The two passwords do not match.";
            } else {
                String live = userDAO.getLiveResetCode(email);
                if (code != null && live != null && constantTimeEquals(code, live)) {
                    User user = userDAO.findByEmail(email);
                    if (user != null && !"ADMIN".equalsIgnoreCase(user.getRole())) {
                        userDAO.updatePassword(user.getId(), pw1);
                        userDAO.clearResetCode(email);
                        resp.sendRedirect(req.getContextPath()
                                + "/login?reset=ok");
                        return;
                    }
                }
                error = "Wrong or expired code. Check and try again, or request a fresh one.";
            }
        } catch (Exception e) {
            error = "Something went wrong. Please try again.";
        }
        req.setAttribute("error", error);
        req.setAttribute("email", email == null ? "" : email);
        if (!EmailUtil.isConfigured() && email != null && !email.isEmpty()) {
            try {
                req.setAttribute("demoCode", userDAO.getLiveResetCode(email));
            } catch (Exception ignored) { /* never break the page */ }
        }
        req.getRequestDispatcher("/WEB-INF/jsp/reset.jsp").forward(req, resp);
    }

    /** Timing-attack-safe comparison for the short numeric code. */
    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
