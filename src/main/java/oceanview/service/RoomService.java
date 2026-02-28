package oceanview.service;

import oceanview.dao.RoomDAO;
import oceanview.model.Room;
import oceanview.model.RoomStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Three-tier role: BUSINESS LOGIC LAYER for Room management.
 */
public class RoomService {

    private final RoomDAO dao = new RoomDAO();

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    public List<Room> getAllRooms() throws RoomException {
        try { return dao.findAll(); }
        catch (SQLException e) { throw new RoomException("Database error: " + e.getMessage()); }
    }

    public Room getById(int id) throws RoomException {
        try {
            Room r = dao.findById(id);
            if (r == null) throw new RoomException("Room #" + id + " not found.");
            return r;
        } catch (SQLException e) { throw new RoomException("Database error: " + e.getMessage()); }
    }

    public Room getByRoomNumber(int roomNumber) throws RoomException {
        try {
            Room r = dao.findByRoomNumber(roomNumber);
            if (r == null) throw new RoomException("Room " + roomNumber + " not found.");
            return r;
        } catch (SQLException e) { throw new RoomException("Database error: " + e.getMessage()); }
    }

    public List<Room> getAvailableRooms() throws RoomException {
        try { return dao.findAvailable(); }
        catch (SQLException e) { throw new RoomException("Database error: " + e.getMessage()); }
    }

    public List<Room> getAvailableRoomsForDates(LocalDate checkIn, LocalDate checkOut)
            throws RoomException {
        try { return dao.findAvailableForDates(checkIn, checkOut); }
        catch (SQLException e) { throw new RoomException("Database error: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    public Room createRoom(Room r) throws RoomException {
        validate(r);
        try {
            int id = dao.insert(r);
            if (id < 0) throw new RoomException("Failed to save room.");
            r.setRoomId(id);
            return r;
        } catch (SQLException e) { throw new RoomException("Database error: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    public Room updateRoom(Room r) throws RoomException {
        validate(r);
        try {
            if (!dao.update(r))
                throw new RoomException("Room #" + r.getRoomId() + " not found.");
            return r;
        } catch (SQLException e) { throw new RoomException("Database error: " + e.getMessage()); }
    }

    public void changeStatus(int roomId, RoomStatus status) throws RoomException {
        try {
            if (!dao.updateStatus(roomId, status))
                throw new RoomException("Room #" + roomId + " not found.");
        } catch (SQLException e) { throw new RoomException("Database error: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    public void deleteRoom(int roomId) throws RoomException {
        try {
            Room r = dao.findById(roomId);
            if (r == null) throw new RoomException("Room #" + roomId + " not found.");
            if (r.getStatus() == RoomStatus.OCCUPIED)
                throw new RoomException("Cannot delete an occupied room.");
            if (!dao.delete(roomId))
                throw new RoomException("Failed to delete room #" + roomId);
        } catch (SQLException e) { throw new RoomException("Database error: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // Validation
    // -----------------------------------------------------------------------

    private void validate(Room r) throws RoomException {
        if (r.getRoomNumber() <= 0)
            throw new RoomException("Room number must be a positive integer.");
        if (r.getRoomType() == null)
            throw new RoomException("Room type is required.");
        if (r.getPricePerNight() <= 0)
            throw new RoomException("Price per night must be greater than zero.");
        if (r.getFloor() <= 0)
            throw new RoomException("Floor must be a positive integer.");
        if (r.getStatus() == null)
            throw new RoomException("Room status is required.");
    }

    // -----------------------------------------------------------------------
    // Checked exception
    // -----------------------------------------------------------------------

    public static class RoomException extends Exception {
        public RoomException(String message) { super(message); }
    }
}
