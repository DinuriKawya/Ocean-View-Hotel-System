package oceanview.dao;

import oceanview.database.DBConnection;
import oceanview.model.BillingRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL-only data access layer for billing queries.
 * No business logic — pure CRUD / aggregation.
 */
public class BillingDAO {

    // ------------------------------------------------------------------
    // Billing list
    // ------------------------------------------------------------------

    public List<BillingRow> findBillingRows(String search,
                                            String dateFrom,
                                            String dateTo,
                                            String statusFilter) throws SQLException {

        String sql = """
            SELECT r.reservation_id, r.guest_name, r.guest_email, r.guest_phone,
                   r.room_number, r.room_type,
                   r.check_in_date, r.check_out_date,
                   DATEDIFF(r.check_out_date, r.check_in_date) AS nights,
                   r.total_amount AS room_charges,
                   COALESCE(ec.extra_total, 0) AS extra_charges,
                   r.total_amount + COALESCE(ec.extra_total, 0) AS total_due,
                   COALESCE(p.total_paid, 0) AS total_paid,
                   (r.total_amount + COALESCE(ec.extra_total,0)) - COALESCE(p.total_paid,0) AS balance,
                   r.status,
                   CASE
                     WHEN r.status NOT IN ('CHECKED_IN','CHECKED_OUT') THEN 'N/A'
                     WHEN COALESCE(p.total_paid,0) >= r.total_amount+COALESCE(ec.extra_total,0)-0.01 THEN 'PAID'
                     WHEN COALESCE(p.total_paid,0) > 0 THEN 'PARTIAL'
                     ELSE 'UNPAID'
                   END AS billing_status
            FROM reservations r
            LEFT JOIN (SELECT reservation_id, SUM(amount) AS extra_total
                       FROM extra_charges GROUP BY reservation_id) ec
                   ON ec.reservation_id = r.reservation_id
            LEFT JOIN (SELECT reservation_id, SUM(amount) AS total_paid
                       FROM payments GROUP BY reservation_id) p
                   ON p.reservation_id = r.reservation_id
            WHERE (? IS NULL OR r.guest_name LIKE ?)
              AND (? IS NULL OR DATE(r.check_in_date) >= ?)
              AND (? IS NULL OR DATE(r.check_out_date) <= ?)
              AND (? IS NULL OR r.status = ?)
            ORDER BY r.check_in_date DESC
            """;

        List<BillingRow> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchLike = search != null ? "%" + search + "%" : null;

            ps.setString(1, search);
            ps.setString(2, searchLike);
            ps.setString(3, dateFrom);
            ps.setString(4, dateFrom);
            ps.setString(5, dateTo);
            ps.setString(6, dateTo);
            ps.setString(7, statusFilter);
            ps.setString(8, statusFilter);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(mapRow(rs));
            }
        }
        return rows;
    }

    // ------------------------------------------------------------------
    // KPI helpers
    // ------------------------------------------------------------------

    /** Total payments received for the given period: "today", "month", or "year". */
    public double getTotalRevenue(String period) throws SQLException {
        String where = periodClause(period, "created_at");
        String sql = "SELECT COALESCE(SUM(amount),0) FROM payments WHERE " + where;
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    /** Number of checked-out reservations for the given period (uses check_out_date). */
    public int getCheckoutCount(String period) throws SQLException {
        String where = periodClause(period, "check_out_date") + " AND status='CHECKED_OUT'";
        String sql = "SELECT COUNT(*) FROM reservations WHERE " + where;
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ------------------------------------------------------------------
    // Chart data
    // ------------------------------------------------------------------

    /** Revenue by payment method — current month. Returns [method, total]. */
    public List<String[]> getRevenueByMethod() throws SQLException {
        String sql = """
            SELECT method, COALESCE(SUM(amount),0) AS total FROM payments
            WHERE YEAR(created_at)=YEAR(CURDATE()) AND MONTH(created_at)=MONTH(CURDATE())
            GROUP BY method ORDER BY total DESC
            """;
        return queryStringArrays(sql, 2);
    }

    /** Revenue by room type — current year. Returns [room_type, grand_revenue]. */
    public List<String[]> getRevenueByRoomType() throws SQLException {
        String sql = """
            SELECT r.room_type,
                   SUM(r.total_amount) + COALESCE(SUM(ec.extra_total),0) AS grand_revenue
            FROM reservations r
            LEFT JOIN (SELECT reservation_id, SUM(amount) AS extra_total
                       FROM extra_charges GROUP BY reservation_id) ec
                   ON ec.reservation_id = r.reservation_id
            WHERE r.status='CHECKED_OUT' AND YEAR(r.check_in_date)=YEAR(CURDATE())
            GROUP BY r.room_type ORDER BY grand_revenue DESC
            """;
        return queryStringArrays(sql, 2);
    }

    /** Daily revenue for the last N days. Returns [date_label, amount]. */
    public List<String[]> getDailyRevenue(int days) throws SQLException {
        String sql = """
            SELECT DATE(created_at) AS day, COALESCE(SUM(amount),0) AS revenue
            FROM payments
            WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
            GROUP BY DATE(created_at) ORDER BY day
            """;
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{ rs.getString(1), rs.getString(2) });
                }
            }
        }
        return list;
    }

    /** Recent payments. Returns [date, guest_name, method, amount]. */
    public List<String[]> getRecentPayments(int limit) throws SQLException {
        String sql = """
            SELECT DATE(p.created_at), r.guest_name, p.method, p.amount
            FROM payments p JOIN reservations r ON r.reservation_id = p.reservation_id
            ORDER BY p.created_at DESC LIMIT ?
            """;
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                    });
                }
            }
        }
        return list;
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private BillingRow mapRow(ResultSet rs) throws SQLException {
        BillingRow b = new BillingRow();
        b.setReservationId(rs.getInt("reservation_id"));
        b.setGuestName(rs.getString("guest_name"));
        b.setGuestEmail(rs.getString("guest_email"));
        b.setGuestPhone(rs.getString("guest_phone"));
        b.setRoomNumber(rs.getInt("room_number"));
        b.setRoomType(rs.getString("room_type"));
        Date ci = rs.getDate("check_in_date");
        if (ci != null) b.setCheckInDate(ci.toLocalDate());
        Date co = rs.getDate("check_out_date");
        if (co != null) b.setCheckOutDate(co.toLocalDate());
        b.setNights(rs.getLong("nights"));
        b.setRoomCharges(rs.getDouble("room_charges"));
        b.setExtraCharges(rs.getDouble("extra_charges"));
        b.setTotalDue(rs.getDouble("total_due"));
        b.setTotalPaid(rs.getDouble("total_paid"));
        b.setBalance(rs.getDouble("balance"));
        b.setReservationStatus(rs.getString("status"));
        b.setBillingStatus(rs.getString("billing_status"));
        return b;
    }

    /** Returns a period WHERE clause for the given timestamp column. */
    private String periodClause(String period, String col) {
        return switch (period != null ? period : "month") {
            case "today" -> "DATE(" + col + ") = CURDATE()";
            case "year"  -> "YEAR(" + col + ") = YEAR(CURDATE())";
            default      -> "YEAR(" + col + ")=YEAR(CURDATE()) AND MONTH(" + col + ")=MONTH(CURDATE())";
        };
    }

    private List<String[]> queryStringArrays(String sql, int cols) throws SQLException {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st   = conn.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {
            while (rs.next()) {
                String[] row = new String[cols];
                for (int i = 0; i < cols; i++) row[i] = rs.getString(i + 1);
                list.add(row);
            }
        }
        return list;
    }
}
