package com.canteen.servlet;

import com.canteen.dao.MenuItemDAO;
import com.canteen.model.MenuItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

/** Admin menu CRUD: add / edit / delete / availability toggle. */
@WebServlet("/admin/menu")
public class AdminMenuServlet extends HttpServlet {

    private final MenuItemDAO dao = new MenuItemDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String editId = req.getParameter("edit");
            if (editId != null && !editId.isBlank()) {
                MenuItem item = dao.findById(Integer.parseInt(editId));
                req.setAttribute("editItem", item);
            }
            req.setAttribute("items", dao.findAll());
            req.setAttribute("categories", dao.distinctCategories());
            req.getRequestDispatcher("/WEB-INF/jsp/admin/menu.jsp").forward(req, resp);
        } catch (Exception e) {
            fail(req, resp, "Menu unavailable", "We could not load the menu.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            switch (action == null ? "" : action) {
                case "add", "update" -> save(req, "update".equals(action));
                case "delete" -> {
                    try {
                        dao.delete(Integer.parseInt(req.getParameter("id")));
                    } catch (Exception fk) {
                        // item is referenced by old orders -> suggest the available flag
                        req.getSession().setAttribute("flash",
                                "Could not delete: this item exists in past orders. "
                              + "Mark it unavailable instead.");
                    }
                }
                case "toggle" -> {
                    MenuItem m = dao.findById(Integer.parseInt(req.getParameter("id")));
                    if (m != null) {
                        m.setAvailable(!m.isAvailable());
                        dao.update(m);
                    }
                }
                default -> { /* ignore */ }
            }
            resp.sendRedirect(req.getContextPath() + "/admin/menu");
        } catch (Exception e) {
            fail(req, resp, "Save failed", e.getMessage());
        }
    }

    private void save(HttpServletRequest req, boolean isUpdate) throws Exception {
        String name = req.getParameter("name");
        String category = req.getParameter("category");
        BigDecimal price = new BigDecimal(req.getParameter("price"));
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Category is required.");
        if (price.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Price must be positive.");

        MenuItem m = new MenuItem();
        if (isUpdate) m.setId(Integer.parseInt(req.getParameter("id")));
        m.setName(name.trim());
        m.setDescription(emptyToNull(req.getParameter("description")));
        m.setPrice(price);
        m.setCategory(category.trim());
        m.setVeg("on".equals(req.getParameter("veg")) || "true".equals(req.getParameter("veg")));
        m.setAvailable(!"on".equals(req.getParameter("unavailable")));
        m.setImageUrl(emptyToNull(req.getParameter("imageUrl")));

        if (isUpdate) dao.update(m); else dao.insert(m);
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String t, String m)
            throws ServletException, IOException {
        req.setAttribute("errorTitle", t);
        req.setAttribute("errorMessage", m);
        req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
    }
}
