package oceanview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import oceanview.Audit.AuditLogger;
import oceanview.model.Role;
import oceanview.model.User;
import oceanview.model.UserStatus;
import oceanview.service.UserService;
import oceanview.service.UserService.UserException;

import java.io.IOException;
import java.util.Collections;

/**
 * Admin-only user management.
 *
 * GET  /users                  → list all users
 * GET  /users?action=new       → new user form
 * GET  /users?action=edit&id=X → edit user form
 * POST /users action=create         → create user
 * POST /users action=update         → update full name + role
 * POST /users action=changePassword → change password
 * POST /users action=toggleStatus   → activate / deactivate
 * POST /users action=delete         → delete user
 */
@WebServlet("/users")
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UserService userService = new UserService();

    // -----------------------------------------------------------------------
    // GET
    // -----------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAdmin(req, resp)) return;

        String action = req.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "list" -> {
                    req.setAttribute("users",       userService.getAllUsers());
                    req.setAttribute("totalUsers",  userService.getAllUsers().size());
                    req.setAttribute("activeCount", userService.countByStatus(UserStatus.ACTIVE));
                    req.setAttribute("adminCount",  userService.countByRole(Role.ADMIN));
                    req.setAttribute("staffCount",  userService.countByRole(Role.STAFF));
                    req.setAttribute("msg",         req.getParameter("msg"));
                    forward(req, resp, "/WEB-INF/views/users/list.jsp");
                }
                case "new" -> forward(req, resp, "/WEB-INF/views/users/form.jsp");
                case "edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    req.setAttribute("editUser", userService.getById(id));
                    forward(req, resp, "/WEB-INF/views/users/form.jsp");
                }
                default -> resp.sendError(400);
            }
        } catch (UserException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("users", Collections.emptyList());
            forward(req, resp, "/WEB-INF/views/users/list.jsp");
        }
    }

    // -----------------------------------------------------------------------
    // POST
    // -----------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!requireAdmin(req, resp)) return;

        User   actor  = currentUser(req);
        String ip     = req.getRemoteAddr();
        String action = req.getParameter("action");

        try {
            switch (action == null ? "" : action) {

                case "create" -> {
                    Role role = Role.valueOf(req.getParameter("role"));
                    userService.createUser(
                        req.getParameter("username"),
                        req.getParameter("fullName"),
                        role,
                        req.getParameter("password"),
                        req.getParameter("confirmPassword")
                    );
                    AuditLogger.log("CREATE", "users", 0,
                            actor.getUsername(), ip,
                            "Created user: " + req.getParameter("username"));
                    resp.sendRedirect(req.getContextPath()
                            + "/users?msg=User+created+successfully.");
                }

                case "update" -> {
                    int  id   = Integer.parseInt(req.getParameter("userId"));
                    Role role = Role.valueOf(req.getParameter("role"));
                    userService.updateUser(id, req.getParameter("fullName"), role);
                    AuditLogger.log("UPDATE", "users", id,
                            actor.getUsername(), ip, "Updated user #" + id);
                    resp.sendRedirect(req.getContextPath()
                            + "/users?msg=User+updated+successfully.");
                }

                case "changePassword" -> {
                    int id = Integer.parseInt(req.getParameter("userId"));
                    userService.changePassword(id,
                        req.getParameter("newPassword"),
                        req.getParameter("confirmPassword")
                    );
                    AuditLogger.log("CHANGE_PASSWORD", "users", id,
                            actor.getUsername(), ip, "Changed password for user #" + id);
                    resp.sendRedirect(req.getContextPath()
                            + "/users?msg=Password+changed+successfully.");
                }

                case "toggleStatus" -> {
                    int        id     = Integer.parseInt(req.getParameter("userId"));
                    UserStatus status = UserStatus.valueOf(req.getParameter("status"));
                    userService.setStatus(id, status, actor.getUserId());
                    AuditLogger.log("STATUS", "users", id,
                            actor.getUsername(), ip,
                            "Set user #" + id + " to " + status.name());
                    resp.sendRedirect(req.getContextPath()
                            + "/users?msg=User+status+updated.");
                }

                case "delete" -> {
                    int id = Integer.parseInt(req.getParameter("userId"));
                    userService.deleteUser(id, actor.getUserId());
                    AuditLogger.log("DELETE", "users", id,
                            actor.getUsername(), ip, "Deleted user #" + id);
                    resp.sendRedirect(req.getContextPath()
                            + "/users?msg=User+deleted.");
                }

                default -> resp.sendError(400);
            }

        } catch (UserException e) {
            // Re-show the appropriate form with the error
            try {
                req.setAttribute("errorMessage", e.getMessage());
                if ("create".equals(action)) {
                    forward(req, resp, "/WEB-INF/views/users/form.jsp");
                } else if ("update".equals(action)) {
                    int id = Integer.parseInt(req.getParameter("userId"));
                    req.setAttribute("editUser", userService.getById(id));
                    forward(req, resp, "/WEB-INF/views/users/form.jsp");
                } else {
                    req.setAttribute("users", userService.getAllUsers());
                    req.setAttribute("activeCount", userService.countByStatus(UserStatus.ACTIVE));
                    req.setAttribute("adminCount",  userService.countByRole(Role.ADMIN));
                    req.setAttribute("staffCount",  userService.countByRole(Role.STAFF));
                    forward(req, resp, "/WEB-INF/views/users/list.jsp");
                }
            } catch (UserException ex) {
                req.setAttribute("errorMessage", ex.getMessage());
                forward(req, resp, "/WEB-INF/views/users/list.jsp");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Returns false and sends 403 if user is not admin. */
    private boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        User u = currentUser(req);
        if (u == null || !u.isAdmin()) {
            resp.sendError(403, "Admin access required.");
            return false;
        }
        return true;
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
