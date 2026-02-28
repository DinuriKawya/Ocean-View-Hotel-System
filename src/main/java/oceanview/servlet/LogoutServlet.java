package oceanview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import oceanview.Audit.AuditLogger;
import oceanview.model.User;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("loggedInUser");
            if (user != null) {
                AuditLogger.log("LOGOUT", "users", user.getUserId(),
                        user.getUsername(), req.getRemoteAddr(),
                        user.getFullName() + " logged out");
            }
            session.invalidate();
        }

        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
