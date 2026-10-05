package com.canteen.filter;

import com.canteen.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Gate for every student-only URL (declared in web.xml).
 * No "user" in session -> bounce to the login page, remembering where
 * the user wanted to go so we can send them there after login (?next=...).
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest) req;
        HttpServletResponse hres = (HttpServletResponse) res;

        HttpSession session = hreq.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

        if (user == null) {
            String target = hreq.getRequestURI().substring(hreq.getContextPath().length());
            String qs = hreq.getQueryString();
            if (qs != null) target += "?" + qs;
            hres.sendRedirect(hreq.getContextPath() + "/login?next="
                    + URLEncoder.encode(target, StandardCharsets.UTF_8));
            return;
        }
        chain.doFilter(req, res);
    }
}
