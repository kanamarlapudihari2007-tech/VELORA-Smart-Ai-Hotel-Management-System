package com.hotel.dao;

import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomDAO - Data Access Object for Room and RoomType database operations.
 */
public class RoomDAO {

    /**
     * Retrieves all rooms joined with their RoomType details.
     */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.id, r.room_number, r.room_type_id, r.floor_number, r.status, " +
                     "rt.type_name, rt.price_per_night, rt.capacity " +
                     "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id " +
                     "ORDER BY r.floor_number ASC, r.room_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setRoomTypeId(rs.getInt("room_type_id"));
                room.setFloorNumber(rs.getInt("floor_number"));
                room.setStatus(rs.getString("status"));
                room.setTypeName(rs.getString("type_name"));
                room.setPricePerNight(rs.getBigDecimal("price_per_night"));
                room.setCapacity(rs.getInt("capacity"));
                rooms.add(room);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching rooms: " + e.getMessage());
        }
        return rooms;
    }

    /**
     * Retrieves all room types available in hotel.
     */
    public List<RoomType> getAllRoomTypes() {
        List<RoomType> types = new ArrayList<>();
        String sql = "SELECT id, type_name, price_per_night, description, capacity FROM room_types ORDER BY price_per_night ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                RoomType type = new RoomType(
                    rs.getInt("id"),
                    rs.getString("type_name"),
                    rs.getBigDecimal("price_per_night"),
                    rs.getString("description"),
                    rs.getInt("capacity")
                );
                types.add(type);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching room types: " + e.getMessage());
        }
        return types;
    }

    /**
     * Adds a new physical room to database.
     */
    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type_id, floor_number, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, room.getRoomNumber());
            stmt.setInt(2, room.getRoomTypeId());
            stmt.setInt(3, room.getFloorNumber());
            stmt.setString(4, room.getStatus() != null ? room.getStatus() : "AVAILABLE");

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding room: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates operational status of a room (e.g. AVAILABLE, RESERVED, OCCUPIED, CLEANING, MAINTENANCE).
     */
    public boolean updateRoomStatus(int roomId, String status) {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, roomId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating room status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if a room number already exists.
     */
    public boolean existsByRoomNumber(String roomNumber) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking room number existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Searches available rooms for specified dates and optional room type filter.
     * Excludes rooms with overlapping active bookings to prevent double-booking.
     */
    public List<Room> searchAvailableRooms(String checkInDate, String checkOutDate, Integer roomTypeId) {
        List<Room> rooms = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT r.id, r.room_number, r.room_type_id, r.floor_number, r.status, " +
            "rt.type_name, rt.price_per_night, rt.capacity " +
            "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id " +
            "WHERE r.status != 'MAINTENANCE' "
        );

        boolean hasDateFilter = (checkInDate != null && !checkInDate.trim().isEmpty() &&
                                 checkOutDate != null && !checkOutDate.trim().isEmpty());

        if (hasDateFilter) {
            sql.append("AND r.id NOT IN ( ")
               .append("    SELECT room_id FROM bookings ")
               .append("    WHERE status IN ('RESERVED', 'CHECKED_IN') ")
               .append("    AND (check_in_date < ? AND check_out_date > ?) ")
               .append(") ");
        } else {
            sql.append("AND r.status = 'AVAILABLE' ");
        }

        if (roomTypeId != null && roomTypeId > 0) {
            sql.append("AND r.room_type_id = ? ");
        }

        sql.append("ORDER BY rt.price_per_night ASC, r.room_number ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (hasDateFilter) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(checkOutDate));
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(checkInDate));
            }
            if (roomTypeId != null && roomTypeId > 0) {
                stmt.setInt(paramIndex++, roomTypeId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room();
                    room.setId(rs.getInt("id"));
                    room.setRoomNumber(rs.getString("room_number"));
                    room.setRoomTypeId(rs.getInt("room_type_id"));
                    room.setFloorNumber(rs.getInt("floor_number"));
                    room.setStatus(rs.getString("status"));
                    room.setTypeName(rs.getString("type_name"));
                    room.setPricePerNight(rs.getBigDecimal("price_per_night"));
                    room.setCapacity(rs.getInt("capacity"));
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching available rooms: " + e.getMessage());
        }
        return rooms;
    }

    /**
     * Retrieves a room by its primary key ID.
     */
    public Room getRoomById(int roomId) {
        String sql = "SELECT r.id, r.room_number, r.room_type_id, r.floor_number, r.status, " +
                     "rt.type_name, rt.price_per_night, rt.capacity " +
                     "FROM rooms r JOIN room_types rt ON r.room_type_id = rt.id " +
                     "WHERE r.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Room room = new Room();
                    room.setId(rs.getInt("id"));
                    room.setRoomNumber(rs.getString("room_number"));
                    room.setRoomTypeId(rs.getInt("room_type_id"));
                    room.setFloorNumber(rs.getInt("floor_number"));
                    room.setStatus(rs.getString("status"));
                    room.setTypeName(rs.getString("type_name"));
                    room.setPricePerNight(rs.getBigDecimal("price_per_night"));
                    room.setCapacity(rs.getInt("capacity"));
                    return room;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching room by id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates the base price per night for a room category (Stage 19 Dynamic Pricing).
     */
    public boolean updateRoomTypePrice(int typeId, BigDecimal newPrice) {
        String sql = "UPDATE room_types SET price_per_night = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newPrice);
            stmt.setInt(2, typeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating room type price: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resets standard default baseline prices for all room categories.
     */
    public boolean resetDefaultRoomPrices() {
        String sql = "UPDATE room_types SET price_per_night = CASE " +
                     "WHEN type_name LIKE '%Single%' THEN 1200.00 " +
                     "WHEN type_name LIKE '%Double%' THEN 2500.00 " +
                     "WHEN type_name LIKE '%Suite%' THEN 5000.00 " +
                     "ELSE price_per_night END";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql) >= 0;
        } catch (SQLException e) {
            System.err.println("Error resetting default room prices: " + e.getMessage());
            return false;
        }
    }
}
