package com.hotel.app.repository;

import com.hotel.app.model.*;
import com.hotel.app.util.FileHelper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataStore {
    public DataStore() {
        // Initial setup if empty
        if (getRooms().isEmpty()) {
            addRoom(new Room("101", RoomType.SINGLE, 100.0, true));
            addRoom(new Room("102", RoomType.DOUBLE, 150.0, true));
            addRoom(new Room("201", RoomType.SUITE, 300.0, true));
        }
    }

    // collection framework list interface
    public List<Room> getRooms() {
        List<Room> list = new ArrayList<>(); // ArrayList
        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM rooms")) {
            while (rs.next()) {
                list.add(new Room(
                        rs.getString("room_number"),
                        RoomType.valueOf(rs.getString("type")),
                        rs.getDouble("price"),
                        rs.getInt("available") == 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Booking> getBookings() {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT b.*, g.name, g.contact, g.id_proof FROM bookings b JOIN guests g ON b.guest_id = g.id")) {
            while (rs.next()) {
                Guest guest = new Guest(rs.getString("guest_id"), rs.getString("name"), rs.getString("contact"),
                        rs.getString("id_proof"));
                Room room = getRoomByNumber(rs.getString("room_number"));
                Booking booking = new Booking(
                        rs.getString("id"),
                        guest,
                        room,
                        LocalDate.parse(rs.getString("check_in")),
                        LocalDate.parse(rs.getString("check_out")));
                booking.setCheckedOut(rs.getInt("checked_out") == 1);
                list.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Room getRoomByNumber(String roomNumber) {
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM rooms WHERE room_number = ?")) {
            pstmt.setString(1, roomNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Room(
                        rs.getString("room_number"),
                        RoomType.valueOf(rs.getString("type")),
                        rs.getDouble("price"),
                        rs.getInt("available") == 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addRoom(Room room) {
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("INSERT OR REPLACE INTO rooms VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType().name());
            pstmt.setDouble(3, room.getPricePerNight());
            pstmt.setInt(4, room.getIsAvailable() ? 1 : 0);
            pstmt.executeUpdate();
            FileHelper.logActivity("JDBC: Added/Updated room " + room.getRoomNumber());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addBooking(Booking booking) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Save Guest
                try (PreparedStatement gStmt = conn
                        .prepareStatement("INSERT OR REPLACE INTO guests VALUES (?, ?, ?, ?)")) {
                    gStmt.setString(1, booking.getGuest().getId());
                    gStmt.setString(2, booking.getGuest().getName());
                    gStmt.setString(3, booking.getGuest().getContact());
                    gStmt.setString(4, booking.getGuest().getIdProof());
                    gStmt.executeUpdate();
                }

                // Save Booking
                try (PreparedStatement bStmt = conn
                        .prepareStatement("INSERT INTO bookings VALUES (?, ?, ?, ?, ?, ?)")) {
                    bStmt.setString(1, booking.getBookingId());
                    bStmt.setString(2, booking.getGuest().getId());
                    bStmt.setString(3, booking.getRoom().getRoomNumber());
                    bStmt.setString(4, booking.getCheckInDate().toString());
                    bStmt.setString(5, booking.getCheckOutDate().toString());
                    bStmt.setInt(6, 0); // checked_out = false
                    bStmt.executeUpdate();
                }

                // Update Room
                try (PreparedStatement rStmt = conn
                        .prepareStatement("UPDATE rooms SET available = 0 WHERE room_number = ?")) {
                    rStmt.setString(1, booking.getRoom().getRoomNumber());
                    rStmt.executeUpdate();
                }

                conn.commit();
                FileHelper.logActivity("JDBC: Transactional booking " + booking.getBookingId() + " completed.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void checkoutBooking(Booking booking) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement bStmt = conn
                        .prepareStatement("UPDATE bookings SET checked_out = 1 WHERE id = ?")) {
                    bStmt.setString(1, booking.getBookingId());
                    bStmt.executeUpdate();
                }

                // NPE safety check for orphaned/deleted rooms
                if (booking.getRoom() != null) {
                    try (PreparedStatement rStmt = conn
                            .prepareStatement("UPDATE rooms SET available = 1 WHERE room_number = ?")) {
                        rStmt.setString(1, booking.getRoom().getRoomNumber());
                        rStmt.executeUpdate();
                    }
                }

                conn.commit();
                FileHelper.logActivity("JDBC: Checkout processed for booking " + booking.getBookingId());
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteRoom(String roomNumber) {
        // Safety check: Cannot delete occupied room
        Room room = getRoomByNumber(roomNumber);
        if (room != null && !room.getIsAvailable())
            return false;

        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM rooms WHERE room_number = ?")) {
            pstmt.setString(1, roomNumber);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                FileHelper.logActivity("JDBC: Room " + roomNumber + " deleted.");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteGuest(String guestId) {
        // Safety check: Cannot delete guest with active (not checked out) bookings
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement checkStmt = conn
                        .prepareStatement("SELECT COUNT(*) FROM bookings WHERE guest_id = ? AND checked_out = 0")) {
            checkStmt.setString(1, guestId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                return false; // Has active bookings

            // Proceed with deletion
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement dBooking = conn.prepareStatement("DELETE FROM bookings WHERE guest_id = ?")) {
                    dBooking.setString(1, guestId);
                    dBooking.executeUpdate();
                }
                try (PreparedStatement dGuest = conn.prepareStatement("DELETE FROM guests WHERE id = ?")) {
                    dGuest.setString(1, guestId);
                    dGuest.executeUpdate();
                }
                conn.commit();
                FileHelper.logActivity("JDBC: Guest " + guestId + " and their histories deleted.");
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateGuest(Guest guest) {
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement pstmt = conn
                        .prepareStatement("UPDATE guests SET name = ?, contact = ?, id_proof = ? WHERE id = ?")) {
            pstmt.setString(1, guest.getName());
            pstmt.setString(2, guest.getContact());
            pstmt.setString(3, guest.getIdProof());
            pstmt.setString(4, guest.getId());
            pstmt.executeUpdate();
            FileHelper.logActivity("JDBC: Guest details updated for " + guest.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Concept: Serialization Backup into a single file
    public void performBackup() {
        BackupData data = new BackupData(getRooms(), getBookings());
        GenericRepository<BackupData, String> genericRepo = new GenericRepository<>("backup.dat");
        List<BackupData> wrapperList = new ArrayList<>();
        wrapperList.add(data);
        genericRepo.saveAll(wrapperList);
        FileHelper.logActivity("Serialization: Full system backup to backup.dat completed.");
    }

    // Generic Method Concept
    public <T> void printListToConsole(List<T> list) {
        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next().toString());
        }
    }
}
