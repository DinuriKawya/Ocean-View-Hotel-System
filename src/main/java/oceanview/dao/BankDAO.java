package oceanview.dao;

import oceanview.database.DBConnection;
import oceanview.model.Bank;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BankDAO {

    public int insert(Bank b) throws SQLException {
        String sql = "INSERT INTO banks (name, is_active) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getName());
            ps.setBoolean(2, b.isActive());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    public Bank findById(int id) throws SQLException {
        String sql = "SELECT * FROM banks WHERE bank_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public List<Bank> findAll() throws SQLException {
        String sql = "SELECT * FROM banks ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return toList(ps.executeQuery());
        }
    }

    /** Only ACTIVE banks — used in payment form dropdowns. */
    public List<Bank> findActive() throws SQLException {
        String sql = "SELECT * FROM banks WHERE is_active = 1 ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return toList(ps.executeQuery());
        }
    }

    public boolean update(Bank b) throws SQLException {
        String sql = "UPDATE banks SET name = ?, is_active = ? WHERE bank_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getName());
            ps.setBoolean(2, b.isActive());
            ps.setInt(3, b.getBankId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM banks WHERE bank_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Bank> toList(ResultSet rs) throws SQLException {
        List<Bank> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    private Bank map(ResultSet rs) throws SQLException {
        return new Bank(rs.getInt("bank_id"), rs.getString("name"), rs.getBoolean("is_active"));
    }
}
