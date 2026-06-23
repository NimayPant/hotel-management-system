package com.hotel.app.model;

import java.io.Serializable;
import java.util.List;

public class BackupData implements Serializable {
    private List<Room> rooms;
    private List<Booking> bookings;

    public BackupData(List<Room> rooms, List<Booking> bookings) {
        this.rooms = rooms;
        this.bookings = bookings;
    }

    public List<Room> getRooms() { return rooms; }
    public List<Booking> getBookings() { return bookings; }
}
