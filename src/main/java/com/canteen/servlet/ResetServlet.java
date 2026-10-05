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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Forgot password, step 2: GET /reset?phone=... shows the code + new-password
 * form, POST verifies the code, sets the new password (fresh salt + hash)
 * and logs the user straight in. The reset code is single-use: it is
 * cleared on success.
 *
 * Phone OTP replaced the email version (2026-10-05). DEMO MODE: the code is
 * shown on screen because no SMS gateway is configured.
 */
@WebServlet("/reset")
public class ResetServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String phone = SmsUtil.normalizePhone(trim(req.getParameter("phone")));
        req.setAttribute("phone", phone == null ? "" : phone);
        req.setAttribute("maskedPhone",
                phone == null ? "" : SmsUtil.maskPhone(phone));
        // Demo mode: show the code on screen when no SMS gateway is configured.
        if (!SmsUtil.isConfigured() && phone != null) {
            try {
                User user = userDAO.findByPhone(phone);
                if (user != null) {
                    req.setAttribute("demoCode",
                            userDAO.getLiveResetCode(user.getEmail()));
                }
            } catch (Exception ignored) { /* never break the page */ }
        }
        req.getRequestDispatcher("/WEB-INF/jsp/reset.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String phone = SmsUtil.normalizePhone(trim(req.getParameter("phone")));
        String code = trim(req.getParameter("code"));
        String pw1 = req.getParameter("newPassword");
        String pw2 = req.getParameter("confirmPassword");

        String error = null;
        try {
            if (phone == null) {
                error = "Missing phone number. Start again from the forgot-password page.";
            } else if (pw1 == null || pw1.length() < 6) {
                error = "Password must be at least 6 characters.";
            } else if (!pw1.equals(pw2)) {
                error = "The two passwords do not match.";
            } else {
                User user = userDAO.findByPhone(phone);
                String live = user == null ? null
                        : userDAO.getLiveResetCode(user.getEmail());
                if (user != null && !"ADMIN".equalsIgnoreCase(user.getRole())
                        && code != null && live != null
                        && constantTimeEquals(code, live)) {
                    userDAO.updatePassword(user.getId(), pw1);
                    userDAO.clearResetCode(user.getEmail());
                    // Straight in: no need to retype the new password.
                    User fresh = userDAO.findByPhone(phone);
                    fresh.setPasswordHash(null);
                    fresh.setSalt(null);
                    req.getSession(true).setAttribute("user", fresh);
                    resp.sendRedirect(req.getContextPath() + "/menu");
                    return;
                }
                error = "Wrong or expired code. Check and try again, or request a fresh one.";
            }
        } catch (Exception e) {
            error = "Something went wrong. Please try again.";
        }
        req.setAttribute("error", error);
        req.setAttribute("phone", phone == null ? "" : phone);
        req.setAttribute("maskedPhone",
                phone == null ? "" : SmsUtil.maskPhone(phone));
        if (!SmsUtil.isConfigured() && phone != null) {
            try {
                User user = userDAO.findByPhone(phone);
                if (user != null) {
                    req.setAttribute("demoCode",
                            userDAO.getLiveResetCode(user.getEmail()));
                }
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
