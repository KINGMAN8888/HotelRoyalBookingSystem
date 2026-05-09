package hotel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String BASE_URL = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&cachePrepStmts=false";
    private static final String DB_URL   = "jdbc:mysql://localhost:3306/Hotel?useSSL=false&allowPublicKeyRetrieval=true&cachePrepStmts=false&useServerPrepStmts=false";
    private static final String USER = "root";
    private static final String PASS = "1234";

    // NOTE: We do NOT cache a single static connection — each call gets a fresh
    // connection so room-availability changes are always visible immediately.
    private static boolean dbInitialized = false;

    private DatabaseConnection() {}

    public static Connection getConnection() {
        try {
            if (!dbInitialized) {
                // Create the Hotel database if it doesn't exist yet
                try (Connection base = DriverManager.getConnection(BASE_URL, USER, PASS);
                     Statement s = base.createStatement()) {
                    s.execute("CREATE DATABASE IF NOT EXISTS Hotel");
                }
                // Use a temporary connection to run one-time table/seed setup
                try (Connection init = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    initializeDatabase(init);
                }
                dbInitialized = true;
            }
            // Always return a fresh connection so callers see the latest data
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void initializeDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) UNIQUE NOT NULL, " +
                "password VARCHAR(100) NOT NULL, " +
                "role VARCHAR(20) NOT NULL, " +
                "phone_number VARCHAR(20))"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS rooms (" +
                "room_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "room_number VARCHAR(20) NOT NULL, " +
                "type VARCHAR(20) NOT NULL, " +
                "price DECIMAL(10,2) NOT NULL, " +
                "is_available TINYINT(1) NOT NULL DEFAULT 1)"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS bookings (" +
                "booking_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "customer_id INT NOT NULL, " +
                "room_id INT NOT NULL, " +
                "check_in_date VARCHAR(20) NOT NULL, " +
                "check_out_date VARCHAR(20) NOT NULL, " +
                "amount DECIMAL(10,2) NOT NULL, " +
                "status VARCHAR(20) NOT NULL, " +
                "FOREIGN KEY (customer_id) REFERENCES users(user_id), " +
                "FOREIGN KEY (room_id) REFERENCES rooms(room_id))"
            );

            stmt.execute(
                "INSERT IGNORE INTO users (name, email, password, role) " +
                "VALUES ('Admin', 'admin@hotel.com', 'admin123', 'admin')"
            );

            stmt.execute(
                "INSERT IGNORE INTO users (name, email, password, role) " +
                "VALUES ('Receptionist', 'reception@hotel.com', 'recep123', 'receptionist')"
            );

            // Pre-populate hotel rooms only on first run (empty table)
            java.sql.ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM rooms");
            if (countRs.next() && countRs.getInt(1) == 0) {
                String[][] rooms = {
                    // Floor 1 — Single rooms ($80/night)
                    {"101","Single","80.00"}, {"102","Single","80.00"}, {"103","Single","80.00"},
                    {"104","Single","80.00"}, {"105","Single","80.00"},
                    // Floor 2 — Double rooms ($120/night)
                    {"201","Double","120.00"}, {"202","Double","120.00"}, {"203","Double","120.00"},
                    {"204","Double","120.00"}, {"205","Double","120.00"},
                    // Floor 3 — Triple rooms ($150/night)
                    {"301","Triple","150.00"}, {"302","Triple","150.00"}, {"303","Triple","150.00"},
                    {"304","Triple","150.00"}, {"305","Triple","150.00"},
                    // Floor 4 — Suite rooms ($250/night)
                    {"401","Suite","250.00"}, {"402","Suite","250.00"}, {"403","Suite","250.00"},
                    {"404","Suite","250.00"}, {"405","Suite","250.00"}
                };
                for (String[] r : rooms) {
                    stmt.execute("INSERT INTO rooms (room_number,type,price,is_available) VALUES ('"
                        + r[0] + "','" + r[1] + "'," + r[2] + ",1)");
                }
            }
            countRs.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
