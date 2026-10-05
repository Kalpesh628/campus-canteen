package com.canteen.filter;

import com.canteen.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Gate for /admin/* (declared AFTER AuthFilter in web.xml, so a user is
 * guaranteed present). Non-admins get a friendly 403 page, never a stack trace.
 */
public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest) req;
        HttpServletResponse hres = (HttpServletResponse) res;

        HttpSession session = hreq.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

        if (user == null || !user.isAdmin()) {
            hres.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("errorTitle", "Access denied");
            req.setAttribute("errorMessage",
                    "This area is for canteen staff only. If you are staff, please log in with an admin account.");
            RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
            rd.forward(req, res);
            return;
        }
        chain.doFilter(req, res);
    }
}
