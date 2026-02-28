package oceanview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import oceanview.model.User;
import oceanview.service.ReportService;

import java.io.IOException;
import java.util.List;

/**
 * Admin-only reports & analytics.
 *
 * GET /reports                                  → empty filter form
 * GET /reports?type=staff&dateFrom=X&dateTo=Y   → staff-wise report
 * GET /reports?type=room&dateFrom=X&dateTo=Y    → room category report
 * GET /reports?type=payment&dateFrom=X&dateTo=Y → payment method report
 */
@WebServlet("/reports")
public class ReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("loggedInUser") : null;
        if (user == null || !user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String type     = req.getParameter("type");
        String dateFrom = req.getParameter("dateFrom");
        String dateTo   = req.getParameter("dateTo");

        req.setAttribute("type",     type     != null ? type     : "");
        req.setAttribute("dateFrom", dateFrom != null ? dateFrom : "");
        req.setAttribute("dateTo",   dateTo   != null ? dateTo   : "");

        if (type != null && !type.isBlank()
                && dateFrom != null && !dateFrom.isBlank()
                && dateTo   != null && !dateTo.isBlank()) {
            try {
                List<String[]> rows = reportService.getReport(type, dateFrom, dateTo);
                req.setAttribute("rows",      rows);
                req.setAttribute("headers",   reportService.getHeaders(type));
                req.setAttribute("title",     reportService.getTitle(type));
                req.setAttribute("moneyCols", reportService.getMoneyCols(type));
            } catch (Exception e) {
                req.setAttribute("errorMessage", "Error generating report: " + e.getMessage());
            }
        }

        req.getRequestDispatcher("/WEB-INF/views/reports/index.jsp").forward(req, resp);
    }
}
