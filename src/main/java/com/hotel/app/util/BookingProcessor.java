package com.hotel.app.util;

import com.hotel.app.model.Booking;
import javafx.application.Platform;

// Inter-thread communication (wait(), notify())
public class BookingProcessor {
    private Booking pendingBooking;
    private boolean isBookingAvailable = false;
    private Runnable onBookingProcessed;

    public void setOnBookingProcessed(Runnable onBookingProcessed) {
        this.onBookingProcessed = onBookingProcessed;
    }

    public synchronized void submitBooking(Booking booking) {
        while (isBookingAvailable) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.pendingBooking = booking;
        this.isBookingAvailable = true;
        System.out.println("Booking submitted for processing: " + booking.getBookingId());
        FileHelper.logActivity("Booking submitted to processor thread: " + booking.getBookingId());
        notifyAll(); // Notify processor thread
    }

    public synchronized Booking processNextBooking() {
        while (!isBookingAvailable) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Booking b = this.pendingBooking;
        try {
            // Simulate processing time
            Thread.sleep(2000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.pendingBooking = null;
        this.isBookingAvailable = false;
        System.out.println("Booking processed: " + b.getBookingId());
        FileHelper.logActivity("Booking fully processed: " + b.getBookingId());
        
        if (onBookingProcessed != null) {
            Platform.runLater(onBookingProcessed);
        }

        notifyAll(); // Notify submitter thread that processing is done
        return b;
    }
    
    public void startProcessorThread() {
        Thread processorThread = new Thread(() -> {
            while (true) {
                processNextBooking();
            }
        });
        processorThread.setDaemon(true);
        processorThread.start();
    }
}
