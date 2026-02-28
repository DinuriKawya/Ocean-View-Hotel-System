package oceanview.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Represents a hotel reservation.
 *
 * Three-tier role: MODEL (data structure / entity layer).
 * This class holds no business logic and no database code.
 */
public class Reservation {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private int               reservationId;
    private String            guestName;
    private String            guestEmail;
    private String            guestPhone;
    private int               roomNumber;
    private RoomType          roomType;
    private LocalDate         checkInDate;
    private LocalDate         checkOutDate;
    private int               numberOfGuests;
    private double            totalAmount;
    private ReservationStatus status;
    private String            specialRequests;
    private String            createdBy;       // username of staff who booked
    private LocalDate         createdAt;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    public Reservation() {}

    public Reservation(int reservationId, String guestName, String guestEmail,
                       String guestPhone, int roomNumber, RoomType roomType,
                       LocalDate checkInDate, LocalDate checkOutDate,
                       int numberOfGuests, double totalAmount,
                       ReservationStatus status, String specialRequests,
                       String createdBy, LocalDate createdAt) {

        this.reservationId  = reservationId;
        this.guestName      = guestName;
        this.guestEmail     = guestEmail;
        this.guestPhone     = guestPhone;
        this.roomNumber     = roomNumber;
        this.roomType       = roomType;
        this.checkInDate    = checkInDate;
        this.checkOutDate   = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.totalAmount    = totalAmount;
        this.status         = status;
        this.specialRequests= specialRequests;
        this.createdBy      = createdBy;
        this.createdAt      = createdAt;
    }

    // -----------------------------------------------------------------------
    // Derived / computed helpers (no setters — calculated from other fields)
    // -----------------------------------------------------------------------

    /** Number of nights between check-in and check-out. */
    public long getNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public int               getReservationId()  { return reservationId; }
    public String            getGuestName()      { return guestName; }
    public String            getGuestEmail()     { return guestEmail; }
    public String            getGuestPhone()     { return guestPhone; }
    public int               getRoomNumber()     { return roomNumber; }
    public RoomType          getRoomType()       { return roomType; }
    public LocalDate         getCheckInDate()    { return checkInDate; }
    public LocalDate         getCheckOutDate()   { return checkOutDate; }
    public int               getNumberOfGuests() { return numberOfGuests; }
    public double            getTotalAmount()    { return totalAmount; }
    public ReservationStatus getStatus()         { return status; }
    public String            getSpecialRequests(){ return specialRequests; }
    public String            getCreatedBy()      { return createdBy; }
    public LocalDate         getCreatedAt()      { return createdAt; }

    // -----------------------------------------------------------------------
    // Setters
    // -----------------------------------------------------------------------

    public void setReservationId(int reservationId)        { this.reservationId  = reservationId; }
    public void setGuestName(String guestName)             { this.guestName      = guestName; }
    public void setGuestEmail(String guestEmail)           { this.guestEmail     = guestEmail; }
    public void setGuestPhone(String guestPhone)           { this.guestPhone     = guestPhone; }
    public void setRoomNumber(int roomNumber)              { this.roomNumber     = roomNumber; }
    public void setRoomType(RoomType roomType)             { this.roomType       = roomType; }
    public void setCheckInDate(LocalDate checkInDate)      { this.checkInDate    = checkInDate; }
    public void setCheckOutDate(LocalDate checkOutDate)    { this.checkOutDate   = checkOutDate; }
    public void setNumberOfGuests(int numberOfGuests)      { this.numberOfGuests = numberOfGuests; }
    public void setTotalAmount(double totalAmount)         { this.totalAmount    = totalAmount; }
    public void setStatus(ReservationStatus status)        { this.status         = status; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests= specialRequests; }
    public void setCreatedBy(String createdBy)             { this.createdBy      = createdBy; }
    public void setCreatedAt(LocalDate createdAt)          { this.createdAt      = createdAt; }

    // -----------------------------------------------------------------------
    // Formatted output
    // -----------------------------------------------------------------------

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Human-readable summary — use for receipts, logs, console output.
     *
     * Example:
     * ╔══════════════════════════════════════════════════╗
     * ║         OCEANVIEW HOTEL — RESERVATION            ║
     * ╠══════════════════════════════════════════════════╣
     * ║ Reservation #  : 1001                           ║
     * ║ Status         : Confirmed                      ║
     * ║ Guest          : Juan Dela Cruz                 ║
     * ...
     */
    public String toFormattedString() {
        String line = "═".repeat(50);
        return "\n╔" + line + "╗\n" +
               "║" + center("OCEANVIEW HOTEL — RESERVATION", 50) + "║\n" +
               "╠" + line + "╣\n" +
               row("Reservation #",  String.valueOf(reservationId)) +
               row("Status",         status != null ? status.getDisplayName() : "—") +
               "╠" + line + "╣\n" +
               row("Guest",          guestName) +
               row("Email",          guestEmail) +
               row("Phone",          guestPhone) +
               "╠" + line + "╣\n" +
               row("Room Number",    String.valueOf(roomNumber)) +
               row("Room Type",      roomType != null ? roomType.getDisplayName() : "—") +
               row("Guests",         String.valueOf(numberOfGuests)) +
               row("Check-In",       checkInDate  != null ? checkInDate.format(DATE_FMT)  : "—") +
               row("Check-Out",      checkOutDate != null ? checkOutDate.format(DATE_FMT) : "—") +
               row("Nights",         String.valueOf(getNights())) +
               "╠" + line + "╣\n" +
               row("Total Amount",   String.format(AppSettings.getCurrency() + " %.2f", totalAmount)) +
               row("Booked By",      createdBy) +
               row("Date Booked",    createdAt != null ? createdAt.format(DATE_FMT) : "—") +
               (specialRequests != null && !specialRequests.isBlank()
                   ? row("Special Req.", specialRequests) : "") +
               "╚" + line + "╝\n";
    }

    /** Standard toString — compact, good for logging. */
    @Override
    public String toString() {
        return "Reservation{" +
               "id="          + reservationId +
               ", guest='"    + guestName     + '\'' +
               ", room="      + roomNumber    +
               ", type="      + roomType      +
               ", checkIn="   + checkInDate   +
               ", checkOut="  + checkOutDate  +
               ", nights="    + getNights()   +
               ", status="    + status        +
               ", total=" + AppSettings.getCurrency() + " " + String.format("%.2f", totalAmount) +
               '}';
    }

    // -----------------------------------------------------------------------
    // Private formatting helpers
    // -----------------------------------------------------------------------

    private String row(String label, String value) {
        String content = String.format(" %-16s: %-31s", label, value == null ? "—" : value);
        return "║" + content + "║\n";
    }

    private String center(String text, int width) {
        int pad = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, pad)) + text +
               " ".repeat(Math.max(0, width - text.length() - pad));
    }
}
