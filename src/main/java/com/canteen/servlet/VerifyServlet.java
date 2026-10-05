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
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Phone verification: GET /verify?email=... shows the code form,
 * POST verifies the 6-digit code (or resends it).
 *
 * Email OTP was replaced by phone OTP (2026-10-05) because email delivery
 * needs an SMTP setup that isn't available yet; the phone number is already
 * collected at registration. The code is random, single-use and expires
 * after 15 minutes.
 *
 * DEMO MODE: no SMS gateway is configured, so the code is shown on screen
 * (SmsUtil.isConfigured() == false). Wire a real gateway later and the
 * servlets will send texts instead of showing the code.
 *
 * Public URL on purpose (AuthFilter does not guard it) - the user is not
 * logged in yet at this point.
 */
@WebServlet("/verify")
public class VerifyServlet extends HttpServlet {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_TTL_MINUTES = 15;

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = trim(req.getParameter("email"));
        if (email == null || email.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/register");
            return;
        }
        email = email.toLowerCase();
        req.setAttribute("email", email);
        req.setAttribute("next", trim(req.getParameter("next")));
        try {
            User user = userDAO.findByEmail(email);
            if (user != null) {
                req.setAttribute("maskedPhone", SmsUtil.maskPhone(user.getPhone()));
                // Demo mode: show the code on screen when no SMS gateway exists.
                if (!SmsUtil.isConfigured()) {
                    req.setAttribute("demoCode", userDAO.getLiveVerificationCode(email));
                }
            }
        } catch (Exception ignored) { /* never break the page */ }
        req.getRequestDispatcher("/WEB-INF/jsp/verify.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = trim(req.getParameter("email"));
        String next = trim(req.getParameter("next"));
        String action = trim(req.getParameter("action"));
        if (email == null || email.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/register");
            return;
        }
        email = email.toLowerCase();

        try {
            if ("resend".equals(action)) {
                issuePhoneCode(email);
                req.setAttribute("info", "A fresh code was sent. It expires in 15 minutes.");
            } else {
                String code = trim(req.getParameter("code"));
                String live = userDAO.getLiveVerificationCode(email);
                if (code != null && live != null && constantTimeEquals(code, live)) {
                    userDAO.markPhoneVerified(email);
                    User user = userDAO.findByEmail(email);
                    user.setPasswordHash(null);
                    user.setSalt(null);
                    req.getSession(true).setAttribute("user", user);
                    resp.sendRedirect(req.getContextPath() + safeNext(next));
                    return;
                }
                req.setAttribute("error", "Wrong or expired code. Check and try again, or resend.");
            }
        } catch (Exception e) {
            req.setAttribute("error", "Something went wrong. Please try again.");
        }
        req.setAttribute("email", email);
        req.setAttribute("next", next);
        try {
            User user = userDAO.findByEmail(email);
            if (user != null) {
                req.setAttribute("maskedPhone", SmsUtil.maskPhone(user.getPhone()));
                if (!SmsUtil.isConfigured()) {
                    req.setAttribute("demoCode", userDAO.getLiveVerificationCode(email));
                }
            }
        } catch (Exception ignored) { /* never break the page */ }
        req.getRequestDispatcher("/WEB-INF/jsp/verify.jsp").forward(req, resp);
    }

    /**
     * Generates, stores and "sends" a fresh phone code.
     * Returns whether a real SMS was sent (always false in demo mode).
     */
    public static boolean issuePhoneCode(String email) throws Exception {
        UserDAO dao = new UserDAO();
        User user = dao.findByEmail(email.toLowerCase());
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        dao.setVerificationCode(email.toLowerCase(), code,
                LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES));
        if (user == null) return false;
        return SmsUtil.sendVerificationCode(user.getPhone(), code);
    }

    private String safeNext(String next) {
        if (next != null && next.startsWith("/") && !next.startsWith("//")) return next;
        return "/menu";
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
