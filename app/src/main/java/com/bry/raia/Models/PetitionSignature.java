package com.bry.raia.Models;

public class PetitionSignature {
    private String signerId;
    private String signerUsername;
    private String signerEmail;
    private long timestamp;

    public PetitionSignature(){}

    public PetitionSignature(String signerId, String username, String email, long Timestamp){
        this.signerId = signerId;
        this.signerUsername = username;
        this.signerEmail = email;
        this.timestamp = Timestamp;
    }

    public String getSignerId() {
        return signerId;
    }

    public void setSignerId(String signerId) {
        this.signerId = signerId;
    }

    public String getSignerUsername() {
        return signerUsername;
    }

    public void setSignerUsername(String signerUsername) {
        this.signerUsername = signerUsername;
    }

    public String getSignerEmail() {
        return signerEmail;
    }

    public void setSignerEmail(String signerEmail) {
        this.signerEmail = signerEmail;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
