package oceanview.dao;

import oceanview.database.DBConnection;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsDAO {

    public Map<String, String> findAll() throws SQLException {
        String sql = "SELECT setting_key, setting_value FROM system_settings ORDER BY setting_key";
        Map<String, String> map = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        }
        return map;
    }

    public void update(String key, String value) throws SQLException {
        String sql = "INSERT INTO system_settings (setting_key, setting_value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, value);
            ps.executeUpdate();
        }
    }
}
