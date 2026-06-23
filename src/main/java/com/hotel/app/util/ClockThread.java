package com.hotel.app.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Platform;
import javafx.scene.control.Label;

// Multithreaded Programming and synchronization sleep
public class ClockThread extends Thread {
    private Label timeLabel;
    private volatile boolean running = true;

    public ClockThread(Label timeLabel) {
        this.timeLabel = timeLabel;
        this.setDaemon(true); // Stop thread when app closes
    }

    @Override
    public void run() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        while (running) {
            String currentTime = LocalDateTime.now().format(formatter);
            Platform.runLater(() -> {
                if (timeLabel != null) {
                    timeLabel.setText(currentTime);
                }
            });
            try {
                Thread.sleep(1000); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void terminate() {
        running = false;
    }
}
