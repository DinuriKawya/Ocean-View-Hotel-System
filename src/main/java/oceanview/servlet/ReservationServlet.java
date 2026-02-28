package oceanview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import oceanview.Audit.AuditLogger;
import oceanview.model.*;
import oceanview.service.ReservationService;
import oceanview.service.ReservationService.ReservationException;

import oceanview.model.Room;
import oceanview.service.RoomService;
import oceanview.service.RoomService.RoomException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Handles all Reservation CRUD operations.
 *
 * GET  /reservations              → list (search + filter)
 * GET  /reservations?action=new   → blank create form
 * GET  /reservations?action=edit&id=X → prefilled edit form
 * GET  /reservations?action=view&id=X → detail view
 *
 * POST /reservations  action=create → create new
 * POST /reservations  action=update → update existing
 * POST /reservations  action=cancel → cancel (STAFF + ADMIN)
 * POST /reservations  action=delete → hard delete (ADMIN only)
 * POST /reservations  action=status → change status (ADMIN only)
 */
@WebServlet("/reservations")
public class ReservationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ReservationService service     = new ReservationService();
    private final RoomService        roomService = new RoomService();

    // -----------------------------------------------------------------------
    // GET — read operations
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
                case "view" -> handleView(req, resp);
                default     -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }
        } catch (ReservationException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/reservations/list.jsp").forward(req, resp);
        }
    }

    // -----------------------------------------------------------------------
    // POST — write operations
    // -----------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        User   user   = currentUser(req);
        String ip     = req.getRemoteAddr();

        try {
            switch (action == null ? "" : action) {
                case "create" -> handleCreate(req, resp, user, ip);
                case "update" -> handleUpdate(req, resp, user, ip);
                case "cancel" -> handleCancel(req, resp, user, ip);
                case "delete" -> handleDelete(req, resp, user, ip);
                case "status" -> handleStatusChange(req, resp, user, ip);
                default       -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
            }
        } catch (ReservationException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("statuses",  ReservationStatus.values());
            try {
                req.setAttribute("rooms", roomService.getAllRooms());
            } catch (RoomException re) {
                req.setAttribute("rooms", java.util.Collections.emptyList());
            }
            req.getRequestDispatcher("/WEB-INF/views/reservations/form.jsp").forward(req, resp);
        }
    }

    // -----------------------------------------------------------------------
    // GET handlers
    // -----------------------------------------------------------------------

    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, ReservationException {

        String search       = req.getParameter("search");
        String statusFilter = req.getParameter("status");

        List<Reservation> list;
        if (search != null && !search.isBlank()) {
            list = service.searchByGuestName(search.trim());
        } else if (statusFilter != null && !statusFilter.isBlank()) {
            list = service.getByStatus(ReservationStatus.valueOf(statusFilter));
        } else {
            list = service.getAllReservations();
        }

        req.setAttribute("reservations", list);
        req.setAttribute("statuses",     ReservationStatus.values());
        req.getRequestDispatcher("/WEB-INF/views/reservations/list.jsp").forward(req, resp);
    }

    private void handleNew(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("rooms",     roomService.getAvailableRooms());
        } catch (RoomException e) {
            req.setAttribute("rooms", java.util.Collections.emptyList());
        }
        req.setAttribute("statuses", ReservationStatus.values());
        req.getRequestDispatcher("/WEB-INF/views/reservations/form.jsp").forward(req, resp);
    }

    private void handleEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, ReservationException {
        int id = parseId(req);
        req.setAttribute("reservation", service.getById(id));
        try {
            req.setAttribute("rooms", roomService.getAllRooms());
        } catch (RoomException e) {
            req.setAttribute("rooms", java.util.Collections.emptyList());
        }
        req.setAttribute("statuses", ReservationStatus.values());
        req.getRequestDispatcher("/WEB-INF/views/reservations/form.jsp").forward(req, resp);
    }

    private void handleView(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, ReservationException {
        int id = parseId(req);
        req.setAttribute("reservation", service.getById(id));
        req.setAttribute("statuses",    ReservationStatus.values());
        req.getRequestDispatcher("/WEB-INF/views/reservations/view.jsp").forward(req, resp);
    }

    // -----------------------------------------------------------------------
    // POST handlers
    // -----------------------------------------------------------------------

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp,
                              User user, String ip)
            throws IOException, ReservationException, ServletException {

        Reservation r = buildFromForm(req);
        service.createReservation(r, user.getUsername());

        AuditLogger.log("CREATE", "reservations", r.getReservationId(),
                user.getUsername(), ip,
                "Created reservation for " + r.getGuestName());

        resp.sendRedirect(req.getContextPath() + "/reservations?msg=Reservation+created+successfully.");
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp,
                              User user, String ip)
            throws IOException, ReservationException, ServletException {

        int id = Integer.parseInt(req.getParameter("reservationId"));

        // Load existing to preserve status + createdBy
        Reservation existing = service.getById(id);
        Reservation updated  = buildFromForm(req);
        updated.setReservationId(id);
        updated.setStatus(existing.getStatus());
        updated.setCreatedBy(existing.getCreatedBy());
        updated.setCreatedAt(existing.getCreatedAt());

        service.updateReservation(updated);

        AuditLogger.log("UPDATE", "reservations", id,
                user.getUsername(), ip,
                "Updated reservation for " + updated.getGuestName());

        resp.sendRedirect(req.getContextPath() + "/reservations?action=view&id=" + id
                + "&msg=Reservation+updated+successfully.");
    }

    private void handleCancel(HttpServletRequest req, HttpServletResponse resp,
                              User user, String ip)
            throws IOException, ReservationException {

        int id = parseId(req);
        service.cancelReservation(id);

        AuditLogger.log("CANCEL", "reservations", id,
                user.getUsername(), ip, "Cancelled reservation #" + id);

        resp.sendRedirect(req.getContextPath() + "/reservations?msg=Reservation+cancelled.");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp,
                              User user, String ip)
            throws IOException, ReservationException, ServletException {

        if (!user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "ADMIN role required.");
            return;
        }

        int id = parseId(req);
        service.deleteReservation(id);

        AuditLogger.log("DELETE", "reservations", id,
                user.getUsername(), ip, "Deleted reservation #" + id);

        resp.sendRedirect(req.getContextPath() + "/reservations?msg=Reservation+deleted.");
    }

    private void handleStatusChange(HttpServletRequest req, HttpServletResponse resp,
                                    User user, String ip)
            throws IOException, ReservationException, ServletException {

        if (!user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "ADMIN role required.");
            return;
        }

        int id = parseId(req);
        ReservationStatus newStatus = ReservationStatus.valueOf(req.getParameter("newStatus"));
        service.changeStatus(id, newStatus);

        AuditLogger.log("STATUS", "reservations", id,
                user.getUsername(), ip,
                "Changed reservation #" + id + " status to " + newStatus);

        resp.sendRedirect(req.getContextPath() + "/reservations?action=view&id=" + id
                + "&msg=Status+updated.");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Parses the reservation form parameters into a Reservation object. */
    private Reservation buildFromForm(HttpServletRequest req) {
        Reservation r = new Reservation();
        r.setGuestName(req.getParameter("guestName"));
        r.setGuestEmail(req.getParameter("guestEmail"));
        r.setGuestPhone(req.getParameter("guestPhone"));

        String roomNumberStr = req.getParameter("roomNumber");
        r.setRoomNumber(roomNumberStr != null && !roomNumberStr.isBlank()
                ? Integer.parseInt(roomNumberStr) : 0);

        String roomTypeStr = req.getParameter("roomType");
        if (roomTypeStr != null && !roomTypeStr.isBlank())
            r.setRoomType(RoomType.valueOf(roomTypeStr));

        String checkIn  = req.getParameter("checkInDate");
        String checkOut = req.getParameter("checkOutDate");
        if (checkIn  != null && !checkIn.isBlank())  r.setCheckInDate(LocalDate.parse(checkIn));
        if (checkOut != null && !checkOut.isBlank()) r.setCheckOutDate(LocalDate.parse(checkOut));

        String guestsStr = req.getParameter("numberOfGuests");
        r.setNumberOfGuests(guestsStr != null && !guestsStr.isBlank()
                ? Integer.parseInt(guestsStr) : 1);

        String amountStr = req.getParameter("totalAmount");
        r.setTotalAmount(amountStr != null && !amountStr.isBlank()
                ? Double.parseDouble(amountStr) : 0.0);

        r.setSpecialRequests(req.getParameter("specialRequests"));
        return r;
    }

    private int parseId(HttpServletRequest req) {
        return Integer.parseInt(req.getParameter("id"));
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession(false).getAttribute("loggedInUser");
    }
}
