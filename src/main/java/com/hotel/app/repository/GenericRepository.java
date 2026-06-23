package com.hotel.app.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Generic classes
// Bounded types 
public class GenericRepository<T extends Serializable, ID> {

    private String filePath;

    public GenericRepository(String filePath) {
        this.filePath = filePath;
    }

    // Input/Output Streams - Byte Streams, Serialization and Deserialization
    public void saveAll(List<T> list) {
        try (FileOutputStream fos = new FileOutputStream(filePath);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> loadAll() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
