package com.canteen.servlet;

import com.canteen.dao.PickupSlotDAO;
import com.canteen.model.PickupSlot;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

/**
 * Tiny JSON endpoint used by the checkout page: when the student changes the
 * date, JS fetches /slots?date=YYYY-MM-DD and rebuilds the slot dropdown
 * without a full page reload. (No JSON library - hand-built, viva-simple.)
 */
@WebServlet("/slots")
public class SlotServlet extends HttpServlet {

    private final PickupSlotDAO slotDAO = new PickupSlotDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            LocalDate date;
            try {
                date = LocalDate.parse(req.getParameter("date"));
            } catch (Exception e) {
                date = LocalDate.now();
            }
            List<PickupSlot> slots = slotDAO.findByDateWithRemaining(date);
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < slots.size(); i++) {
                PickupSlot s = slots.get(i);
                if (i > 0) json.append(',');
                json.append("{\"id\":").append(s.getId())
                    .append(",\"label\":\"").append(s.getLabel()).append('"')
                    .append(",\"remaining\":").append(s.getRemaining())
                    .append('}');
            }
            out.print(json.append(']'));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
