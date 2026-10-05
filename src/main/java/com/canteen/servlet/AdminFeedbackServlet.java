package com.canteen.servlet;

import com.canteen.dao.FeedbackDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Admin view of all student feedback. */
@WebServlet("/admin/feedback")
public class AdminFeedbackServlet extends HttpServlet {

    private final FeedbackDAO dao = new FeedbackDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("feedbacks", dao.findAll());
            req.getRequestDispatcher("/WEB-INF/jsp/admin/feedback.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Feedback unavailable");
            req.setAttribute("errorMessage", "We could not load the feedback.");
            req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
        }
    }
}
