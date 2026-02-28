package oceanview.dao;

import oceanview.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    // -----------------------------------------------------------------------
    // Staff-wise: payments grouped by the staff who collected them
    // -----------------------------------------------------------------------
    public List<String[]> staffReport(String dateFrom, String dateTo) throws SQLException {
        String sql =
            "SELECT " +
            "    COALESCE(p.created_by, 'Unknown') AS staff_name, " +
            "    COUNT(DISTINCT p.reservation_id)  AS reservations, " +
            "    COUNT(*)                           AS transactions, " +
            "    SUM(p.amount)                      AS total_collected " +
            "FROM payments p " +
            "WHERE DATE(p.created_at) BETWEEN ? AND ? " +
            "GROUP BY p.created_by " +
            "ORDER BY total_collected DESC";
        return query(sql, dateFrom, dateTo);
    }

    // -----------------------------------------------------------------------
    // Room category-wise: checked-out reservations grouped by room_type
    // -----------------------------------------------------------------------
    public List<String[]> roomCategoryReport(String dateFrom, String dateTo) throws SQLException {
        String sql =
            "SELECT " +
            "    res.room_type                                          AS category, " +
            "    COUNT(*)                                               AS reservations, " +
            "    SUM(DATEDIFF(res.check_out_date, res.check_in_date))   AS total_nights, " +
            "    SUM(res.total_amount)                                  AS room_revenue, " +
            "    COALESCE(SUM(ec.extra_total), 0)                       AS extra_charges, " +
            "    SUM(res.total_amount) + COALESCE(SUM(ec.extra_total), 0) AS grand_revenue " +
            "FROM reservations res " +
            "LEFT JOIN ( " +
            "    SELECT reservation_id, SUM(amount) AS extra_total " +
            "    FROM extra_charges GROUP BY reservation_id " +
            ") ec ON ec.reservation_id = res.reservation_id " +
            "WHERE res.status = 'CHECKED_OUT' " +
            "  AND DATE(res.check_in_date) BETWEEN ? AND ? " +
            "GROUP BY res.room_type " +
            "ORDER BY grand_revenue DESC";
        return query(sql, dateFrom, dateTo);
    }

    // -----------------------------------------------------------------------
    // Payment method-wise: payments grouped by method (CASH / CARD / TRANSFER)
    // -----------------------------------------------------------------------
    public List<String[]> paymentMethodReport(String dateFrom, String dateTo) throws SQLException {
        String sql =
            "SELECT " +
            "    p.method                           AS payment_method, " +
            "    COUNT(DISTINCT p.reservation_id)  AS reservations, " +
            "    COUNT(*)                           AS transactions, " +
            "    SUM(p.amount)                      AS total_amount " +
            "FROM payments p " +
            "WHERE DATE(p.created_at) BETWEEN ? AND ? " +
            "GROUP BY p.method " +
            "ORDER BY total_amount DESC";
        return query(sql, dateFrom, dateTo);
    }

    // -----------------------------------------------------------------------
    // Generic query runner — returns every cell as a String
    // -----------------------------------------------------------------------
    private List<String[]> query(String sql, String dateFrom, String dateTo)
            throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dateFrom);
            ps.setString(2, dateTo);
            try (ResultSet rs = ps.executeQuery()) {
                int cols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    String[] row = new String[cols];
                    for (int i = 0; i < cols; i++) {
                        Object v = rs.getObject(i + 1);
                        row[i] = (v != null) ? v.toString() : "0";
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
