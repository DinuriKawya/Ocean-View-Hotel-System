package oceanview.dao;

import oceanview.database.DBConnection;
import oceanview.model.Room;
import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Three-tier role: DATA ACCESS LAYER — all SQL for the rooms table.
 */
public class RoomDAO {

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------

    public int insert(Room r) throws SQLException {
        String sql = """
                INSERT INTO rooms (room_number, room_type, price_per_night, status, floor, description)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,    r.getRoomNumber());
            ps.setString(2, r.getRoomType().name());
            ps.setDouble(3, r.getPricePerNight());
            ps.setString(4, r.getStatus().name());
            ps.setInt(5,    r.getFloor());
            ps.setString(6, r.getDescription());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    // -----------------------------------------------------------------------
    // READ
    // -----------------------------------------------------------------------

    public Room findById(int id) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public Room findByRoomNumber(int roomNumber) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    public List<Room> findAll() throws SQLException {
        String sql = "SELECT * FROM rooms ORDER BY room_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return toList(ps.executeQuery());
        }
    }

    public List<Room> findAvailable() throws SQLException {
        String sql = "SELECT * FROM rooms WHERE status = 'AVAILABLE' ORDER BY room_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return toList(ps.executeQuery());
        }
    }

    /**
     * Returns rooms that are AVAILABLE and have no confirmed reservation
     * overlapping the given date range.
     * Overlap condition:  existing.checkIn < requestedCheckOut
     *                 AND existing.checkOut > requestedCheckIn
     */
    public List<Room> findAvailableForDates(LocalDate checkIn, LocalDate checkOut)
            throws SQLException {

        String sql = """
                SELECT r.* FROM rooms r
                WHERE r.status = 'AVAILABLE'
                AND r.room_number NOT IN (
                    SELECT res.room_number FROM reservations res
                    WHERE res.status NOT IN ('CANCELLED','CHECKED_OUT','NO_SHOW')
                    AND res.check_in_date  < ?
                    AND res.check_out_date > ?
                )
                ORDER BY r.room_number
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(checkOut));
            ps.setDate(2, Date.valueOf(checkIn));
            return toList(ps.executeQuery());
        }
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------

    public boolean update(Room r) throws SQLException {
        String sql = """
                UPDATE rooms SET room_number = ?, room_type = ?, price_per_night = ?,
                                 status = ?, floor = ?, description = ?
                WHERE room_id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    r.getRoomNumber());
            ps.setString(2, r.getRoomType().name());
            ps.setDouble(3, r.getPricePerNight());
            ps.setString(4, r.getStatus().name());
            ps.setInt(5,    r.getFloor());
            ps.setString(6, r.getDescription());
            ps.setInt(7,    r.getRoomId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int roomId, RoomStatus status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------

    public boolean delete(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private List<Room> toList(ResultSet rs) throws SQLException {
        List<Room> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    private Room map(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id"));
        r.setRoomNumber(rs.getInt("room_number"));
        r.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        r.setPricePerNight(rs.getDouble("price_per_night"));
        r.setStatus(RoomStatus.valueOf(rs.getString("status")));
        r.setFloor(rs.getInt("floor"));
        r.setDescription(rs.getString("description"));
        return r;
    }
}
