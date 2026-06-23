package com.hotel.app.model;

// Inheritance
public class Guest extends Person {
    private String idProof;

    public Guest(String id, String name, String contact, String idProof) {

        super(id, name, contact);
        this.idProof = idProof;
    }

    public String getIdProof() {
        return idProof;
    }

    public void setIdProof(String idProof) {
        this.idProof = idProof;
    }

    @Override
    public String getRoleDetails() {
        return "Guest - ID Proof: " + idProof;
    }
}
