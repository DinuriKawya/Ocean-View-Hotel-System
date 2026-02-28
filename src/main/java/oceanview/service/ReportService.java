package oceanview.service;

import oceanview.dao.ReportDAO;
import oceanview.model.AppSettings;

import java.sql.SQLException;
import java.util.List;

public class ReportService {

    private final ReportDAO reportDAO = new ReportDAO();

    // -----------------------------------------------------------------------
    // Column headers per report type
    // -----------------------------------------------------------------------
    public static String[] getStaffHeaders() {
        String c = AppSettings.getCurrency();
        return new String[]{ "Staff Name", "Reservations", "Transactions", "Total Collected (" + c + ")" };
    }
    public static String[] getRoomHeaders() {
        String c = AppSettings.getCurrency();
        return new String[]{ "Room Category", "Reservations", "Total Nights",
                "Room Revenue (" + c + ")", "Extra Charges (" + c + ")", "Grand Revenue (" + c + ")" };
    }
    public static String[] getPaymentHeaders() {
        String c = AppSettings.getCurrency();
        return new String[]{ "Payment Method", "Reservations", "Transactions", "Total Amount (" + c + ")" };
    }
    // Keep legacy static fields for backward compat
    public static final String[] STAFF_HEADERS   = { "Staff Name", "Reservations", "Transactions", "Total Collected" };
    public static final String[] ROOM_HEADERS    = { "Room Category", "Reservations", "Total Nights", "Room Revenue", "Extra Charges", "Grand Revenue" };
    public static final String[] PAYMENT_HEADERS = { "Payment Method", "Reservations", "Transactions", "Total Amount" };

    // 0-based indices of columns that hold money values
    public static final int[] STAFF_MONEY_COLS   = {3};
    public static final int[] ROOM_MONEY_COLS    = {3, 4, 5};
    public static final int[] PAYMENT_MONEY_COLS = {3};

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    public List<String[]> getReport(String type, String dateFrom, String dateTo)
            throws SQLException {
        switch (type) {
            case "staff":   return reportDAO.staffReport(dateFrom, dateTo);
            case "room":    return reportDAO.roomCategoryReport(dateFrom, dateTo);
            case "payment": return reportDAO.paymentMethodReport(dateFrom, dateTo);
            default: throw new IllegalArgumentException("Unknown report type: " + type);
        }
    }

    public String[] getHeaders(String type) {
        switch (type) {
            case "staff":   return getStaffHeaders();
            case "room":    return getRoomHeaders();
            case "payment": return getPaymentHeaders();
            default:        return new String[0];
        }
    }

    public String getTitle(String type) {
        switch (type) {
            case "staff":   return "Staff-Wise Revenue Report";
            case "room":    return "Room Category Revenue Report";
            case "payment": return "Payment Method Report";
            default:        return "Report";
        }
    }

    /** 0-based column indices that contain monetary values. */
    public int[] getMoneyCols(String type) {
        switch (type) {
            case "staff":   return STAFF_MONEY_COLS;
            case "room":    return ROOM_MONEY_COLS;
            case "payment": return PAYMENT_MONEY_COLS;
            default:        return new int[0];
        }
    }
}
