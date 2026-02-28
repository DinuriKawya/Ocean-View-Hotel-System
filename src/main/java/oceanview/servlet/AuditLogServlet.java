package oceanview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import oceanview.model.AuditLog;
import oceanview.model.User;
import oceanview.service.AuditLogService;

import java.io.IOException;
import java.util.List;

/**
 * Admin-only audit log viewer.
 *
 * GET /audit                      → first page, no filters
 * GET /audit?action=X&performedBy=Y&dateFrom=Z&dateTo=W&search=S&page=N
 */
@WebServlet("/audit")
public class AuditLogServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Admin guard
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("loggedInUser") : null;
        if (user == null || !user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Filter params
        String action      = nullIfBlank(req.getParameter("action"));
        String performedBy = nullIfBlank(req.getParameter("performedBy"));
        String dateFrom    = nullIfBlank(req.getParameter("dateFrom"));
        String dateTo      = nullIfBlank(req.getParameter("dateTo"));
        String search      = nullIfBlank(req.getParameter("search"));

        int page = 1;
        try { page = Integer.parseInt(req.getParameter("page")); } catch (Exception ignored) {}
        if (page < 1) page = 1;

        try {
            List<AuditLog> logs = auditLogService.getLogs(
                    action, performedBy, dateFrom, dateTo, search, page);
            int totalPages = auditLogService.getTotalPages(
                    action, performedBy, dateFrom, dateTo, search);
            int totalCount = auditLogService.getTotalCount(
                    action, performedBy, dateFrom, dateTo, search);
            List<String> actions     = auditLogService.getDistinctActions();
            List<String> performers  = auditLogService.getDistinctPerformedBy();

            req.setAttribute("logs",        logs);
            req.setAttribute("totalPages",  totalPages);
            req.setAttribute("totalCount",  totalCount);
            req.setAttribute("currentPage", page);
            req.setAttribute("actions",     actions);
            req.setAttribute("performers",  performers);

            // Echo filter params back so the form stays populated
            req.setAttribute("fAction",      action      != null ? action      : "");
            req.setAttribute("fPerformedBy", performedBy != null ? performedBy : "");
            req.setAttribute("fDateFrom",    dateFrom    != null ? dateFrom    : "");
            req.setAttribute("fDateTo",      dateTo      != null ? dateTo      : "");
            req.setAttribute("fSearch",      search      != null ? search      : "");

            req.getRequestDispatcher("/WEB-INF/views/audit/list.jsp")
               .forward(req, resp);

        } catch (Exception e) {
            throw new ServletException("Error loading audit logs", e);
        }
    }

    private String nullIfBlank(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }
}
