package com.canteen.servlet;

import com.canteen.dao.UserDAO;
import com.canteen.model.User;
import com.canteen.util.PasswordUtil;
import com.canteen.util.SmsUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles /login, /register and /logout.
 * Passwords are verified against SHA-256(salt + password); the plaintext
 * password only ever lives inside this request, never in the DB or session.
 */
@WebServlet({"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {

    /** Only college emails may register / log in as students. */
    private static final String COLLEGE_DOMAIN = "acpce.ac.in";

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/login" -> forward(req, resp, "/WEB-INF/jsp/login.jsp");
            case "/register" -> forward(req, resp, "/WEB-INF/jsp/register.jsp");
            case "/logout" -> {
                HttpSession s = req.getSession(false);
                if (s != null) s.invalidate();
                resp.sendRedirect(req.getContextPath() + "/menu");
            }
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            if ("/login".equals(path)) {
                doLogin(req, resp);
            } else if ("/register".equals(path)) {
                doRegister(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            fail(req, resp, "Something went wrong", "Please try again in a moment.");
        }
    }

    private void doLogin(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        String next = trim(req.getParameter("next"));

        User user = userDAO.findByEmail(email == null ? "" : email.toLowerCase());
        if (user == null || !PasswordUtil.verify(password == null ? "" : password,
                user.getSalt(), user.getPasswordHash())) {
            req.setAttribute("error", "Invalid email or password.");
            req.setAttribute("next", next);
            forward(req, resp, "/WEB-INF/jsp/login.jsp");
            return;
        }
        // Students must use a college email; the admin account is exempt.
        if (!user.isAdmin() && !isCollegeEmail(user.getEmail())) {
            req.setAttribute("error", "Please log in with your college email (@"
                    + COLLEGE_DOMAIN + ").");
            req.setAttribute("next", next);
            forward(req, resp, "/WEB-INF/jsp/login.jsp");
            return;
        }
        // Phone must be verified before first login (admins are pre-verified).
        if (!user.isAdmin() && !user.isPhoneVerified()) {
            resp.sendRedirect(req.getContextPath() + "/verify?email="
                    + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                    + (next != null && !next.isEmpty()
                        ? "&next=" + URLEncoder.encode(next, StandardCharsets.UTF_8) : ""));
            return;
        }
        login(req, user);
        // open-redirect guard: only allow relative targets inside this app
        if (next != null && next.startsWith("/") && !next.startsWith("//")) {
            resp.sendRedirect(req.getContextPath() + next);
        } else {
            resp.sendRedirect(req.getContextPath() + (user.isAdmin() ? "/admin" : "/menu"));
        }
    }

    private void doRegister(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        String name = trim(req.getParameter("name"));
        String email = trim(req.getParameter("email"));
        String phone = SmsUtil.normalizePhone(trim(req.getParameter("phone")));
        String password = req.getParameter("password");
        String confirm = req.getParameter("confirm");

        String err = validate(name, email, password, confirm, phone);
        if (err == null && userDAO.emailExists(email.toLowerCase())) {
            err = "This email is already registered. Try logging in.";
        }
        if (err == null && userDAO.phoneExists(phone)) {
            err = "This phone number is already registered. Try logging in.";
        }
        if (err != null) {
            req.setAttribute("error", err);
            req.setAttribute("name", name);
            req.setAttribute("email", email);
            req.setAttribute("phone", phone);
            forward(req, resp, "/WEB-INF/jsp/register.jsp");
            return;
        }

        User u = new User();
        u.setName(name);
        u.setEmail(email.toLowerCase());
        u.setPhone(phone);
        int id = userDAO.create(u, password);
        u.setId(id);
        u.setRole("STUDENT");
        // New accounts start unverified: issue a phone code and send them to /verify.
        // login(req, u) happens only after the code is confirmed (VerifyServlet).
        VerifyServlet.issuePhoneCode(u.getEmail());
        resp.sendRedirect(req.getContextPath() + "/verify?email="
                + URLEncoder.encode(u.getEmail(), StandardCharsets.UTF_8));
    }

    /** College-domain check for student emails (case-insensitive). */
    private static boolean isCollegeEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@" + COLLEGE_DOMAIN);
    }

    /** Server-side validation (the JSP also validates in JS - defence in depth). */
    private String validate(String name, String email, String pw, String confirm, String phone) {
        if (name == null || name.length() < 2) return "Please enter your full name.";
        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
            return "Please enter a valid email address.";
        if (!isCollegeEmail(email))
            return "Please register with your college email (@" + COLLEGE_DOMAIN + ").";
        if (pw == null || pw.length() < 6) return "Password must be at least 6 characters.";
        if (!pw.equals(confirm)) return "Passwords do not match.";
        if (!SmsUtil.isValidIndianMobile(phone))
            return "Please enter a valid 10-digit mobile number.";
        return null;
    }

    /** Puts a scrubbed user object in the session (no hash/salt kept in memory). */
    private void login(HttpServletRequest req, User user) {
        user.setPasswordHash(null);
        user.setSalt(null);
        req.getSession(true).setAttribute("user", user);
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String t, String m)
            throws ServletException, IOException {
        req.setAttribute("errorTitle", t);
        req.setAttribute("errorMessage", m);
        forward(req, resp, "/WEB-INF/jsp/error.jsp");
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String jsp)
            throws ServletException, IOException {
        RequestDispatcher rd = req.getRequestDispatcher(jsp);
        rd.forward(req, resp);
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
