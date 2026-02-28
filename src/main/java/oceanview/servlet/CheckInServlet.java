package oceanview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import oceanview.Audit.AuditLogger;
import oceanview.model.*;
import oceanview.service.*;
import oceanview.service.PaymentService.PaymentException;
import oceanview.service.ReservationService.ReservationException;

import java.io.IOException;

/**
 * GET  /checkin          → search form
 * GET  /checkin?id=X     → load reservation, show confirmation form
 * POST /checkin          → confirm check-in (no payment collected here)
 */
@WebServlet("/checkin")
public class CheckInServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ReservationService reservationService = new ReservationService();
    private final PaymentService     paymentService     = new PaymentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isBlank()) {
            req.getRequestDispatcher("/WEB-INF/views/checkin/form.jsp").forward(req, resp);
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Reservation res = reservationService.getById(id);
            req.setAttribute("reservation", res);
            req.getRequestDispatcher("/WEB-INF/views/checkin/form.jsp").forward(req, resp);
        } catch (ReservationException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/checkin/form.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User   user = currentUser(req);
        String ip   = req.getRemoteAddr();

        int reservationId;
        try {
            reservationId = Integer.parseInt(req.getParameter("reservationId"));
        } catch (NumberFormatException e) {
            req.setAttribute("errorMessage", "Invalid reservation ID.");
            req.getRequestDispatcher("/WEB-INF/views/checkin/form.jsp").forward(req, resp);
            return;
        }

        try {
            paymentService.checkIn(reservationId, user.getUsername());

            AuditLogger.log("CHECK_IN", "reservations", reservationId,
                    user.getUsername(), ip,
                    "Guest checked in for reservation #" + reservationId);

            resp.sendRedirect(req.getContextPath()
                    + "/reservations?action=view&id=" + reservationId
                    + "&msg=Guest+checked+in+successfully.");

        } catch (PaymentException e) {
            try {
                req.setAttribute("errorMessage", e.getMessage());
                req.setAttribute("reservation", reservationService.getById(reservationId));
                req.getRequestDispatcher("/WEB-INF/views/checkin/form.jsp").forward(req, resp);
            } catch (ReservationException ex) {
                req.setAttribute("errorMessage", ex.getMessage());
                req.getRequestDispatcher("/WEB-INF/views/checkin/form.jsp").forward(req, resp);
            }
        }
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession(false).getAttribute("loggedInUser");
    }
}
