package oceanview.model;

import java.time.LocalDateTime;

public class Payment {

    private int           paymentId;
    private int           reservationId;
    private double        amount;
    private PaymentMethod method;

    // Card / Transfer fields
    private Integer       bankId;       // null for CASH
    private String        bankName;     // denormalised — kept for history
    private String        cardLast4;    // 4-digit string, CARD only
    private String        referenceNo;  // TRANSFER only

    private String        comment;
    private String        createdBy;
    private LocalDateTime createdAt;

    public Payment() {}

    // Getters
    public int           getPaymentId()    { return paymentId; }
    public int           getReservationId(){ return reservationId; }
    public double        getAmount()       { return amount; }
    public PaymentMethod getMethod()       { return method; }
    public Integer       getBankId()       { return bankId; }
    public String        getBankName()     { return bankName; }
    public String        getCardLast4()    { return cardLast4; }
    public String        getReferenceNo()  { return referenceNo; }
    public String        getComment()      { return comment; }
    public String        getCreatedBy()    { return createdBy; }
    public LocalDateTime getCreatedAt()    { return createdAt; }

    // Setters
    public void setPaymentId(int paymentId)          { this.paymentId    = paymentId; }
    public void setReservationId(int reservationId)  { this.reservationId= reservationId; }
    public void setAmount(double amount)             { this.amount       = amount; }
    public void setMethod(PaymentMethod method)      { this.method       = method; }
    public void setBankId(Integer bankId)            { this.bankId       = bankId; }
    public void setBankName(String bankName)         { this.bankName     = bankName; }
    public void setCardLast4(String cardLast4)       { this.cardLast4    = cardLast4; }
    public void setReferenceNo(String referenceNo)   { this.referenceNo  = referenceNo; }
    public void setComment(String comment)           { this.comment      = comment; }
    public void setCreatedBy(String createdBy)       { this.createdBy    = createdBy; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt    = createdAt; }
}
