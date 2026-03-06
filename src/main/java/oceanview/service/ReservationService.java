package oceanview.service;

import oceanview.dao.ReservationDAO;
import oceanview.model.Reservation;
import oceanview.model.ReservationStatus;
import oceanview.FilterPattern.Criteria;
import oceanview.FilterPattern.AndCriteria;
import oceanview.FilterPattern.NotCriteria;
import oceanview.FilterPattern.CriteriaByGuestName;
import oceanview.FilterPattern.CriteriaByStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReservationService {

    private final ReservationDAO dao;

    public ReservationService() {
    	this.dao = new ReservationDAO();
    }
 
    public ReservationService(ReservationDAO dao) { 
    	this.dao = dao;
    }

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    public Reservation createReservation(Reservation r, String createdByUsername)
            throws ReservationException {

        validate(r);

        r.setStatus(ReservationStatus.PENDING);
        r.setCreatedBy(createdByUsername);
        r.setCreatedAt(LocalDate.now());

        try {
            int id = dao.insert(r);
            if (id < 0) throw new ReservationException("Failed to save reservation.");
            r.setReservationId(id);
            return r;
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    
    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    public Reservation getById(int id) throws ReservationException {
        try {
            Reservation r = dao.findById(id);
            if (r == null) throw new ReservationException("Reservation #" + id + " not found.");
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
            List<Reservation> all = dao.findAll();
            Criteria criteria = new CriteriaByGuestName(name.trim());
            return criteria.meetCriteria(all);
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    public List<Reservation> getByStatus(ReservationStatus status) throws ReservationException {
        try {
            List<Reservation> all = dao.findAll();
            Criteria criteria = new CriteriaByStatus(status);
            return criteria.meetCriteria(all);
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    public List<Reservation> searchActiveByGuestName(String name) throws ReservationException {
        if (name == null || name.isBlank())
            throw new ReservationException("Guest name cannot be empty.");
        try {
            List<Reservation> all = dao.findAll();
            Criteria nameCriteria      = new CriteriaByGuestName(name.trim());
            Criteria cancelledCriteria = new CriteriaByStatus(ReservationStatus.CANCELLED);
            Criteria activeAndNamed    = new AndCriteria(nameCriteria, new NotCriteria(cancelledCriteria));
            return activeAndNamed.meetCriteria(all);
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
            if (!dao.update(r))
                throw new ReservationException("Reservation #" + r.getReservationId() + " not found.");
            return r;
        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

//    public void changeStatus(int reservationId, ReservationStatus newStatus)
//            throws ReservationException {
//        try {
//            if (!dao.updateStatus(reservationId, newStatus))
//                throw new ReservationException("Reservation #" + reservationId + " not found.");
//        } catch (SQLException e) {
//            throw new ReservationException("Database error: " + e.getMessage());
//        }
//    }
//
//    // -----------------------------------------------------------------------
//    // Delete / Cancel
//    // -----------------------------------------------------------------------
//
//    public void cancelReservation(int reservationId) throws ReservationException {
//        changeStatus(reservationId, ReservationStatus.CANCELLED);
//    }
    
 // ── UPDATED: sends CONFIRMED email when admin changes status ──
    public void changeStatus(int reservationId, ReservationStatus newStatus)
            throws ReservationException {
        try {
            if (!dao.updateStatus(reservationId, newStatus))
                throw new ReservationException("Reservation #" + reservationId + " not found.");

            // Send confirmation email when status becomes CONFIRMED
            if (newStatus == ReservationStatus.CONFIRMED) {
                Reservation r = getById(reservationId);
                EmailService.getInstance().sendConfirmationEmail(r);
            }

        } catch (SQLException e) {
            throw new ReservationException("Database error: " + e.getMessage());
        }
    }

    // ── UPDATED: loads reservation first, cancels, then sends email ──
    public void cancelReservation(int reservationId) throws ReservationException {
        // Step 1: load BEFORE cancelling so we have the guest email
        Reservation r = getById(reservationId);

        // Step 2: set status to CANCELLED in DB
        changeStatus(reservationId, ReservationStatus.CANCELLED);

        // Step 3: send email — never throws, failure is just logged
        EmailService.getInstance().sendCancellationEmail(r);
    }

    public void deleteReservation(int reservationId) throws ReservationException {
        try {
            if (!dao.delete(reservationId))
                throw new ReservationException("Reservation #" + reservationId + " not found.");
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
