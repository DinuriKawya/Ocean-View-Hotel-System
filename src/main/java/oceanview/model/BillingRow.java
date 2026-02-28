package oceanview.model;

import java.time.LocalDate;

/**
 * View-model DTO for the billing list — computed from a joined query,
 * not a direct table mapping.
 */
public class BillingRow {

    private int       reservationId;
    private String    guestName;
    private String    guestEmail;
    private String    guestPhone;
    private int       roomNumber;
    private String    roomType;         // display name
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long      nights;
    private double    roomCharges;
    private double    extraCharges;
    private double    totalDue;
    private double    totalPaid;
    private double    balance;
    private String    reservationStatus;
    private String    billingStatus;    // PAID / PARTIAL / UNPAID / N/A

    public BillingRow() {}

    // Getters
    public int       getReservationId()      { return reservationId; }
    public String    getGuestName()          { return guestName; }
    public String    getGuestEmail()         { return guestEmail; }
    public String    getGuestPhone()         { return guestPhone; }
    public int       getRoomNumber()         { return roomNumber; }
    public String    getRoomType()           { return roomType; }
    public LocalDate getCheckInDate()        { return checkInDate; }
    public LocalDate getCheckOutDate()       { return checkOutDate; }
    public long      getNights()             { return nights; }
    public double    getRoomCharges()        { return roomCharges; }
    public double    getExtraCharges()       { return extraCharges; }
    public double    getTotalDue()           { return totalDue; }
    public double    getTotalPaid()          { return totalPaid; }
    public double    getBalance()            { return balance; }
    public String    getReservationStatus()  { return reservationStatus; }
    public String    getBillingStatus()      { return billingStatus; }

    // Setters
    public void setReservationId(int reservationId)           { this.reservationId      = reservationId; }
    public void setGuestName(String guestName)                { this.guestName          = guestName; }
    public void setGuestEmail(String guestEmail)              { this.guestEmail         = guestEmail; }
    public void setGuestPhone(String guestPhone)              { this.guestPhone         = guestPhone; }
    public void setRoomNumber(int roomNumber)                  { this.roomNumber         = roomNumber; }
    public void setRoomType(String roomType)                  { this.roomType           = roomType; }
    public void setCheckInDate(LocalDate checkInDate)         { this.checkInDate        = checkInDate; }
    public void setCheckOutDate(LocalDate checkOutDate)       { this.checkOutDate       = checkOutDate; }
    public void setNights(long nights)                        { this.nights             = nights; }
    public void setRoomCharges(double roomCharges)            { this.roomCharges        = roomCharges; }
    public void setExtraCharges(double extraCharges)          { this.extraCharges       = extraCharges; }
    public void setTotalDue(double totalDue)                  { this.totalDue           = totalDue; }
    public void setTotalPaid(double totalPaid)                { this.totalPaid          = totalPaid; }
    public void setBalance(double balance)                    { this.balance            = balance; }
    public void setReservationStatus(String reservationStatus){ this.reservationStatus  = reservationStatus; }
    public void setBillingStatus(String billingStatus)        { this.billingStatus      = billingStatus; }
}
