package com.canteen.servlet;

import com.canteen.dao.FeedbackDAO;
import com.canteen.dao.OrderDAO;
import com.canteen.model.Order;
import com.canteen.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Rating (1-5) + comment on a completed order. One feedback per order. */
@WebServlet("/feedback")
public class FeedbackServlet extends HttpServlet {

    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            User user = (User) req.getSession().getAttribute("user");
            Order order = orderDAO.findById(orderId);
            if (order == null || order.getUserId() != user.getId()
                    || !Order.PICKED_UP.equals(order.getStatus())
                    || feedbackDAO.existsForOrder(orderId)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("order", order);
            req.getRequestDispatcher("/WEB-INF/jsp/feedback.jsp").forward(req, resp);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            int rating = Integer.parseInt(req.getParameter("rating"));
            String comment = req.getParameter("comment");
            User user = (User) req.getSession().getAttribute("user");

            if (rating < 1 || rating > 5) throw new IllegalArgumentException("rating");
            if (comment != null && comment.length() > 1000) comment = comment.substring(0, 1000);

            feedbackDAO.add(orderId, user.getId(), rating, comment);
            resp.sendRedirect(req.getContextPath() + "/orders");
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Feedback not saved");
            req.setAttribute("errorMessage",
                    e.getMessage() == null ? "Please try again." : e.getMessage());
            req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
        }
    }
}
