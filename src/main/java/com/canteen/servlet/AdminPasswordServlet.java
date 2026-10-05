package com.canteen.servlet;

import com.canteen.dao.UserDAO;
import com.canteen.model.User;
import com.canteen.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Admin self-service password change: GET /admin/password shows the form,
 * POST verifies the current password and stores the new one (fresh salt).
 * Guarded by AdminFilter (/admin/*) like every other admin URL.
 */
@WebServlet("/admin/password")
public class AdminPasswordServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/admin/password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            User user = (User) req.getSession(false).getAttribute("user");
            // Re-read from DB so we have the current hash (session copy is scrubbed).
            User fresh = userDAO.findById(user.getId());

            String current = req.getParameter("current");
            String next = req.getParameter("next");
            String confirm = req.getParameter("confirm");

            String err = null;
            if (current == null || !PasswordUtil.verify(current, fresh.getSalt(), fresh.getPasswordHash())) {
                err = "Current password is incorrect.";
            } else if (next == null || next.length() < 6) {
                err = "New password must be at least 6 characters.";
            } else if (!next.equals(confirm)) {
                err = "New passwords do not match.";
            }
            if (err != null) {
                req.setAttribute("error", err);
                req.getRequestDispatcher("/WEB-INF/jsp/admin/password.jsp").forward(req, resp);
                return;
            }
            userDAO.updatePassword(user.getId(), next);
            req.setAttribute("info", "Password updated successfully.");
            req.getRequestDispatcher("/WEB-INF/jsp/admin/password.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Something went wrong");
            req.setAttribute("errorMessage", "Please try again in a moment.");
            req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
        }
    }
}
