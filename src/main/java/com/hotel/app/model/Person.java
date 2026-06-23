package com.hotel.app.model;

import java.io.Serializable;

// Abstraction
public abstract class Person implements Serializable {
    // Encapsulation
    private String id;
    private String name;
    private String contact;

    public Person(String id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    // Abstract method to be overridden
    public abstract String getRoleDetails();
}
