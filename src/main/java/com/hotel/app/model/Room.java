package com.hotel.app.model;

import java.io.Serializable;

public class Room implements Serializable {
    private String roomNumber;
    private RoomType roomType;
    // Wrapper Classes
    private Double pricePerNight;
    private Boolean isAvailable;

    public Room(String roomNumber, RoomType roomType, Double pricePerNight, Boolean isAvailable) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.isAvailable = isAvailable;
    }

    // Method Overloading
    public double calculatePrice(int nights) {
        // Autounboxing happens here implicity
        return pricePerNight * nights; 
    }

    // Method Overloading
    public double calculatePrice(int nights, double discountPercentage) {
        double total = calculatePrice(nights);
        return total - (total * (discountPercentage / 100));
    }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public Double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(Double pricePerNight) { this.pricePerNight = pricePerNight; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean available) { isAvailable = available; }
    
    @Override
    public String toString() {
        return roomNumber + " (" + roomType.getDisplayName() + ")";
    }
}
