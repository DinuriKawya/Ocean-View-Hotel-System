package oceanview.service;

import oceanview.dao.ReservationDAO;
import oceanview.model.Reservation;
import oceanview.model.ReservationStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ReservationService {

    private final ReservationDAO dao = new ReservationDAO();

    // -----------------------------------------------------------------------
    // COLLECTION 1 — Queue
    // -----------------------------------------------------------------------
    private final Queue<Reservation> reservationQueue = new LinkedList<>();

    // -----------------------------------------------------------------------
    // COLLECTION 2 — Set
    // -----------------------------------------------------------------------
    private final Set<String> reservationKeys = new HashSet<>();

    // -----------------------------------------------------------------------
    // COLLECTION 3 — Map
    // -----------------------------------------------------------------------
    private final Map<Integer, Reservation> reservationMap = new HashMap<>();


    public void enqueueReservation(Reservation r) throws ReservationException {
        String key = r.getRoomNumber() + "_" + r.getCheckInDate() + "_" + r.getCheckOutDate();

        if (reservationKeys.contains(key)) {
            throw new ReservationException(
                "Room " + r.getRoomNumber() + " is already in the queue for those dates.");
        }
      
        try {
            if (dao.isRoomBooked(r.getRoomNumber(), r.getCheckInDate(), r.getCheckOutDate(), -1)) {
                throw new ReservationException(
                    "Room " + r.getRoomNumber() + " is already booked for those dates.");
            }
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }

        reservationQueue.add(r);   
        reservationKeys.add(key);
    }

  
    public Reservation processNextReservation(String createdByUsername)
            throws ReservationException {

        Reservation r = reservationQueue.poll();
        if (r == null) {
            throw new ReservationException("No reservations in the queue to process.");
        }

        r.setCreatedBy(createdByUsername);
        r.setCreatedAt(LocalDate.now());
        r.setStatus(ReservationStatus.PENDING);

        try {
            int id = dao.insert(r);
            if (id < 0) throw new ReservationException("Failed to save reservation.");
            r.setReservationId(id);

            String key = r.getRoomNumber() + "_" + r.getCheckInDate() + "_" + r.getCheckOutDate();
            reservationKeys.remove(key);

            reservationMap.put(id, r); 
            return r;

        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    public int getQueueSize() {
        return reservationQueue.size();
    }

    public Reservation createReservation(Reservation r, String createdByUsername)
            throws ReservationException {

        validate(r);

        try {
            if (dao.isRoomBooked(r.getRoomNumber(), r.getCheckInDate(), r.getCheckOutDate(), -1)) {
                throw new ReservationException(
                    "Room " + r.getRoomNumber() + " is already booked for those dates.");
            }
        } catch (ReservationException re) {
            throw re;
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }

        r.setStatus(ReservationStatus.PENDING);
        r.setCreatedBy(createdByUsername);
        r.setCreatedAt(LocalDate.now());

        try {
            int id = dao.insert(r);
            if (id < 0) throw new ReservationException("Failed to save reservation.");
            r.setReservationId(id);

            reservationMap.put(id, r);
            return r;
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    public Reservation getById(int id) throws ReservationException {
        if (reservationMap.containsKey(id)) {
            return reservationMap.get(id);
        }
        try {
            Reservation r = dao.findById(id);
            if (r == null) throw new ReservationException("Reservation #" + id + " not found.");
            reservationMap.put(id, r); 
            return r;
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }


    public List<Reservation> getAllReservations() throws ReservationException {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    public List<Reservation> searchByGuestName(String name) throws ReservationException {
        if (name == null || name.isBlank())
            throw new ReservationException("Guest name cannot be empty.");
        try {
            return dao.findByGuestName(name.trim());
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    public List<Reservation> getByStatus(ReservationStatus status) throws ReservationException {
        try {
            return dao.findByStatus(status);
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    public Reservation updateReservation(Reservation r) throws ReservationException {
        validate(r);

        if (r.getStatus() == ReservationStatus.CANCELLED ||
            r.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new ReservationException(
                "Cannot edit a " + r.getStatus().getDisplayName() + " reservation.");
        }

        try {
            if (dao.isRoomBooked(r.getRoomNumber(), r.getCheckInDate(),
                                 r.getCheckOutDate(), r.getReservationId())) {
                throw new ReservationException(
                    "Room " + r.getRoomNumber() + " is already booked for those dates.");
            }
        } catch (ReservationException re) {
            throw re;
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }

        try {
            if (!dao.update(r))
                throw new ReservationException("Reservation #" + r.getReservationId() + " not found.");
            reservationMap.put(r.getReservationId(), r); // keep cache in sync
            return r;
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    public void changeStatus(int reservationId, ReservationStatus newStatus)
            throws ReservationException {
        try {
            if (!dao.updateStatus(reservationId, newStatus))
                throw new ReservationException("Reservation #" + reservationId + " not found.");
            if (reservationMap.containsKey(reservationId)) {
                reservationMap.get(reservationId).setStatus(newStatus);
            }
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Delete / Cancel
    // -----------------------------------------------------------------------

    public void cancelReservation(int reservationId) throws ReservationException {
        changeStatus(reservationId, ReservationStatus.CANCELLED);
    }

    public void deleteReservation(int reservationId) throws ReservationException {
        try {
            if (!dao.delete(reservationId))
                throw new ReservationException("Reservation #" + reservationId + " not found.");
            reservationMap.remove(reservationId);
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Validation
    // -----------------------------------------------------------------------

    private void validate(Reservation r) throws ReservationException {
        if (r.getGuestName() == null || r.getGuestName().isBlank())
            throw new ReservationException("Guest name is required.");

        if (r.getGuestEmail() == null || !r.getGuestEmail().contains("@"))
            throw new ReservationException("A valid guest email is required.");

        if (r.getRoomType() == null)
            throw new ReservationException("Room type is required.");

        if (r.getCheckInDate() == null)
            throw new ReservationException("Check-in date is required.");

        if (r.getCheckOutDate() == null)
            throw new ReservationException("Check-out date is required.");

        if (!r.getCheckOutDate().isAfter(r.getCheckInDate()))
            throw new ReservationException("Check-out date must be after check-in date.");

        if (r.getCheckInDate().isBefore(LocalDate.now()))
            throw new ReservationException("Check-in date cannot be in the past.");

        if (r.getNumberOfGuests() < 1)
            throw new ReservationException("At least 1 guest is required.");

        if (r.getRoomType() != null &&
            r.getNumberOfGuests() > r.getRoomType().getMaxGuests()) {
            throw new ReservationException(
                r.getRoomType().getDisplayName() + " allows max " +
                r.getRoomType().getMaxGuests() + " guests.");
        }

        if (r.getTotalAmount() < 0)
            throw new ReservationException("Total amount cannot be negative.");
    }

    // -----------------------------------------------------------------------
    // Checked exception
    // -----------------------------------------------------------------------

    public static class ReservationException extends Exception {
        public ReservationException(String message) { super(message); }
    }
}
