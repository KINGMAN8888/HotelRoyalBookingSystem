package hotel.model;

public abstract class User {

    private int userId;
    private String name;
    private String email;
    private String password;
    private String role;

    public User() {}

    public User(int userId, String name, String email, String password, String role) {
        this.userId   = userId;
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.role     = role;
    }

    public int getUserId()           { return userId; }
    public void setUserId(int v)     { this.userId = v; }

    public String getName()          { return name; }
    public void setName(String v)    { this.name = v; }

    public String getEmail()         { return email; }
    public void setEmail(String v)   { this.email = v; }

    public String getPassword()      { return password; }
    public void setPassword(String v){ this.password = v; }

    public String getRole()          { return role; }
    public void setRole(String v)    { this.role = v; }
}
