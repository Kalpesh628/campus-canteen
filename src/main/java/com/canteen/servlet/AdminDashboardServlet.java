package com.canteen.servlet;

import com.canteen.dao.MenuItemDAO;
import com.canteen.dao.OrderDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/** Admin home: today's numbers, pending work, availability, and mini stats. */
@WebServlet({"/admin", "/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final MenuItemDAO menuDAO = new MenuItemDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("todayCount", orderDAO.countToday());
            req.setAttribute("todayRevenue", orderDAO.revenueToday());
            req.setAttribute("pendingCount", orderDAO.countPending());
            req.setAttribute("unavailableCount", menuDAO.countUnavailable());
            List<com.canteen.model.Order> recent = orderDAO.findAll(null);
            req.setAttribute("recentOrders", recent.subList(0, Math.min(8, recent.size())));
            req.setAttribute("ordersPerDay", orderDAO.ordersPerDay());
            req.setAttribute("topItems", orderDAO.topItems());
            req.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Dashboard unavailable");
            req.setAttribute("errorMessage", "We could not load the dashboard. Please try again.");
            req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
        }
    }
}
