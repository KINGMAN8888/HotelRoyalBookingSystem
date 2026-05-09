package hotel.model;

public class Room implements IReservable {

    private int roomId;
    private String roomNumber;
    private String type;
    private double price;
    private boolean isAvailable;

    public Room() {}

    public Room(int roomId, String roomNumber, String type, double price, boolean isAvailable) {
        this.roomId      = roomId;
        this.roomNumber  = roomNumber;
        this.type        = type;
        this.price       = price;
        this.isAvailable = isAvailable;
    }

    public int getRoomId()              { return roomId; }
    public void setRoomId(int v)        { this.roomId = v; }

    public String getRoomNumber()       { return roomNumber; }
    public void setRoomNumber(String v) { this.roomNumber = v; }

    public String getType()             { return type; }
    public void setType(String v)       { this.type = v; }

    public double getPrice()            { return price; }
    public void setPrice(double v)      { this.price = v; }

    public boolean isAvailable()        { return isAvailable; }
    public void setAvailable(boolean v) { this.isAvailable = v; }

    @Override
    public boolean reserve() {
        if (isAvailable) { isAvailable = false; return true; }
        return false;
    }

    @Override
    public boolean cancelReservation() {
        isAvailable = true;
        return true;
    }
}
