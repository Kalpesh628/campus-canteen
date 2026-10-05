package com.canteen.servlet;

import com.canteen.dao.MenuItemDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Public menu browsing with search / category / veg-nonveg filters. */
@WebServlet("/menu")
public class MenuServlet extends HttpServlet {

    private final MenuItemDAO dao = new MenuItemDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String q = req.getParameter("q");
            String category = req.getParameter("category");
            String veg = req.getParameter("veg"); // all | veg | nonveg
            req.setAttribute("items", dao.search(q, category, veg));
            req.setAttribute("categories", dao.distinctCategories());
            req.setAttribute("q", q == null ? "" : q);
            req.setAttribute("category", category == null ? "" : category);
            req.setAttribute("veg", veg == null ? "all" : veg);
            RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/jsp/menu.jsp");
            rd.forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Menu unavailable");
            req.setAttribute("errorMessage", "We could not load the menu right now. Please try again.");
            req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
        }
    }
}
