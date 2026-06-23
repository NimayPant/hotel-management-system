package com.hotel.app.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Bill implements Serializable {
    private String id;
    private Booking booking;
    private double totalAmount;
    private LocalDate generationDate;

    public Bill(String id, Booking booking, double totalAmount) {
        this.id = id;
        this.booking = booking;
        this.totalAmount = totalAmount;
        this.generationDate = LocalDate.now();
    }
    
    public String getId() { return id; }
    public Booking getBooking() { return booking; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDate getGenerationDate() { return generationDate; }
}
