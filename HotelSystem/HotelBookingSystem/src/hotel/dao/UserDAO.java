package hotel.dao;

import hotel.model.*;
import hotel.database.DatabaseConnection;
import hotel.exception.DatabaseException;

import java.sql.*;

public class UserDAO {

    public User loginUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                if (role.equals("admin")) {
                    return new Admin(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")
                    );
                } else if (role.equals("receptionist")) {
                    return new hotel.model.Receptionist(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")
                    );
                } else {
                    return new Customer(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("phone_number")
                    );
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Login failed: " + e.getMessage());
        }
        return null;
    }

    public boolean registerCustomer(Customer customer) {
        String sql = "INSERT INTO users(name,email,password,role,phone_number) VALUES(?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPassword());
            stmt.setString(4, "customer");
            stmt.setString(5, customer.getPhoneNumber());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Registration failed: " + e.getMessage());
        }
    }
}
