package oceanview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import oceanview.model.User;
import oceanview.service.SettingsService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Admin-only settings management.
 * GET  /settings  → show settings form
 * POST /settings  → save settings, reload AppSettings, redirect with msg
 */
public class SettingsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final SettingsService settingsService = new SettingsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isAdmin(req, resp)) return;

        try {
            Map<String, String> settings = settingsService.getAll();
            req.setAttribute("settings", settings);
            String msg = req.getParameter("msg") != null ? req.getParameter("msg") : "";
            req.setAttribute("msg", msg);
            req.getRequestDispatcher("/WEB-INF/views/settings/index.jsp").forward(req, resp);
        } catch (SettingsService.SettingsException e) {
            throw new ServletException("Error loading settings", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isAdmin(req, resp)) return;

        String currency    = trim(req.getParameter("currency"));
        String hotelName   = trim(req.getParameter("hotel_name"));
        String hotelAddress = trim(req.getParameter("hotel_address"));
        String hotelPhone  = trim(req.getParameter("hotel_phone"));
        String taxRateStr  = trim(req.getParameter("tax_rate"));

        // Validate tax rate
        double taxRate = 0.0;
        try {
            taxRate = Double.parseDouble(taxRateStr);
            if (taxRate < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            req.setAttribute("settings", buildMap(currency, hotelName, hotelAddress, hotelPhone, taxRateStr));
            req.setAttribute("msg", "");
            req.setAttribute("errorMessage", "Tax Rate must be a number greater than or equal to 0.");
            req.getRequestDispatcher("/WEB-INF/views/settings/index.jsp").forward(req, resp);
            return;
        }

        Map<String, String> settings = buildMap(currency, hotelName, hotelAddress, hotelPhone,
                String.valueOf(taxRate));
        try {
            settingsService.save(settings);
        } catch (SettingsService.SettingsException e) {
            req.setAttribute("settings", settings);
            req.setAttribute("msg", "");
            req.setAttribute("errorMessage", "Failed to save settings: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/settings/index.jsp").forward(req, resp);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/settings?msg=Settings+saved+successfully.");
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("loggedInUser") : null;
        if (user == null || !user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private String trim(String s) {
        return (s != null) ? s.trim() : "";
    }

    private Map<String, String> buildMap(String currency, String hotelName,
                                         String hotelAddress, String hotelPhone, String taxRate) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("currency",      currency);
        m.put("hotel_name",    hotelName);
        m.put("hotel_address", hotelAddress);
        m.put("hotel_phone",   hotelPhone);
        m.put("tax_rate",      taxRate);
        return m;
    }
}
