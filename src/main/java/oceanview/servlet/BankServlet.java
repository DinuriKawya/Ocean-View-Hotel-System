package oceanview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import oceanview.Audit.AuditLogger;
import oceanview.model.Bank;
import oceanview.model.User;
import oceanview.service.BankService;

import java.io.IOException;
import java.util.Collections;

/** Admin-only bank management. */
@WebServlet("/banks")
public class BankServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final BankService bankService = new BankService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        requireAdmin(req, resp);
        String action = req.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "list" -> {
                    req.setAttribute("banks", bankService.getAllBanks());
                    req.setAttribute("msg", req.getParameter("msg"));
                    forward(req, resp, "/WEB-INF/views/banks/list.jsp");
                }
                case "new" -> forward(req, resp, "/WEB-INF/views/banks/form.jsp");
                case "edit" -> {
                    req.setAttribute("bank", bankService.getById(Integer.parseInt(req.getParameter("id"))));
                    forward(req, resp, "/WEB-INF/views/banks/form.jsp");
                }
                default -> resp.sendError(400);
            }
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("banks", Collections.emptyList());
            forward(req, resp, "/WEB-INF/views/banks/list.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        requireAdmin(req, resp);
        User   user = currentUser(req);
        String ip   = req.getRemoteAddr();
        String action = req.getParameter("action");

        try {
            switch (action == null ? "" : action) {
                case "create" -> {
                    Bank b = new Bank();
                    b.setName(req.getParameter("name").trim());
                    b.setActive(true);
                    bankService.createBank(b);
                    AuditLogger.log("CREATE", "banks", b.getBankId(), user.getUsername(), ip, "Added bank: " + b.getName());
                    resp.sendRedirect(req.getContextPath() + "/banks?msg=Bank+added+successfully.");
                }
                case "update" -> {
                    Bank b = new Bank();
                    b.setBankId(Integer.parseInt(req.getParameter("bankId")));
                    b.setName(req.getParameter("name").trim());
                    b.setActive("1".equals(req.getParameter("isActive")));
                    bankService.updateBank(b);
                    AuditLogger.log("UPDATE", "banks", b.getBankId(), user.getUsername(), ip, "Updated bank: " + b.getName());
                    resp.sendRedirect(req.getContextPath() + "/banks?msg=Bank+updated.");
                }
                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    bankService.deleteBank(id);
                    AuditLogger.log("DELETE", "banks", id, user.getUsername(), ip, "Deleted bank #" + id);
                    resp.sendRedirect(req.getContextPath() + "/banks?msg=Bank+deleted.");
                }
                default -> resp.sendError(400);
            }
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("banks", Collections.emptyList());
            forward(req, resp, "/WEB-INF/views/banks/list.jsp");
        }
    }

    private void requireAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        User u = currentUser(req);
        if (u == null || !u.isAdmin()) {
            try { resp.sendError(403); } catch (Exception ignored) {}
        }
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String path)
            throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, resp);
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute("loggedInUser");
    }
}
