package hotel.dao;

import hotel.model.Room;
import hotel.database.DatabaseConnection;
import hotel.exception.DatabaseException;

import java.sql.*;
import java.util.*;

public class RoomDAO {

    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) { list.add(mapRow(rs)); }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch rooms: " + e.getMessage());
        }
        return list;
    }

    public List<Room> getAvailableRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE is_available = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) { list.add(mapRow(rs)); }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch available rooms: " + e.getMessage());
        }
        return list;
    }

    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms(room_number,type,price,is_available) VALUES(?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room.getRoomNumber());
            stmt.setString(2, room.getType());
            stmt.setDouble(3, room.getPrice());
            stmt.setInt(4, room.isAvailable() ? 1 : 0);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to add room: " + e.getMessage());
        }
    }

    public boolean updateRoomStatus(int roomId, boolean isAvailable) {
        String sql = "UPDATE rooms SET is_available = ? WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, isAvailable ? 1 : 0);
            stmt.setInt(2, roomId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to update room status: " + e.getMessage());
        }
    }

    public boolean updateRoom(int roomId, String type, double price, boolean isAvailable) {
        String sql = "UPDATE rooms SET type=?, price=?, is_available=? WHERE room_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type);
            stmt.setDouble(2, price);
            stmt.setInt(3, isAvailable ? 1 : 0);
            stmt.setInt(4, roomId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to update room: " + e.getMessage());
        }
    }

    public boolean deleteRoom(String roomNumber) {
        String sql = "DELETE FROM rooms WHERE room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, roomNumber);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete room: " + e.getMessage());
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
            rs.getInt("room_id"),
            rs.getString("room_number"),
            rs.getString("type"),
            rs.getDouble("price"),
            rs.getInt("is_available") == 1
        );
    }
}
