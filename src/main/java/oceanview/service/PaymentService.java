package oceanview.service;

import oceanview.dao.BankDAO;
import oceanview.dao.ExtraChargeDAO;
import oceanview.dao.PaymentDAO;
import oceanview.dao.ReservationDAO;
import oceanview.model.*;
import oceanview.model.AppSettings;

import java.sql.SQLException;
import java.util.List;

/**
 * Handles check-in and check-out payment processing.
 */
public class PaymentService {

    private final PaymentDAO     paymentDAO     = new PaymentDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final BankDAO        bankDAO        = new BankDAO();
    private final ExtraChargeDAO extraChargeDAO = new ExtraChargeDAO();

    // -----------------------------------------------------------------------
    // Check-In: just confirm arrival — no payment collected at this stage.
    // Payment is collected in full at check-out.
    // -----------------------------------------------------------------------

    public void checkIn(int reservationId, String performedBy) throws PaymentException {
        try {
            Reservation res = reservationDAO.findById(reservationId);
            if (res == null)
                throw new PaymentException("Reservation #" + reservationId + " not found.");
            if (res.getStatus() != ReservationStatus.CONFIRMED)
                throw new PaymentException(
                    "Check-in requires a CONFIRMED reservation. Current status: "
                    + res.getStatus().getDisplayName());

            reservationDAO.updateStatus(reservationId, ReservationStatus.CHECKED_IN);

        } catch (SQLException e) {
            throw new PaymentException("Database error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Checkout step 1: save extra charges to DB (called on Generate Bill)
    // -----------------------------------------------------------------------

    public void saveExtraCharges(int reservationId, List<ExtraCharge> charges, String performedBy)
            throws PaymentException {

        if (charges == null || charges.isEmpty()) return;

        try {
            for (int i = 0; i < charges.size(); i++) {
                ExtraCharge ec = charges.get(i);
                String label = "Charge " + (i + 1);

                if (ec.getChargeType() == null || ec.getChargeType().isBlank())
                    throw new PaymentException(label + ": charge type is required.");
                if (ec.getAmount() <= 0)
                    throw new PaymentException(label + ": amount must be greater than zero.");

                ec.setReservationId(reservationId);
                ec.setAddedBy(performedBy);
                extraChargeDAO.insert(ec);
            }
        } catch (SQLException e) {
            throw new PaymentException("Database error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Checkout step 2: collect payment for extra charges + update status
    // Extra charges are already saved in DB at this point.
    // -----------------------------------------------------------------------

    public void processCheckOut(int reservationId, List<Payment> payments, String performedBy)
            throws PaymentException {

        try {
            Reservation res = reservationDAO.findById(reservationId);
            if (res == null)
                throw new PaymentException("Reservation #" + reservationId + " not found.");
            if (res.getStatus() != ReservationStatus.CHECKED_IN)
                throw new PaymentException(
                    "Check-out requires a CHECKED IN reservation. Current status: "
                    + res.getStatus().getDisplayName());

            // Full bill = room charges + any extra charges saved in DB
            double extraTotal = extraChargeDAO.sumByReservationId(reservationId);
            double totalDue   = res.getTotalAmount() + extraTotal;

            // Overpayment allowed at checkout (staff returns change)
            validatePayments(payments, totalDue, true);

            // Save checkout payments (if any)
            if (payments != null && !payments.isEmpty()) {
                enrichBankNames(payments);
                for (Payment p : payments) {
                    p.setReservationId(reservationId);
                    p.setCreatedBy(performedBy);
                    paymentDAO.insert(p);
                }
            }

            reservationDAO.updateStatus(reservationId, ReservationStatus.CHECKED_OUT);

        } catch (SQLException e) {
            throw new PaymentException("Database error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    public List<Payment> getPaymentsByReservation(int reservationId) throws PaymentException {
        try { return paymentDAO.findByReservationId(reservationId); }
        catch (SQLException e) { throw new PaymentException("Database error: " + e.getMessage()); }
    }

    public double getTotalPaid(int reservationId) throws PaymentException {
        try { return paymentDAO.sumByReservationId(reservationId); }
        catch (SQLException e) { throw new PaymentException("Database error: " + e.getMessage()); }
    }

    public List<ExtraCharge> getExtraChargesByReservation(int reservationId) throws PaymentException {
        try { return extraChargeDAO.findByReservationId(reservationId); }
        catch (SQLException e) { throw new PaymentException("Database error: " + e.getMessage()); }
    }

    public double getTotalExtraCharges(int reservationId) throws PaymentException {
        try { return extraChargeDAO.sumByReservationId(reservationId); }
        catch (SQLException e) { throw new PaymentException("Database error: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // Validation helpers
    // -----------------------------------------------------------------------

    /**
     * @param allowOverpayment true for checkout (cash change is fine);
     *                         false for check-in (must match exactly).
     */
    private void validatePayments(List<Payment> payments, double totalDue,
                                  boolean allowOverpayment)
            throws PaymentException {

        if (payments == null || payments.isEmpty())
            throw new PaymentException("At least one payment method is required.");

        double totalEntered = 0;
        for (int i = 0; i < payments.size(); i++) {
            Payment p = payments.get(i);
            String label = "Payment " + (i + 1);

            if (p.getAmount() <= 0)
                throw new PaymentException(label + ": Amount must be greater than zero.");

            if (p.getMethod() == PaymentMethod.CARD) {
                if (p.getBankId() == null)
                    throw new PaymentException(label + " (Card): Please select a bank.");
                if (p.getCardLast4() == null || !p.getCardLast4().matches("\\d{4}"))
                    throw new PaymentException(label + " (Card): Last 4 digits must be exactly 4 numbers.");
            }

            if (p.getMethod() == PaymentMethod.TRANSFER) {
                if (p.getBankId() == null)
                    throw new PaymentException(label + " (Transfer): Please select a bank.");
                if (p.getReferenceNo() == null || p.getReferenceNo().isBlank())
                    throw new PaymentException(label + " (Transfer): Reference number is required.");
            }

            totalEntered += p.getAmount();
        }

        String c = AppSettings.getCurrency();
        if (allowOverpayment) {
            // Checkout: overpayment is fine (staff gives change); underpayment is not
            if (totalEntered < totalDue - 0.01)
                throw new PaymentException(String.format(
                    "Payment insufficient. " + c + " %,.2f still required (entered " + c + " %,.2f).",
                    totalDue - totalEntered, totalEntered));
        } else {
            // Check-in: must match exactly
            if (Math.abs(totalEntered - totalDue) > 0.01)
                throw new PaymentException(String.format(
                    "Payment total (" + c + " %,.2f) must equal the reservation total (" + c + " %,.2f).",
                    totalEntered, totalDue));
        }
    }

    private void enrichBankNames(List<Payment> payments) throws SQLException {
        for (Payment p : payments) {
            if (p.getBankId() != null) {
                Bank bank = bankDAO.findById(p.getBankId());
                if (bank != null) p.setBankName(bank.getName());
            }
        }
    }

    // -----------------------------------------------------------------------
    // Checked exception
    // -----------------------------------------------------------------------

    public static class PaymentException extends Exception {
        public PaymentException(String message) { super(message); }
    }
}
