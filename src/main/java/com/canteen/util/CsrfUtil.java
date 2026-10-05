package com.canteen.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Minimal synchronizer-token CSRF protection.
 *
 * A random token is minted once per session and embedded as a hidden field in
 * forms; POST handlers reject requests whose token doesn't match. GETs never
 * change state, so they don't need the token.
 *
 * Usage in a servlet:
 *   // doGet: req.setAttribute("csrfToken", CsrfUtil.tokenFor(req.getSession(true)));
 *   // JSP:   <input type="hidden" name="csrfToken" value="${csrfToken}">
 *   // doPost: if (!CsrfUtil.valid(req)) { // reject }
 */
public final class CsrfUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ATTR = "csrfToken";

    private CsrfUtil() {}

    /** Returns the session's token, minting one on first use. */
    public static String tokenFor(HttpSession session) {
        String token = (String) session.getAttribute(ATTR);
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = HexFormat.of().formatHex(bytes);
            session.setAttribute(ATTR, token);
        }
        return token;
    }

    /** True when the request carries the session's token. */
    public static boolean valid(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        String expected = (String) session.getAttribute(ATTR);
        String got = req.getParameter(ATTR);
        return expected != null && !expected.isEmpty() && expected.equals(got);
    }
}
