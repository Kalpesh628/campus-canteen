package com.canteen.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Adds baseline security headers to every response.
 *
 * - X-Content-Type-Options: nosniff — stops MIME-sniffing attacks.
 * - X-Frame-Options: DENY — the app is never meant to be iframed
 *   (clickjacking defence).
 * - Referrer-Policy: strict-origin-when-cross-origin — don't leak full URLs.
 *
 * HSTS is set here (max-age 1 year): the site is HTTPS-only in
 * production and the edge does not add it for us.
 * Declared via @WebFilter so no web.xml edit is needed.
 */
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        if (resp instanceof HttpServletResponse) {
            HttpServletResponse h = (HttpServletResponse) resp;
            h.setHeader("X-Content-Type-Options", "nosniff");
            h.setHeader("X-Frame-Options", "DENY");
            h.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            h.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            if (req instanceof jakarta.servlet.http.HttpServletRequest) {
                String uri = ((jakarta.servlet.http.HttpServletRequest) req).getRequestURI();
                if (uri != null && uri.endsWith(".css")) {
                    h.setHeader("Cache-Control", "public, max-age=86400");
                }
            }
        }
        chain.doFilter(req, resp);
    }
}
