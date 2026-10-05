package com.canteen.servlet;

import com.canteen.dao.MenuItemDAO;
import com.canteen.model.CartItem;
import com.canteen.model.MenuItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Session-based cart. GET shows the cart; POST mutates it:
 * action=add | update | remove, with id and qty.
 * Prices are re-read from the database on every add, so a tampered
 * price in the request can never sneak into the cart.
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private final MenuItemDAO menuDAO = new MenuItemDAO();

    @SuppressWarnings("unchecked")
    static Map<Integer, CartItem> cartOf(HttpSession session) {
        Map<Integer, CartItem> cart =
                (Map<Integer, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Map<Integer, CartItem> cart = cartOf(req.getSession());
        req.setAttribute("cartTotal", total(cart));
        req.getRequestDispatcher("/WEB-INF/jsp/cart.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            int id = Integer.parseInt(req.getParameter("id"));
            Map<Integer, CartItem> cart = cartOf(req.getSession(true));

            switch (action == null ? "" : action) {
                case "add" -> {
                    int qty = parseQty(req.getParameter("qty"), 1);
                    MenuItem m = menuDAO.findById(id);
                    // only sell what is actually available right now
                    if (m == null || !m.isAvailable()) break;
                    CartItem existing = cart.get(id);
                    if (existing == null) {
                        cart.put(id, new CartItem(id, m.getName(), m.getPrice(), m.isVeg(), qty));
                    } else {
                        existing.setQty(Math.min(20, existing.getQty() + qty));
                    }
                }
                case "update" -> {
                    int qty = parseQty(req.getParameter("qty"), 1);
                    CartItem existing = cart.get(id);
                    if (existing != null) {
                        if (qty <= 0) cart.remove(id);
                        else existing.setQty(Math.min(20, qty));
                    }
                }
                case "remove" -> cart.remove(id);
                default -> { /* unknown action: ignore */ }
            }

            // back to where the user came from (menu or cart page)
            String back = req.getParameter("back");
            resp.sendRedirect(req.getContextPath() + ("/cart".equals(back) ? "/cart" : "/menu"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/menu");
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Cart error");
            req.setAttribute("errorMessage", "We could not update your cart. Please try again.");
            req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
        }
    }

    private int parseQty(String s, int def) {
        try {
            int q = Integer.parseInt(s);
            return (q < 1 || q > 20) ? def : q;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private BigDecimal total(Map<Integer, CartItem> cart) {
        return cart.values().stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
