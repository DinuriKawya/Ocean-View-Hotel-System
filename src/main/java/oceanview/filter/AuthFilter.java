package oceanview.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import oceanview.model.Role;
import oceanview.model.User;

import java.io.IOException;

/**
 * Intercepts all requests:
 *  - Unauthenticated users are redirected to /login.
 *  - Routes under /admin/* are restricted to ADMIN role only.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    // Paths that do NOT require authentication
    private static final String[] PUBLIC_PATHS = { "/login", "/logout" };

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String path = req.getServletPath();

        // Allow public paths through
        for (String pub : PUBLIC_PATHS) {
            if (path.equals(pub) || path.startsWith(pub + "/")) {
                chain.doFilter(req, resp);
                return;
            }
        }

        // Also allow static resources (css, js, images)
        if (path.startsWith("/static/") || path.endsWith(".css")
                || path.endsWith(".js") || path.endsWith(".png")
                || path.endsWith(".jpg") || path.endsWith(".ico")) {
            chain.doFilter(req, resp);
            return;
        }

        // Check session
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // ADMIN-only guard for /admin/* paths
        if (path.startsWith("/admin") && user.getRole() != Role.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: ADMIN role required.");
            return;
        }

        chain.doFilter(req, resp);
    }
}
