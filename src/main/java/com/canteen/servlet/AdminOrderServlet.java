package com.canteen.servlet;

import com.canteen.dao.OrderDAO;
import com.canteen.model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Admin order workflow: list (with status filter), view detail,
 * and advance the status through the lifecycle.
 */
@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {

    private final OrderDAO dao = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String id = req.getParameter("id");
            if (id != null && !id.isBlank()) {
                Order order = dao.findById(Integer.parseInt(id));
                if (order == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                req.setAttribute("order", order);
                req.setAttribute("nextStatuses", Order.allowedNext(order.getStatus()));
                req.getRequestDispatcher("/WEB-INF/jsp/admin/order-detail.jsp").forward(req, resp);
                return;
            }
            String status = req.getParameter("status");
            List<Order> orders = dao.findAll(status);
            req.setAttribute("orders", orders);
            req.setAttribute("statusFilter", status == null ? "" : status);
            req.setAttribute("allStatuses", List.of("PLACED", "ACCEPTED", "PREPARING",
                    "READY", "PICKED_UP", "REJECTED"));
            req.getRequestDispatcher("/WEB-INF/jsp/admin/orders.jsp").forward(req, resp);
        } catch (Exception e) {
            fail(req, resp, "Orders unavailable", "We could not load the orders.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            String newStatus = req.getParameter("newStatus");
            String reason = req.getParameter("rejectReason");
            if (Order.REJECTED.equals(newStatus)
                    && (reason == null || reason.isBlank())) {
                reason = "No reason given";
            }
            // OrderDAO.updateStatus re-validates the transition server-side.
            dao.updateStatus(id, newStatus, reason);
            resp.sendRedirect(req.getContextPath() + "/admin/orders?id=" + id);
        } catch (Exception e) {
            fail(req, resp, "Status not updated",
                    e.getMessage() == null ? "Please try again." : e.getMessage());
        }
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String t, String m)
            throws ServletException, IOException {
        req.setAttribute("errorTitle", t);
        req.setAttribute("errorMessage", m);
        req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
    }
}
