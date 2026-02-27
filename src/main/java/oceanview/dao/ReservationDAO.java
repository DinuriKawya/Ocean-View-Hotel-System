package oceanview.dao;

import oceanview.database.DBConnection;
import oceanview.model.Reservation;
import oceanview.model.ReservationStatus;
import oceanview.model.RoomType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Three-tier role: DATA ACCESS LAYER.
 * All SQL for the reservations table lives here.
 * No business rules — only CRUD.
 */
public class ReservationDAO {

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------

    public int insert(Reservation r) throws SQLException {
        String sql = """
                INSERT INTO reservations
                  (guest_name, guest_email, guest_phone, room_number, room_type,
                   check_in_date, check_out_date, number_of_guests, total_amount,
                   status, special_requests, created_by, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1,  r.getGuestName());
            ps.setString(2,  r.getGuestEmail());
            ps.setString(3,  r.getGuestPhone());
            ps.setInt(4,     r.getRoomNumber());
            ps.setString(5,  r.getRoomType().name());
            ps.setDate(6,    Date.valueOf(r.getCheckInDate()));
            ps.setDate(7,    Date.valueOf(r.getCheckOutDate()));
            ps.setInt(8,     r.getNumberOfGuests());
            ps.setDouble(9,  r.getTotalAmount());
            ps.setString(10, r.getStatus().name());
            ps.setString(11, r.getSpecialRequests());
            ps.setString(12, r.getCreatedBy());
            ps.setDate(13,   Date.valueOf(
                    r.getCreatedAt() != null ? r.getCreatedAt() : LocalDate.now()));

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            return -1;
        }
    }

    // -----------------------------------------------------------------------
    // READ — single
    // -----------------------------------------------------------------------

    public Reservation findById(int id) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        }
    }

    // -----------------------------------------------------------------------
    // READ — list
    // -----------------------------------------------------------------------

    public List<Reservation> findAll() throws SQLException {
        String sql = "SELECT * FROM reservations ORDER BY check_in_date DESC";
        return queryList(sql);
    }

    public List<Reservation> findByStatus(ReservationStatus status) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE status = ? ORDER BY check_in_date";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            return toList(ps.executeQuery());
        }
    }

    public List<Reservation> findByGuestName(String name) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE guest_name LIKE ? ORDER BY check_in_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            return toList(ps.executeQuery());
        }
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------

    public boolean update(Reservation r) throws SQLException {
        String sql = """
                UPDATE reservations SET
                  guest_name = ?, guest_email = ?, guest_phone = ?,
                  room_number = ?, room_type = ?,
                  check_in_date = ?, check_out_date = ?,
                  number_of_guests = ?, total_amount = ?,
                  status = ?, special_requests = ?
                WHERE reservation_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,  r.getGuestName());
            ps.setString(2,  r.getGuestEmail());
            ps.setString(3,  r.getGuestPhone());
            ps.setInt(4,     r.getRoomNumber());
            ps.setString(5,  r.getRoomType().name());
            ps.setDate(6,    Date.valueOf(r.getCheckInDate()));
            ps.setDate(7,    Date.valueOf(r.getCheckOutDate()));
            ps.setInt(8,     r.getNumberOfGuests());
            ps.setDouble(9,  r.getTotalAmount());
            ps.setString(10, r.getStatus().name());
            ps.setString(11, r.getSpecialRequests());
            ps.setInt(12,    r.getReservationId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int reservationId, ReservationStatus status) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------

    public boolean delete(int reservationId) throws SQLException {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private List<Reservation> queryList(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return toList(ps.executeQuery());
        }
    }

    private List<Reservation> toList(ResultSet rs) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    /** Maps a ResultSet row to a Reservation object. */
    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setGuestName(rs.getString("guest_name"));
        r.setGuestEmail(rs.getString("guest_email"));
        r.setGuestPhone(rs.getString("guest_phone"));
        r.setRoomNumber(rs.getInt("room_number"));
        r.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        r.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        r.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        r.setNumberOfGuests(rs.getInt("number_of_guests"));
        r.setTotalAmount(rs.getDouble("total_amount"));
        r.setStatus(ReservationStatus.valueOf(rs.getString("status")));
        r.setSpecialRequests(rs.getString("special_requests"));
        r.setCreatedBy(rs.getString("created_by"));
        Date created = rs.getDate("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDate());
        return r;
    }
}
