package hotel.model;

public class Customer extends User {

    private String phoneNumber;

    public Customer() {}

    public Customer(int id, String name, String email, String password, String phoneNumber) {
        super(id, name, email, password, "customer");
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber()         { return phoneNumber; }
    public void setPhoneNumber(String v)   { this.phoneNumber = v; }
}
