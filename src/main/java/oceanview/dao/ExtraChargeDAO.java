package oceanview.dao;

import oceanview.database.DBConnection;
import oceanview.model.ExtraCharge;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExtraChargeDAO {

    public void insert(ExtraCharge charge) throws SQLException {
        String sql = "INSERT INTO extra_charges "
                   + "(reservation_id, charge_type, description, amount, added_by) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, charge.getReservationId());
            ps.setString(2, charge.getChargeType());
            ps.setString(3, charge.getDescription());
            ps.setDouble(4, charge.getAmount());
            ps.setString(5, charge.getAddedBy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) charge.setChargeId(rs.getInt(1));
            }
        }
    }

    public List<ExtraCharge> findByReservationId(int reservationId) throws SQLException {
        String sql = "SELECT * FROM extra_charges WHERE reservation_id = ? ORDER BY created_at";
        List<ExtraCharge> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public double sumByReservationId(int reservationId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM extra_charges WHERE reservation_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    private ExtraCharge map(ResultSet rs) throws SQLException {
        ExtraCharge e = new ExtraCharge();
        e.setChargeId(rs.getInt("charge_id"));
        e.setReservationId(rs.getInt("reservation_id"));
        e.setChargeType(rs.getString("charge_type"));
        e.setDescription(rs.getString("description"));
        e.setAmount(rs.getDouble("amount"));
        e.setAddedBy(rs.getString("added_by"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) e.setCreatedAt(ts.toLocalDateTime());
        return e;
    }
}
