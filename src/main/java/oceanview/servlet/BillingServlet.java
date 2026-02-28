package oceanview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import oceanview.model.User;
import oceanview.service.BillingService;
import oceanview.service.BillingService.BillingException;

import java.io.IOException;
import java.util.Map;

/**
 * Admin-only Billing & Revenue servlet.
 *
 * GET /billing                          → billing list  (action absent or "list")
 * GET /billing?action=dashboard         → revenue dashboard
 * GET /billing?action=invoice&id=X      → printable invoice
 * GET /billing?action=folio&id=X        → chronological folio
 */
@WebServlet("/billing")
public class BillingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final BillingService billingService = new BillingService();

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

        String action = nullIfBlank(req.getParameter("action"));

        try {
            if ("dashboard".equals(action)) {
                showDashboard(req, resp);
            } else if ("invoice".equals(action)) {
                showInvoice(req, resp);
            } else if ("folio".equals(action)) {
                showFolio(req, resp);
            } else {
                showList(req, resp);
            }
        } catch (BillingException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/billing/list.jsp").forward(req, resp);
        }
    }

    // ------------------------------------------------------------------
    // Action handlers
    // ------------------------------------------------------------------

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, BillingException {

        String search   = nullIfBlank(req.getParameter("search"));
        String dateFrom = nullIfBlank(req.getParameter("dateFrom"));
        String dateTo   = nullIfBlank(req.getParameter("dateTo"));
        String status   = nullIfBlank(req.getParameter("status"));

        req.setAttribute("billingRows", billingService.getBillingList(search, dateFrom, dateTo, status));

        // Echo filters back so the form stays populated
        req.setAttribute("fSearch",   search   != null ? search   : "");
        req.setAttribute("fDateFrom", dateFrom != null ? dateFrom : "");
        req.setAttribute("fDateTo",   dateTo   != null ? dateTo   : "");
        req.setAttribute("fStatus",   status   != null ? status   : "");

        req.getRequestDispatcher("/WEB-INF/views/billing/list.jsp").forward(req, resp);
    }

    private void showDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, BillingException {

        Map<String, Object> stats = billingService.getDashboardStats();
        for (Map.Entry<String, Object> e : stats.entrySet()) {
            req.setAttribute(e.getKey(), e.getValue());
        }
        req.getRequestDispatcher("/WEB-INF/views/billing/dashboard.jsp").forward(req, resp);
    }

    private void showInvoice(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, BillingException {

        int id = parseId(req.getParameter("id"));
        if (id <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid reservation id.");
            return;
        }
        Map<String, Object> data = billingService.getInvoice(id);
        for (Map.Entry<String, Object> e : data.entrySet()) {
            req.setAttribute(e.getKey(), e.getValue());
        }
        req.getRequestDispatcher("/WEB-INF/views/billing/invoice.jsp").forward(req, resp);
    }

    private void showFolio(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, BillingException {

        int id = parseId(req.getParameter("id"));
        if (id <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid reservation id.");
            return;
        }
        Map<String, Object> data = billingService.getFolio(id);
        for (Map.Entry<String, Object> e : data.entrySet()) {
            req.setAttribute(e.getKey(), e.getValue());
        }
        req.getRequestDispatcher("/WEB-INF/views/billing/folio.jsp").forward(req, resp);
    }

    // ------------------------------------------------------------------
    // Utility
    // ------------------------------------------------------------------

    private String nullIfBlank(String s) {
        return (s != null && !s.isBlank()) ? s.trim() : null;
    }

    private int parseId(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }
}
