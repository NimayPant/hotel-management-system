package com.hotel.app.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileHelper {

    // Character Streams Concept - Export Logs for Admin
    public static void exportLogsToCsv() {
        File logFile = new File("activity_log.txt");
        if (!logFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter("System_Log_Export.csv"))) {
            
            writer.write("Timestamp,Action\n"); // CSV Header
            String line;
            while ((line = reader.readLine()) != null) {
                // Converting log format [timestamp] action to CSV
                writer.write(line.replace("[", "").replace("] ", ",") + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Random Access File Concept
    public static void logActivity(String action) {
        try (RandomAccessFile raf = new RandomAccessFile("activity_log.txt", "rw")) {
            raf.seek(raf.length());
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            raf.writeBytes("[" + timestamp + "] " + action + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
