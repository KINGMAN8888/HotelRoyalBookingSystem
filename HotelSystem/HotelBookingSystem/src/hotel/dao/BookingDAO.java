package hotel.dao;

import hotel.model.Booking;
import hotel.database.DatabaseConnection;
import hotel.exception.DatabaseException;

import java.sql.*;
import java.util.*;

public class BookingDAO {

    public boolean createBooking(Booking booking) {
        String sql = "INSERT INTO bookings(customer_id,room_id,check_in_date," +
                     "check_out_date,amount,status) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, booking.getCustomerId());
            stmt.setInt(2, booking.getRoomId());
            stmt.setString(3, booking.getCheckInDate());
            stmt.setString(4, booking.getCheckOutDate());
            stmt.setDouble(5, booking.getAmount());
            stmt.setString(6, booking.getStatus());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                new RoomDAO().updateRoomStatus(booking.getRoomId(), false);
                return true;
            }
            return false;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to create booking: " + e.getMessage());
        }
    }

    public List<Booking> getBookingsByCustomer(int customerId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) { list.add(mapRow(rs)); }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch bookings: " + e.getMessage());
        }
        return list;
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) { list.add(mapRow(rs)); }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all bookings: " + e.getMessage());
        }
        return list;
    }

    public double calculateRevenue() {
        String sql =
            "SELECT b.check_in_date, b.check_out_date, r.price " +
            "FROM bookings b JOIN rooms r ON b.room_id = r.room_id " +
            "WHERE b.status IN ('Confirmed','CheckedIn','CheckedOut')";
        double total = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                long n = parseNights(rs.getString("check_in_date"),
                                     rs.getString("check_out_date"));
                if (n > 0) total += n * rs.getDouble("price");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate revenue: " + e.getMessage());
        }
        return total;
    }

    private long parseNights(String ci, String co) {
        java.time.LocalDate d1 = tryParse(ci);
        java.time.LocalDate d2 = tryParse(co);
        if (d1 == null || d2 == null) return 0;
        long n = java.time.temporal.ChronoUnit.DAYS.between(d1, d2);
        return n > 0 ? n : 0;
    }

    private java.time.LocalDate tryParse(String d) {
        if (d == null || d.isBlank()) return null;
        d = d.trim();
        for (String pat : new String[]{"yyyy-MM-dd","dd/MM/yyyy","MM/dd/yyyy","dd-MM-yyyy"}) {
            try { return java.time.LocalDate.parse(d,
                java.time.format.DateTimeFormatter.ofPattern(pat)); }
            catch (Exception ignored) {}
        }
        return null;
    }

    public boolean checkInBooking(int bookingId) {
        String sql = "UPDATE bookings SET status = 'CheckedIn' WHERE booking_id = ? AND status = 'Confirmed'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check in: " + e.getMessage());
        }
    }

    public boolean checkOutBooking(int bookingId, int roomId) {
        String sql = "UPDATE bookings SET status = 'CheckedOut' WHERE booking_id = ? AND status = 'CheckedIn'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            if (stmt.executeUpdate() > 0) {
                new RoomDAO().updateRoomStatus(roomId, true);
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check out: " + e.getMessage());
        }
    }

    public List<Object[]> getAllBookingsDetailed() {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT b.booking_id, u.name, r.room_number, r.type, " +
            "b.check_in_date, b.check_out_date, b.amount, b.status, b.room_id, r.price " +
            "FROM bookings b " +
            "JOIN users u ON b.customer_id = u.user_id " +
            "JOIN rooms r ON b.room_id = r.room_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("booking_id"),
                    rs.getString("name"),
                    rs.getString("room_number"),
                    rs.getString("type"),
                    rs.getString("check_in_date"),
                    rs.getString("check_out_date"),
                    String.format("%.2f", rs.getDouble("amount")),
                    rs.getString("status"),
                    rs.getInt("room_id"),
                    rs.getDouble("price")   // index 9: room price/night
                });
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch detailed bookings: " + e.getMessage());
        }
        return list;
    }

    public boolean cancelBooking(int bookingId, int roomId) {
        String sql = "UPDATE bookings SET status = 'Cancelled' WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            if (stmt.executeUpdate() > 0) {
                new RoomDAO().updateRoomStatus(roomId, true);
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to cancel booking: " + e.getMessage());
        }
    }

    public boolean deleteBooking(int bookingId, int roomId) {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            if (stmt.executeUpdate() > 0) {
                new RoomDAO().updateRoomStatus(roomId, true);
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete booking: " + e.getMessage());
        }
    }

    public boolean updateBooking(int bookingId, String checkIn, String checkOut, String status) {
        String sql = "UPDATE bookings SET check_in_date=?, check_out_date=?, status=? WHERE booking_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, checkIn);
            stmt.setString(2, checkOut);
            stmt.setString(3, status);
            stmt.setInt(4, bookingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update booking: " + e.getMessage());
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        return new Booking(
            rs.getInt("booking_id"),
            rs.getInt("customer_id"),
            rs.getInt("room_id"),
            rs.getString("check_in_date"),
            rs.getString("check_out_date"),
            rs.getDouble("amount"),
            rs.getString("status")
        );
    }
}
