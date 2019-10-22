package com.bry.raia.Models;

import android.graphics.Bitmap;

import java.security.Signature;
import java.util.ArrayList;
import java.util.List;

public class Petition {
    private String petitionId;
    private String petitionTitle;
    private String encodedPetitionImage;
    private Bitmap petitionBitmap;
    private long petitionCreationTime;
    private String uploaderId;
    private String uploaderUsername;
    private String uploaderEmail;
    private County county;
    private List<PetitionSignature> signatures = new ArrayList<>();
    private long petitionSignatureTarget;



    public Petition(){}

    public Petition(String title, String image, long creationTime){
        this.petitionTitle = title;
        this.encodedPetitionImage = image;
        this.petitionCreationTime = creationTime;
    }


    public String getPetitionId() {
        return petitionId;
    }

    public void setPetitionId(String petitionId) {
        this.petitionId = petitionId;
    }

    public String getPetitionTitle() {
        return petitionTitle;
    }

    public void setPetitionTitle(String petitionTitle) {
        this.petitionTitle = petitionTitle;
    }


    public Bitmap getPetitionBitmap() {
        return petitionBitmap;
    }

    public void setPetitionBitmap(Bitmap petitionBitmap) {
        this.petitionBitmap = petitionBitmap;
    }

    public long getPetitionCreationTime() {
        return petitionCreationTime;
    }

    public void setPetitionCreationTime(long petitionCreationTime) {
        this.petitionCreationTime = petitionCreationTime;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderUsername() {
        return uploaderUsername;
    }

    public void setUploaderUsername(String uploaderUsername) {
        this.uploaderUsername = uploaderUsername;
    }

    public String getUploaderEmail() {
        return uploaderEmail;
    }

    public void setUploaderEmail(String uploaderEmail) {
        this.uploaderEmail = uploaderEmail;
    }

    public String getEncodedPetitionImage() {
        return encodedPetitionImage;
    }

    public void setEncodedPetitionImage(String encodedPetitionImage) {
        this.encodedPetitionImage = encodedPetitionImage;
    }

    public County getCounty() {
        if(county==null)return new County();
        return county;
    }

    public void setCounty(County county) {
        this.county = county;
    }


    public List<PetitionSignature> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<PetitionSignature> signatures) {
        this.signatures = signatures;
    }

    public void addSignature(PetitionSignature signature){
        this.signatures.add(signature);
    }

    public long getPetitionSignatureTarget() {
        return petitionSignatureTarget;
    }

    public void setPetitionSignatureTarget(long petitionSignatureTarget) {
        this.petitionSignatureTarget = petitionSignatureTarget;
    }
}
