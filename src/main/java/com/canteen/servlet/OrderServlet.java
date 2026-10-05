package com.canteen.servlet;

import com.canteen.dao.FeedbackDAO;
import com.canteen.dao.OrderDAO;
import com.canteen.dao.PickupSlotDAO;
import com.canteen.model.CartItem;
import com.canteen.model.Order;
import com.canteen.model.PickupSlot;
import com.canteen.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * /checkout - pick a pickup slot and place the order (Pay at Canteen).
 * /orders   - the student's order history.
 * /track    - live status of one order.
 */
@WebServlet({"/checkout", "/orders", "/track"})
public class OrderServlet extends HttpServlet {

    private final OrderDAO orderDAO = new OrderDAO();
    private final PickupSlotDAO slotDAO = new PickupSlotDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    private User user(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("user");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/checkout" -> showCheckout(req, resp);
                case "/orders" -> showHistory(req, resp);
                case "/track" -> showTrack(req, resp);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            fail(req, resp, "Order unavailable", "We could not load your orders right now.");
        }
    }

    private void showCheckout(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        Map<Integer, CartItem> cart = CartServlet.cartOf(req.getSession());
        if (cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }
        // Offer today + next 2 days; default to today.
        List<LocalDate> dates = List.of(LocalDate.now(),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
        LocalDate chosen;
        try {
            chosen = req.getParameter("date") == null ? dates.get(0)
                    : LocalDate.parse(req.getParameter("date"));
        } catch (Exception e) {
            chosen = dates.get(0);
        }
        if (!dates.contains(chosen)) chosen = dates.get(0);

        List<PickupSlot> slots = slotDAO.findByDateWithRemaining(chosen);
        // M-1: don't offer time slots that have already elapsed today.
        if (chosen.equals(java.time.LocalDate.now())) {
            java.time.LocalTime now = java.time.LocalTime.now();
            slots.removeIf(slot -> slot.getEndTime() != null && !slot.getEndTime().isAfter(now));
        }
        req.setAttribute("dates", dates);
        req.setAttribute("chosenDate", chosen);
        req.setAttribute("slots", slots);
        req.getRequestDispatcher("/WEB-INF/jsp/checkout.jsp").forward(req, resp);
    }

    private void showHistory(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        List<Order> orders = orderDAO.findByUser(user(req).getId());
        // which PICKED_UP orders already have feedback (to toggle the button)
        Map<Integer, Boolean> feedbackDone = new LinkedHashMap<>();
        for (Order o : orders) {
            if (Order.PICKED_UP.equals(o.getStatus())) {
                feedbackDone.put(o.getId(), feedbackDAO.existsForOrder(o.getId()));
            }
        }
        req.setAttribute("orders", orders);
        req.setAttribute("feedbackDone", feedbackDone);
        req.getRequestDispatcher("/WEB-INF/jsp/history.jsp").forward(req, resp);
    }

    private void showTrack(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        int id = Integer.parseInt(req.getParameter("id"));
        Order order = orderDAO.findById(id);
        // students may only track their own orders
        if (order == null || order.getUserId() != user(req).getId()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("order", order);
        req.getRequestDispatcher("/WEB-INF/jsp/track.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!"/checkout".equals(req.getServletPath())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            HttpSession session = req.getSession();
            Map<Integer, CartItem> cart = CartServlet.cartOf(session);
            if (cart.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/cart");
                return;
            }
            int slotId = Integer.parseInt(req.getParameter("slotId"));
            String note = req.getParameter("note");
            if (note != null && note.length() > 255) note = note.substring(0, 255);

            // placeOrder() is transactional and race-safe (see OrderDAO).
            int orderId = orderDAO.placeOrder(user(req).getId(), slotId, cart, note);
            cart.clear(); // order placed -> empty the cart
            resp.sendRedirect(req.getContextPath() + "/track?id=" + orderId);
        } catch (NumberFormatException e) {
            fail(req, resp, "Checkout failed", "Please choose a pickup slot.");
        } catch (Exception e) {
            // e.g. "slot just filled up" - show the message, it's user-actionable
            fail(req, resp, "Checkout failed", e.getMessage());
        }
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String t, String m)
            throws ServletException, IOException {
        req.setAttribute("errorTitle", t);
        req.setAttribute("errorMessage", m);
        req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
    }
}
