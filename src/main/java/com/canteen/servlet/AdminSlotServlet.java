package com.canteen.servlet;

import com.canteen.dao.PickupSlotDAO;
import com.canteen.model.PickupSlot;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

/** Admin pickup-slot management: create, enable/disable, delete. */
@WebServlet("/admin/slots")
public class AdminSlotServlet extends HttpServlet {

    private final PickupSlotDAO dao = new PickupSlotDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("slots", dao.findAll());
            req.getRequestDispatcher("/WEB-INF/jsp/admin/slots.jsp").forward(req, resp);
        } catch (Exception e) {
            fail(req, resp, "Slots unavailable", "We could not load the pickup slots.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            switch (action == null ? "" : action) {
                case "add" -> {
                    PickupSlot s = new PickupSlot();
                    s.setSlotDate(LocalDate.parse(req.getParameter("date")));
                    s.setStartTime(LocalTime.parse(req.getParameter("start")));
                    s.setEndTime(LocalTime.parse(req.getParameter("end")));
                    int max = Integer.parseInt(req.getParameter("maxOrders"));
                    if (max < 1 || max > 500) throw new IllegalArgumentException("maxOrders");
                    if (!s.getEndTime().isAfter(s.getStartTime()))
                        throw new IllegalArgumentException("End time must be after start time.");
                    s.setMaxOrders(max);
                    s.setActive(true);
                    dao.insert(s);
                }
                case "toggle" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    PickupSlot s = dao.findById(id);
                    if (s != null) dao.setActive(id, !s.isActive());
                }
                case "delete" -> dao.delete(Integer.parseInt(req.getParameter("id")));
                default -> { /* ignore */ }
            }
            resp.sendRedirect(req.getContextPath() + "/admin/slots");
        } catch (Exception e) {
            fail(req, resp, "Slot not saved",
                    e.getMessage() == null ? "Please check the values." : e.getMessage());
        }
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String t, String m)
            throws ServletException, IOException {
        req.setAttribute("errorTitle", t);
        req.setAttribute("errorMessage", m);
        req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
    }
}
