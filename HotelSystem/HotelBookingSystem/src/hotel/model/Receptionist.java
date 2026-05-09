package hotel.model;

public class Receptionist extends User {

    public Receptionist(int userId, String name, String email, String password) {
        super(userId, name, email, password, "receptionist");
    }

    public void checkIn(int bookingId) {}
    public void checkOut(int bookingId) {}
}
