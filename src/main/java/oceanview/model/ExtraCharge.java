package oceanview.model;

import java.time.LocalDateTime;

/** Represents an extra charge added at checkout (damage, room service, etc.). */
public class ExtraCharge {

    private int           chargeId;
    private int           reservationId;
    private String        chargeType;    // e.g. "Damage/Breakage", "Room Service"
    private String        description;  // optional note
    private double        amount;
    private String        addedBy;
    private LocalDateTime createdAt;

    public int           getChargeId()            { return chargeId; }
    public void          setChargeId(int v)        { this.chargeId = v; }

    public int           getReservationId()        { return reservationId; }
    public void          setReservationId(int v)   { this.reservationId = v; }

    public String        getChargeType()           { return chargeType; }
    public void          setChargeType(String v)   { this.chargeType = v; }

    public String        getDescription()          { return description; }
    public void          setDescription(String v)  { this.description = v; }

    public double        getAmount()               { return amount; }
    public void          setAmount(double v)       { this.amount = v; }

    public String        getAddedBy()              { return addedBy; }
    public void          setAddedBy(String v)      { this.addedBy = v; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    /** Display label: "Type — description" or just "Type" when no description. */
    public String getLabel() {
        if (description != null && !description.isBlank())
            return chargeType + " \u2014 " + description;
        return chargeType != null ? chargeType : "";
    }
}
