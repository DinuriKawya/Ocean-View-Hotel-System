package oceanview.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import oceanview.Audit.AuditLogger;
import oceanview.model.*;
import oceanview.service.*;
import oceanview.service.PaymentService.PaymentException;
import oceanview.service.ReservationService.ReservationException;
import oceanview.service.BankService.BankException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GET  /checkout               → search form
 * GET  /checkout?id=X          → checkout form (add extra charges)
 * GET  /checkout?action=bill&id=X → bill preview page
 * POST /checkout action=generateBill → save new extra charges, redirect to bill
 * POST /checkout action=confirm     → save payments, complete check-out
 */
@WebServlet("/checkout")
public class CheckOutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ReservationService reservationService = new ReservationService();
    private final PaymentService     paymentService     = new PaymentService();
    private final BankService        bankService        = new BankService();

    // -----------------------------------------------------------------------
    // GET
    // -----------------------------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action  = req.getParameter("action");
        String idParam = req.getParameter("id");

        // -- Bill preview --
        if ("bill".equals(action)) {
            if (idParam == null || idParam.isBlank()) {
                resp.sendRedirect(req.getContextPath() + "/checkout");
                return;
            }
            try {
                int id = Integer.parseInt(idParam);
                requireCheckedIn(id);
                loadCheckoutData(req, id);
                req.getRequestDispatcher("/WEB-INF/views/checkout/bill.jsp").forward(req, resp);
            } catch (Exception e) {
                req.setAttribute("errorMessage", e.getMessage());
                req.getRequestDispatcher("/WEB-INF/views/checkout/form.jsp").forward(req, resp);
            }
            return;
        }

        // -- Checkout form (with or without reservation) --
        if (idParam == null || idParam.isBlank()) {
            req.getRequestDispatcher("/WEB-INF/views/checkout/form.jsp").forward(req, resp);
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            requireCheckedIn(id);
            loadCheckoutData(req, id);
            req.getRequestDispatcher("/WEB-INF/views/checkout/form.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/checkout/form.jsp").forward(req, resp);
        }
    }

    // -----------------------------------------------------------------------
    // POST
    // -----------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User   user   = currentUser(req);
        String ip     = req.getRemoteAddr();
        String action = req.getParameter("action");
        int    id;

        try {
            id = Integer.parseInt(req.getParameter("reservationId"));
        } catch (NumberFormatException e) {
            req.setAttribute("errorMessage", "Invalid reservation ID.");
            req.getRequestDispatcher("/WEB-INF/views/checkout/form.jsp").forward(req, resp);
            return;
        }

        switch (action == null ? "" : action) {

            // ---- Save extra charges, redirect to bill preview ----
            case "generateBill" -> {
                try {
                    requireCheckedIn(id);
                    List<ExtraCharge> newCharges = parseExtraCharges(req);
                    paymentService.saveExtraCharges(id, newCharges, user.getUsername());

                    if (!newCharges.isEmpty()) {
                        AuditLogger.log("EXTRA_CHARGES", "reservations", id,
                                user.getUsername(), ip,
                                "Added " + newCharges.size() + " extra charge(s) for reservation #" + id);
                    }

                    resp.sendRedirect(req.getContextPath() + "/checkout?action=bill&id=" + id);

                } catch (PaymentException e) {
                    try {
                        req.setAttribute("errorMessage", e.getMessage());
                        loadCheckoutData(req, id);
                        req.getRequestDispatcher("/WEB-INF/views/checkout/form.jsp").forward(req, resp);
                    } catch (Exception ex) {
                        req.setAttribute("errorMessage", ex.getMessage());
                        req.getRequestDispatcher("/WEB-INF/views/checkout/form.jsp").forward(req, resp);
                    }
                }
            }

            // ---- Collect payment + confirm check-out ----
            case "confirm" -> {
                try {
                    List<Payment> payments = parsePaymentRows(req);
                    paymentService.processCheckOut(id, payments, user.getUsername());

                    AuditLogger.log("CHECK_OUT", "reservations", id,
                            user.getUsername(), ip,
                            "Guest checked out from reservation #" + id);

                    resp.sendRedirect(req.getContextPath()
                            + "/reservations?action=view&id=" + id
                            + "&msg=Guest+checked+out+successfully.");

                } catch (PaymentException e) {
                    try {
                        req.setAttribute("errorMessage", e.getMessage());
                        loadCheckoutData(req, id);
                        req.getRequestDispatcher("/WEB-INF/views/checkout/bill.jsp").forward(req, resp);
                    } catch (Exception ex) {
                        req.setAttribute("errorMessage", ex.getMessage());
                        req.getRequestDispatcher("/WEB-INF/views/checkout/bill.jsp").forward(req, resp);
                    }
                }
            }

            default -> resp.sendError(400, "Unknown action.");
        }
    }

    // -----------------------------------------------------------------------
    // Status guard — throws if reservation is not CHECKED_IN
    // -----------------------------------------------------------------------

    private void requireCheckedIn(int id) throws PaymentException {
        try {
            Reservation res = reservationService.getById(id);
            if (res.getStatus() != ReservationStatus.CHECKED_IN) {
                throw new PaymentException(
                    "Reservation #" + id + " cannot be checked out. " +
                    "Current status: " + res.getStatus().getDisplayName() +
                    ". Only CHECKED IN reservations can be checked out.");
            }
        } catch (ReservationService.ReservationException e) {
            throw new PaymentException(e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Shared data loader
    // -----------------------------------------------------------------------

    private void loadCheckoutData(HttpServletRequest req, int id) throws Exception {
        try {
            req.setAttribute("reservation",  reservationService.getById(id));
            req.setAttribute("extraCharges", paymentService.getExtraChargesByReservation(id));
            req.setAttribute("totalExtra",   paymentService.getTotalExtraCharges(id));
            req.setAttribute("activeBanks",  bankService.getActiveBanks());
        } catch (ReservationException | PaymentException | BankException e) {
            throw new Exception(e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Parse extra charge arrays: chargeType[], chargeDesc[], chargeAmount[]
    // -----------------------------------------------------------------------

    private List<ExtraCharge> parseExtraCharges(HttpServletRequest req) {
        String[] types   = req.getParameterValues("chargeType[]");
        String[] descs   = req.getParameterValues("chargeDesc[]");
        String[] amounts = req.getParameterValues("chargeAmount[]");

        List<ExtraCharge> list = new ArrayList<>();
        if (types == null) return list;

        for (int i = 0; i < types.length; i++) {
            String type = get(types, i);
            double amt  = parseDouble(amounts, i);
            if (type == null || amt <= 0) continue;   // skip empty/invalid rows

            ExtraCharge ec = new ExtraCharge();
            ec.setChargeType(type);
            ec.setDescription(get(descs, i));
            ec.setAmount(amt);
            list.add(ec);
        }
        return list;
    }

    // -----------------------------------------------------------------------
    // Parse payment rows: paymentMethod[] + paymentAmount[] + ...
    // -----------------------------------------------------------------------

    private List<Payment> parsePaymentRows(HttpServletRequest req) {
        String[] methods    = req.getParameterValues("paymentMethod[]");
        String[] amounts    = req.getParameterValues("paymentAmount[]");
        String[] bankIds    = req.getParameterValues("paymentBankId[]");
        String[] cardLast4s = req.getParameterValues("paymentCardLast4[]");
        String[] references = req.getParameterValues("paymentReference[]");
        String[] comments   = req.getParameterValues("paymentComment[]");

        List<Payment> list = new ArrayList<>();
        if (methods == null) return list;

        for (int i = 0; i < methods.length; i++) {
            Payment p = new Payment();
            p.setMethod(PaymentMethod.valueOf(methods[i]));
            p.setAmount(parseDouble(amounts, i));

            String bankIdStr = get(bankIds, i);
            if (bankIdStr != null) p.setBankId(Integer.parseInt(bankIdStr));

            p.setCardLast4(get(cardLast4s, i));
            p.setReferenceNo(get(references, i));
            p.setComment(get(comments, i));
            list.add(p);
        }
        return list;
    }

    private double parseDouble(String[] arr, int i) {
        try { return (arr != null && i < arr.length) ? Double.parseDouble(arr[i]) : 0; }
        catch (NumberFormatException e) { return 0; }
    }

    private String get(String[] arr, int i) {
        if (arr == null || i >= arr.length) return null;
        String v = arr[i];
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession(false).getAttribute("loggedInUser");
    }
}
