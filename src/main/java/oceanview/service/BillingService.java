package oceanview.service;

import oceanview.dao.BillingDAO;
import oceanview.dao.ExtraChargeDAO;
import oceanview.dao.PaymentDAO;
import oceanview.dao.ReservationDAO;
import oceanview.model.*;

import java.sql.SQLException;
import java.util.*;

public class BillingService {

    private final BillingDAO     billingDAO;
    private final ReservationDAO reservationDAO;
    private final PaymentDAO     paymentDAO;
    private final ExtraChargeDAO extraChargeDAO;

  
    public BillingService() {
        this.billingDAO     = new BillingDAO();
        this.reservationDAO = new ReservationDAO();
        this.paymentDAO     = new PaymentDAO();
        this.extraChargeDAO = new ExtraChargeDAO();
    }

  
    public BillingService(BillingDAO billingDAO, ReservationDAO reservationDAO,
                          PaymentDAO paymentDAO, ExtraChargeDAO extraChargeDAO) {
        this.billingDAO     = billingDAO;
        this.reservationDAO = reservationDAO;
        this.paymentDAO     = paymentDAO;
        this.extraChargeDAO = extraChargeDAO;
    }

    // ------------------------------------------------------------------
    // Billing list
    // ------------------------------------------------------------------

    public List<BillingRow> getBillingList(String search, String dateFrom,
                                           String dateTo, String status)
            throws BillingException {
        try {
            return billingDAO.findBillingRows(search, dateFrom, dateTo, status);
        } catch (SQLException e) {
            throw new BillingException("Database error loading billing list: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Dashboard stats
    // ------------------------------------------------------------------

    public Map<String, Object> getDashboardStats() throws BillingException {
        try {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("revenueToday",    billingDAO.getTotalRevenue("today"));
            stats.put("revenueMonth",    billingDAO.getTotalRevenue("month"));
            stats.put("revenueYear",     billingDAO.getTotalRevenue("year"));
            stats.put("checkoutsMonth",  billingDAO.getCheckoutCount("month"));
            stats.put("revenueByMethod", billingDAO.getRevenueByMethod());
            stats.put("revenueByType",   billingDAO.getRevenueByRoomType());
            stats.put("dailyRevenue",    billingDAO.getDailyRevenue(30));
            stats.put("recentPayments",  billingDAO.getRecentPayments(10));
            return stats;
        } catch (SQLException e) {
            throw new BillingException("Database error loading dashboard: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Invoice
    // ------------------------------------------------------------------

    public Map<String, Object> getInvoice(int reservationId) throws BillingException {
        try {
            Reservation res = reservationDAO.findById(reservationId);
            if (res == null)
                throw new BillingException("Reservation #" + reservationId + " not found.");

            List<Payment>     payments     = paymentDAO.findByReservationId(reservationId);
            List<ExtraCharge> extraCharges = extraChargeDAO.findByReservationId(reservationId);

            double extraTotal = extraCharges.stream().mapToDouble(ExtraCharge::getAmount).sum();
            double totalDue   = res.getTotalAmount() + extraTotal;
            double totalPaid  = payments.stream().mapToDouble(Payment::getAmount).sum();
            double balance    = totalDue - totalPaid;

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("reservation",  res);
            data.put("payments",     payments);
            data.put("extraCharges", extraCharges);
            data.put("extraTotal",   extraTotal);
            data.put("totalDue",     totalDue);
            data.put("totalPaid",    totalPaid);
            data.put("balance",      balance);
            return data;
        } catch (SQLException e) {
            throw new BillingException("Database error loading invoice: " + e.getMessage());
        }
    }

    public Map<String, Object> getFolio(int reservationId) throws BillingException {
        return getInvoice(reservationId);
    }

    // ------------------------------------------------------------------
    // Checked exception
    // ------------------------------------------------------------------

    public static class BillingException extends Exception {
        public BillingException(String message) { super(message); }
    }
}
