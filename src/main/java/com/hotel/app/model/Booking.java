package com.hotel.app.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Booking implements Serializable {
    private String bookingId;
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private boolean isCheckedOut;

    public Booking(String bookingId, Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this.bookingId = bookingId;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.isCheckedOut = false;
    }
    
    public String getBookingId() { return bookingId; }
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public boolean isCheckedOut() { return isCheckedOut; }
    
    public void setCheckedOut(boolean isCheckedOut) {
        this.isCheckedOut = isCheckedOut;
    }
    public void setCheckOutDate(LocalDate date) {
        this.checkOutDate = date;
    }
}
