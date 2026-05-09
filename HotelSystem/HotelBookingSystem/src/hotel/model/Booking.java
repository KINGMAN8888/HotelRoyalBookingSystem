package hotel.model;

public class Booking {

    private int bookingId;
    private int customerId;
    private int roomId;
    private String checkInDate;
    private String checkOutDate;
    private double amount;
    private String status;

    public Booking() {}

    public Booking(int bookingId, int customerId, int roomId,
            String checkInDate, String checkOutDate,
            double amount, String status) {
        this.bookingId    = bookingId;
        this.customerId   = customerId;
        this.roomId       = roomId;
        this.checkInDate  = checkInDate;
        this.checkOutDate = checkOutDate;
        this.amount       = amount;
        this.status       = status;
    }

    public int getBookingId()          { return bookingId; }
    public void setBookingId(int v)    { this.bookingId = v; }

    public int getCustomerId()         { return customerId; }
    public void setCustomerId(int v)   { this.customerId = v; }

    public int getRoomId()             { return roomId; }
    public void setRoomId(int v)       { this.roomId = v; }

    public String getCheckInDate()          { return checkInDate; }
    public void setCheckInDate(String v)    { this.checkInDate = v; }

    public String getCheckOutDate()         { return checkOutDate; }
    public void setCheckOutDate(String v)   { this.checkOutDate = v; }

    public double getAmount()          { return amount; }
    public void setAmount(double v)    { this.amount = v; }

    public String getStatus()          { return status; }
    public void setStatus(String v)    { this.status = v; }
}
