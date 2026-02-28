package oceanview.dao;

import oceanview.database.DBConnection;
import oceanview.model.Payment;
import oceanview.model.PaymentMethod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public int insert(Payment p) throws SQLException {
        String sql = """
                INSERT INTO payments
                  (reservation_id, amount, method, bank_id, bank_name,
                   card_last4, reference_no, comment, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,    p.getReservationId());
            ps.setDouble(2, p.getAmount());
            ps.setString(3, p.getMethod().name());

            if (p.getBankId() != null) ps.setInt(4, p.getBankId());
            else                       ps.setNull(4, Types.INTEGER);

            ps.setString(5, p.getBankName());
            ps.setString(6, p.getCardLast4());
            ps.setString(7, p.getReferenceNo());
            ps.setString(8, p.getComment());
            ps.setString(9, p.getCreatedBy());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    public List<Payment> findByReservationId(int reservationId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE reservation_id = ? ORDER BY created_at";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            return toList(ps.executeQuery());
        }
    }

    public double sumByReservationId(int reservationId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    private List<Payment> toList(ResultSet rs) throws SQLException {
        List<Payment> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setReservationId(rs.getInt("reservation_id"));
        p.setAmount(rs.getDouble("amount"));
        p.setMethod(PaymentMethod.valueOf(rs.getString("method")));

        int bankId = rs.getInt("bank_id");
        if (!rs.wasNull()) p.setBankId(bankId);

        p.setBankName(rs.getString("bank_name"));
        p.setCardLast4(rs.getString("card_last4"));
        p.setReferenceNo(rs.getString("reference_no"));
        p.setComment(rs.getString("comment"));
        p.setCreatedBy(rs.getString("created_by"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());

        return p;
    }
}
