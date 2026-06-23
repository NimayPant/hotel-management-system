package com.hotel.app.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String URL = "jdbc:sqlite:hotel.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");

            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "room_number TEXT PRIMARY KEY," +
                    "type TEXT," +
                    "price REAL," +
                    "available INTEGER)");

            stmt.execute("CREATE TABLE IF NOT EXISTS guests (" +
                    "id TEXT PRIMARY KEY," +
                    "name TEXT," +
                    "contact TEXT," +
                    "id_proof TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "id TEXT PRIMARY KEY," +
                    "guest_id TEXT," +
                    "room_number TEXT," +
                    "check_in TEXT," +
                    "check_out TEXT," +
                    "checked_out INTEGER," +
                    "FOREIGN KEY(guest_id) REFERENCES guests(id)," +
                    "FOREIGN KEY(room_number) REFERENCES rooms(room_number))");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
