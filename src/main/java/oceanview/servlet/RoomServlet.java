package oceanview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import oceanview.Audit.AuditLogger;
import oceanview.model.Room;
import oceanview.model.RoomFactory;
import oceanview.model.RoomStatus;
import oceanview.model.RoomType;
import oceanview.model.User;
import oceanview.service.RoomService;
import oceanview.service.RoomService.RoomException;

import java.io.IOException;
import java.util.List;

@WebServlet("/rooms")
public class RoomServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final RoomService roomService = new RoomService();

    // -----------------------------------------------------------------------
    // GET
    // -----------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "list" -> handleList(req, resp);
                case "new"  -> handleNew(req, resp);
                case "edit" -> handleEdit(req, resp);
                case "json" -> handleJson(req, resp);
                default     -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }
        } catch (RoomException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/rooms/list.jsp").forward(req, resp);
        }
    }

    // -----------------------------------------------------------------------
    // POST
    // -----------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = currentUser(req);
        if (!user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "ADMIN role required.");
            return;
        }

        String action = req.getParameter("action");
        String ip     = req.getRemoteAddr();

        try {
            switch (action == null ? "" : action) {
                case "create" -> handleCreate(req, resp, user, ip);
                case "update" -> handleUpdate(req, resp, user, ip);
                case "delete" -> handleDelete(req, resp, user, ip);
                default       -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }
        } catch (RoomException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("roomTypes",    RoomType.values());
            req.setAttribute("roomStatuses", RoomStatus.values());
            req.getRequestDispatcher("/WEB-INF/views/rooms/form.jsp").forward(req, resp);
        }
    }

    // -----------------------------------------------------------------------
    // GET handlers
    // -----------------------------------------------------------------------

    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, RoomException {
        req.setAttribute("rooms",       roomService.getAllRooms());
        req.setAttribute("roomStatuses", RoomStatus.values());
        req.getRequestDispatcher("/WEB-INF/views/rooms/list.jsp").forward(req, resp);
    }

    private void handleNew(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Room defaultRoom = RoomFactory.createRoom(RoomType.STANDARD); 
        req.setAttribute("room",         defaultRoom);
        req.setAttribute("roomTypes",    RoomType.values());
        req.setAttribute("roomStatuses", RoomStatus.values());
        req.getRequestDispatcher("/WEB-INF/views/rooms/form.jsp").forward(req, resp);
    }

    private void handleEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, RoomException {
        int id = Integer.parseInt(req.getParameter("id"));
        req.setAttribute("room",         roomService.getById(id));
        req.setAttribute("roomTypes",    RoomType.values());
        req.setAttribute("roomStatuses", RoomStatus.values());
        req.getRequestDispatcher("/WEB-INF/views/rooms/form.jsp").forward(req, resp);
    }

    private void handleJson(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, RoomException {

        List<Room> rooms = roomService.getAllRooms();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            if (i > 0) json.append(",");
            json.append(String.format(
                "{\"roomId\":%d,\"roomNumber\":%d,\"roomType\":\"%s\",\"roomTypeDisplay\":\"%s\"," +
                "\"pricePerNight\":%.2f,\"floor\":%d,\"status\":\"%s\",\"description\":\"%s\"}",
                r.getRoomId(), r.getRoomNumber(),
                r.getRoomType().name(), r.getRoomType().getDisplayName(),
                r.getPricePerNight(), r.getFloor(),
                r.getStatus().name(),
                r.getDescription() != null ? r.getDescription().replace("\"","\\\"") : ""
            ));
        }
        json.append("]");

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(json.toString());
    }

    // -----------------------------------------------------------------------
    // POST handlers
    // -----------------------------------------------------------------------

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp,
                              User user, String ip)
            throws IOException, RoomException, ServletException {

        Room r = buildFromForm(req);
        roomService.createRoom(r);

        AuditLogger.log("CREATE", "rooms", r.getRoomId(),
                user.getUsername(), ip, "Created room " + r.getRoomNumber());

        resp.sendRedirect(req.getContextPath() + "/rooms?msg=Room+created+successfully.");
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp,
                              User user, String ip)
            throws IOException, RoomException, ServletException {

        int id = Integer.parseInt(req.getParameter("roomId"));
        Room r = buildFromForm(req);
        r.setRoomId(id);
        roomService.updateRoom(r);

        AuditLogger.log("UPDATE", "rooms", id, user.getUsername(), ip, "Updated room " + r.getRoomNumber());

        resp.sendRedirect(req.getContextPath() + "/rooms?msg=Room+updated+successfully.");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User user, String ip) 
     
    	throws IOException, RoomException {

        int id = Integer.parseInt(req.getParameter("id"));
        roomService.deleteRoom(id);

        AuditLogger.log("DELETE", "rooms", id, user.getUsername(), ip, "Deleted room #" + id);
        resp.sendRedirect(req.getContextPath() + "/rooms?msg=Room+deleted.");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Room buildFromForm(HttpServletRequest req) {
        return RoomFactory.createRoom(
            Integer.parseInt(req.getParameter("roomNumber")),
            RoomType.valueOf(req.getParameter("roomType")),
            Double.parseDouble(req.getParameter("pricePerNight")),
            Integer.parseInt(req.getParameter("floor")),
            RoomStatus.valueOf(req.getParameter("status")),
            req.getParameter("description")
        );
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession(false).getAttribute("loggedInUser");
    }
}
